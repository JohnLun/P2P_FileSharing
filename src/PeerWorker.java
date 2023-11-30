import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class PeerWorker implements Runnable{
    private final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private PeerManager peerManager;
    private Vitals vitals;
    private int peerId;
    private int neighborPeerId;
    private int lastRequestedPieceIndex = -1;
    public double downloadRate;
    private Socket socket;
    private PeerLogger logger;
    private boolean isInitiator;
    private boolean isChoked;
    private boolean neighborIsChoked;
    private boolean isInterested;
    private boolean neighborIsInterested;
    private boolean isAlive = true;
    private boolean lastRequestedPieceSuccessful = true;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private BitSet neighborPiecesToChooseFrom;



    public PeerWorker(PeerManager peerManager, Vitals vitals, Socket socket, int peerId, Optional<Integer> neighborPeerIdOptional) {
        try {
            this.peerManager = peerManager;
            this.vitals = vitals;
            this.socket = socket;
            this.isInterested = false;
            this.neighborIsInterested = false;
            this.isChoked = false;
            this.neighborIsChoked = false;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
            this.peerId = peerId;
            this.resolveNeighborPeerId(neighborPeerIdOptional);
            this.logger = this.vitals.getPeerLogger();
            this.downloadRate = 0.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        for(Peer peer : vitals.getListOfPeers()) {
            peer.setChoked(false);
        }
    }

    public void killWorker()
    {
        this.isAlive = false;
    }

    // Resolve the neighbor peer ID. If the neighbor is the client (ie this is the sender), the neighbor peer id will be passed in as an optional
    // Otherwise, the optional will be empty: This means this peer is the sender, and it will get the neighbor peer id from the handshake message that will come
    private void resolveNeighborPeerId(Optional<Integer> neighborPeerIdOptional) {
        // If the neighbor peer id is present
        if (neighborPeerIdOptional.isPresent()) {
            this.isInitiator = true;
            this.neighborPeerId = neighborPeerIdOptional.get();
        }
        else {
            this.isInitiator = false;
            // this.neighborPeerId will be set once the handshake message is received
        }
    }
    public void run() {
        this.runPeerWorker();
    }

    private void runPeerWorker() {
        this.resolveHandshakes();
        if (this.vitals.getThisPeer().hasEntireFile()) {
            this.sendBitfieldMessage();
        }
        try {
            // Main loop for reading messages from socket and responding
            while (this.isAlive) {
                int actualMessageLength = in.readInt();
                byte[] actualMessageAsBytes = new byte[actualMessageLength];
                in.readFully(actualMessageAsBytes);
                ActualMessage actualMessage = new ActualMessage(actualMessageAsBytes);
                byte messageType = actualMessage.getMessageType();
                this.processActualMessage(actualMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // If this peer is the initiator, send the handshake message and then wait for one back
    // Else, wait for a handshake message and then send one
    private void resolveHandshakes() {
        byte[] neighborHandshakeMessage = new byte[32];
        try {
            if (this.isInitiator) {
                // This worker is the initiator, so send the handshake message and then wait for one from the neighbor
                this.sendHandshakeMessage();
                while (true) {
                    in.read(neighborHandshakeMessage);
                    this.processHandShakeMessage(neighborHandshakeMessage);
                    logger.toTcpConnection(this.neighborPeerId);
                    return;
                }
            }
            else {
                // This worker is not the initiator, so wait for a handshake message and then send one after receival
                while (true) {
                    in.read(neighborHandshakeMessage);
                    this.processHandShakeMessage(neighborHandshakeMessage);
                    logger.fromTcpConnection(this.neighborPeerId);

                    // since the neighbor peer id was not known until now, add this worker and this socket to their corresponding maps
                    vitals.addWorkerToMap(this.neighborPeerId, this);
                    vitals.addSocketToMap(this.neighborPeerId, this.socket);
                    this.sendHandshakeMessage();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send handshake message to neighbor
    public synchronized void sendHandshakeMessage() {
        try {
            byte[] msgToSend = MessageCreator.createHandshakeMessage(this.peerId);
            this.out.write(msgToSend);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Process handshake message received from neighbor
    public void processHandShakeMessage(byte[] neighborHandshakeMessage) {
        String messageHeader = new String(neighborHandshakeMessage, 0, 18, StandardCharsets.UTF_8);
        if (!messageHeader.equals(this.HANDSHAKE_HEADER)) {
            throw new IllegalArgumentException("Incorrect neighbor handshake message header: " + messageHeader);
        }
        byte[] bufferForNeighborPeerId = new byte[4];
        System.arraycopy(neighborHandshakeMessage, 28, bufferForNeighborPeerId, 0, 4);
        int potentialNeighborPeerId = ByteBuffer.wrap(bufferForNeighborPeerId).getInt();

        // If this peer is the initiator, it knows the neighbor peer ID, so double check
        if (this.isInitiator && this.neighborPeerId != potentialNeighborPeerId) {
            throw new IllegalArgumentException("Neighbor peer id stored in this PeerWorker: " + this.neighborPeerId +
                    "\nNeighbor peer id in received handshake message: " + potentialNeighborPeerId);
        }
        else {
            this.neighborPeerId = potentialNeighborPeerId;
        }
    }

    public synchronized void sendActualMessage(byte messageType, byte[] messagePayload) {
        try {
            byte[] msgToSend = MessageCreator.createActualMessage(messageType, messagePayload);
            this.out.write(msgToSend);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processActualMessage(ActualMessage actualMessage) {
        switch (actualMessage.getMessageType()) {
            case (byte) 0x00:
                this.processChokeMessage(actualMessage);
                break;
            case (byte) 0x01:
                this.processUnchokeMessage(actualMessage);
                break;
            case (byte) 0x02:
                this.processInterestedMessage(actualMessage);
                break;
            case (byte) 0x03:
                this.processNotInterestedMessage(actualMessage);
                break;
            case (byte) 0x04:
                this.processHaveMessage(actualMessage);
                break;
            case (byte) 0x05:
                this.processBitFieldMessage(actualMessage);
                break;
            case (byte) 0x06:
                this.processRequestMessage(actualMessage);
                break;
            case (byte) 0x07:
                this.processPieceMessage(actualMessage);
                break;
            default:
                throw new IllegalArgumentException("The message type of the actual message is invalid. Provided message type: " + actualMessage.getMessageType());
        }
    }

    public void sendChokeMessage() {
        this.neighborIsChoked = true;
        this.sendActualMessage((byte)0x00, new byte[0]);
    }

    // When this message is received, it means that the neighbor is choking this message.
    public void processChokeMessage(ActualMessage actualMessage) {
        this.isChoked = true;

        // If the last request was not completed before choking, set the bitfield value to false
        if (!this.lastRequestedPieceSuccessful && this.lastRequestedPieceIndex != -1) {
            this.vitals.getBitSet().set(this.lastRequestedPieceIndex, false);
        }
        logger.choke(this.neighborPeerId);
    }

    public void sendUnchokeMessage() {
        this.neighborIsChoked = false;
        this.sendActualMessage((byte)0x01, new byte[0]);
    }

    // When this message is received, it means that the neighbor is unchoking this peer
    public void processUnchokeMessage(ActualMessage actualMessage) {
        this.isChoked = false;
        logger.unchoke(this.neighborPeerId);
        if (this.lastRequestedPieceSuccessful)
        {
            this.sendRequestMessage();
        }

    }

    public void sendInterestedMessage() {
        this.isInterested = true;
        this.sendActualMessage((byte)0x02, new byte[0]);
    }

    // When this message is received, it means that the neighbor is interested
    public void processInterestedMessage(ActualMessage actualMessage) {
        this.vitals.addToInterested(this.neighborPeerId);
        this.neighborIsInterested = true;
        logger.receiveInterested(this.neighborPeerId);
    }

    public void sendNotInterestedMessage() {
        this.isInterested = false;
        this.sendActualMessage((byte)0x03, new byte[0]);
    }

    // When this message is received, it means that the neighbor is not interested
    public void processNotInterestedMessage(ActualMessage actualMessage) {
        this.vitals.removeFromInterested(this.neighborPeerId);
        this.neighborIsInterested = false;
        logger.receiveNotInterested(this.neighborPeerId);
    }

    public void sendHaveMessage(int index) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(index);
        byte[] indexAsBytes = b.array();
        this.sendActualMessage((byte)0x04, indexAsBytes);
    }

    public void processHaveMessage(ActualMessage actualMessage) {
        int pieceIndex = ByteBuffer.wrap(actualMessage.getMessagePayload()).getInt();

        // Update the pieces to choose from, since the neighbor is signalling that it has a new piece
        this.vitals.mapOfPeerBitfields.get(this.neighborPeerId).set(pieceIndex);

        logger.receiveHave(this.neighborPeerId, pieceIndex);

        // If this peer does not have the piece that the neighbor has, send an interested message
        if (!vitals.getBitSet().get(pieceIndex)) {
            this.sendInterestedMessage();
        } else {
            this.sendNotInterestedMessage();
        }

        // Check if all peers are done
        if (this.vitals.areAllPeersComplete()) {
            this.peerManager.terminate();
        }
    }

    public void sendBitfieldMessage() {
        byte[] bitfield = vitals.getBitSet().toByteArray();
        this.sendActualMessage((byte)0x05, bitfield);
    }

    public void processBitFieldMessage(ActualMessage actualMessage) {
        BitSet neighborBitSet = BitSet.valueOf(actualMessage.getMessagePayload());

        // Replace the default empty bitset with the received bitfield
        this.vitals.mapOfPeerBitfields.put(this.neighborPeerId, neighborBitSet);

        // Get the difference of the neighbor bitset and our bitset
        // the result is a bitset of the pieces we don't have
        this.neighborPiecesToChooseFrom = (BitSet) neighborBitSet.clone();
        this.neighborPiecesToChooseFrom.andNot(vitals.getBitSet());

        // If the neighbor has pieces we don't, send interested message
        if (this.neighborPiecesToChooseFrom.cardinality() > 0) {
            this.sendInterestedMessage();
            this.sendRequestMessage();
        }
        else {
            this.sendNotInterestedMessage();
        }
    }

    public void sendRequestMessage() {
        if (this.isChoked || !this.isInterested) {
            return;
        }

        // Check if the last request piece was successful. If unsuccessful, reset the bitfield index to false
        if (!this.lastRequestedPieceSuccessful && this.lastRequestedPieceIndex != -1) {
            this.vitals.getBitSet().set(this.lastRequestedPieceIndex, false);
        }

        int index = this.chooseIndexForRequestMessage();

        // If there are no pieces that this peer wants, send a not interested message
        if (index == -1) {
            this.sendNotInterestedMessage();
            return;
        }

        // Set the piece here and not when the piece is received, so that other threads do not request the same piece
        this.vitals.getBitSet().set(index);
        this.lastRequestedPieceIndex = index;
        this.lastRequestedPieceSuccessful = false;

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(index);
        byte[] indexAsBytes = b.array();
        this.sendActualMessage((byte)0x06, indexAsBytes);
    }

    private int chooseIndexForRequestMessage() {
        // Refresh the pieces to choose from, since other threads may have updated our bitfield
        this.neighborPiecesToChooseFrom = (BitSet) this.vitals.mapOfPeerBitfields.get(this.neighborPeerId).clone();
        this.neighborPiecesToChooseFrom.andNot(this.vitals.getBitSet());


        // Get a random piece position
        int randomPosition = ThreadLocalRandom.current().nextInt(0, this.vitals.getNumPiecesInFile());

        // Get the closest piece index to our randomly generated position
        int pieceIndex =  this.neighborPiecesToChooseFrom.nextSetBit(randomPosition);
        if (pieceIndex == -1) {
            pieceIndex = this.neighborPiecesToChooseFrom.previousSetBit(randomPosition);
        }

        return pieceIndex;
    }

    public void processRequestMessage(ActualMessage actualMessage) {
        if (this.neighborIsChoked) {
            return;
        }
        
        int pieceIndex = ByteBuffer.wrap(actualMessage.getMessagePayload()).getInt();
        if (vitals.getBitSet().get(pieceIndex)) {
            this.sendPieceMessage(pieceIndex);
        }
        else {
            throw new IllegalArgumentException("This peer received a request message for a piece that it does not have " +
                    "from neighbor " + this.neighborPeerId);
        }
    }

    public void sendPieceMessage(int pieceIndex) {
        if (pieceIndex >= this.vitals.getNumPiecesInFile()) {
            throw new IllegalArgumentException("The requested piece index to be sent is out of bounds.");
        }
        byte[] piecePayload = vitals.getPiecePayload(pieceIndex);
        this.sendActualMessage((byte)0x07, piecePayload);
    }

    public void processPieceMessage(ActualMessage actualMessage) {
        // Extract index and data from message
        ByteBuffer buffer = ByteBuffer.wrap(actualMessage.getMessagePayload());
        int pieceIndex = buffer.getInt();
        byte[] pieceData = new byte[buffer.remaining()];
        buffer.get(pieceData);

        if (pieceIndex >= this.vitals.getNumPiecesInFile()) {
            throw new IllegalArgumentException("The received piece index is out of bounds.");
        }

        if (this.lastRequestedPieceIndex != pieceIndex) {
            throw new IllegalArgumentException("The piece index " + this.lastRequestedPieceIndex + " was last requested " +
                    "but the index " + pieceIndex + " was received");
        }

        // Put data in vitals (and therefore this peer)
        this.vitals.putPiece(pieceIndex, pieceData);
        this.downloadRate++;

        this.lastRequestedPieceSuccessful = true;

        this.logger.downloadPiece(this.neighborPeerId, pieceIndex, this.vitals.getNumPiecesDownloaded());

        this.peerManager.sendHaveMessageToAllNeighbors(pieceIndex);

        this.vitals.isThisPeerComplete();
        if (this.vitals.areAllPeersComplete()) {
            try
            {
                this.peerManager.terminate();
            }
            catch (Exception e) {}

        }


        // Finally, request another piece if this peer is not choked and is interested
        //TODO just see if the areAllPeers complete call screws up an edge case
        if (!this.isChoked && this.isInterested && !this.vitals.areAllPeersComplete()) {
            this.sendRequestMessage();
        }
    }

    public void setChoked(boolean choked) {
        this.isChoked = choked;
    }

    public boolean getInterested() {
        return this.isInterested;
    }

    public int getPeerId() {
        return this.peerId;
    }

    public boolean equals(PeerWorker peerWorker) {
        if (this.peerId == peerWorker.getPeerId()) {
            return true;
        }
        return false;
    }

    public void setDownloadRate(double dr) {
        this.downloadRate = dr;
    }

    public double getDownloadRate() {
        return this.downloadRate;
    }

    public int getNeighborPeerId() {
        return this.neighborPeerId;
    }
}

