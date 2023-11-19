import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PeerWorker implements Runnable{
    private final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private Vitals vitals;
    private int peerId;
    private int neighborPeerId;
    private Socket socket;
    private PeerLogger logger;
    private boolean isInitiator;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public PeerWorker(Vitals vitals, Socket socket, int peerId, Optional<Integer> neighborPeerIdOptional) {
        try {
            this.vitals = vitals;
            this.socket = socket;
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.peerId = peerId;
            this.resolveNeighborPeerId(neighborPeerIdOptional);
            logger = new PeerLogger(this.vitals);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        for(Peer peer : vitals.getListOfPeers()) {
            peer.setChoked(false);
        }
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
        // TODO: Deal with sending initial bit fields
        try {
            // Main loop for reading messages from socket and responding
            while (true) {
                int actualMessageLength = in.readInt();
                byte[] actualMessageAsBytes = new byte[actualMessageLength];
                in.read(actualMessageAsBytes);
                ActualMessage actualMessage = new ActualMessage(actualMessageAsBytes);
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
                    return;
                }
            }
            else {
                // This worker is not the initiator, so wait for a handshake message and then send one after receival
                while (true) {
                    in.read(neighborHandshakeMessage);
                    this.processHandShakeMessage(neighborHandshakeMessage);

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
    public void sendHandshakeMessage() {
        try {
            byte[] msgToSend = MessageCreator.createHandshakeMessage(this.peerId);
            out.write(msgToSend);
            out.flush();
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

    public void sendActualMessage(byte messageType, byte[] messagePayload) {
        try {
            byte[] msgToSend = MessageCreator.createActualMessage(messageType, messagePayload);
            out.write(msgToSend);
            out.flush();
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
        this.sendActualMessage((byte)0x00, new byte[0]);
    }

    public void processChokeMessage(ActualMessage actualMessage) {

    }

    public void sendUnchokeMessage() {
        this.sendActualMessage((byte)0x01, new byte[0]);
    }

    public void processUnchokeMessage(ActualMessage actualMessage) {

    }

    public void sendInterestedMessage() {
        this.sendActualMessage((byte)0x02, new byte[0]);
    }

    public void processInterestedMessage(ActualMessage actualMessage) {

    }

    public void sendNotInterestedMessage() {
        this.sendActualMessage((byte)0x03, new byte[0]);
    }

    public void processNotInterestedMessage(ActualMessage actualMessage) {

    }

    public void sendHaveMessage(int index) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(index);
        byte[] indexAsBytes = b.array();
        this.sendActualMessage((byte)0x04, indexAsBytes);
    }

    public void processHaveMessage(ActualMessage actualMessage) {

    }

    public void sendBitfieldMessage() {
        byte[] bitfield = vitals.getBitSet().toByteArray();
        this.sendActualMessage((byte)0x05, bitfield);
    }

    public void processBitFieldMessage(ActualMessage actualMessage) {

    }

    public void sendRequestMessage() {

    }

    public void processRequestMessage(ActualMessage actualMessage) {

    }

    public void sendPieceMessage() {
        //byte[] message = MessageCreator.createActualMessage((byte)0x07, vitals.convertToPiece(0));
    }

    public void processPieceMessage(ActualMessage actualMessage) {

    }

    public void checkIfHave() {
//        for(int i = 0; i < vitals.getBitSet().length(); i++) {
//            if(vitals.getBitSet().get(i) && !receiverPeer.getBitSet().get(i)) {
//                sendHaveMessage(i);
//                break;
//            }
//        }
    }
    public void checkMissingPieces() {
//        boolean foundMissingPiece = false;
//        for(int i = 0; i < vitals.getBitSet().length(); i++) {
//            if(!(vitals.getBitSet().get(i)) && receiverPeer.getBitSet().get(i)) {
//                vitals.getThisPeer().setInterested(true);
//                foundMissingPiece = true;
//                sendInterestedMessage();
//                break;
//            }
//        }
//
//        if(!foundMissingPiece) {
//            sendNotInterestedMessage();
//        }
    }
}

