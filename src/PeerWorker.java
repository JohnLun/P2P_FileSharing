import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;

public class PeerWorker implements Runnable{
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
    }

    // If this peer is the initiator, send the handshake message and then wait for one back
    // Else, wait for a handshake message and then send one
    private void resolveHandshakes() {

    }
    public void sendHandshakeMessage() {
        try {
            byte[] msgToSend = MessageCreator.createHandshakeMessage(this.peerId);
            out.write(msgToSend);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void sendChokeMessage() {

    }

    public void sendUnchokeMessage() {

    }

    public void sendInterestedMessage() {
        this.sendActualMessage((byte)0x02, new byte[0]);
    }

    public void sendNotInterestedMessage() {
        this.sendActualMessage((byte)0x03, new byte[0]);
    }

    public void sendHaveMessage(int index) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(index);
        byte[] indexAsBytes = b.array();
        this.sendActualMessage((byte)0x04, indexAsBytes);
    }

    public void sendBitfieldMessage() {
        byte[] bitfield = vitals.getBitSet().toByteArray();
        this.sendActualMessage((byte)0x05, bitfield);
    }
    public void sendRequestMessage() {

    }

    public void sendPieceMessage() {
        //byte[] message = MessageCreator.createActualMessage((byte)0x07, vitals.convertToPiece(0));
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

