import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;

public class PeerWorker implements Runnable{
    private Vitals vitals;
    private int peerId;
    private int neighborPeerId;
    private Socket socket;
    private boolean isChoked;
    private boolean isInterested;
    private PeerLogger logger;
    private int receiverId;
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

    }

    public void sendNotInterestedMessage() {

    }

    public void sendHaveMessage() {

    }

    public void sendBitfieldMessage() {

    }
    public void sendRequestMessage() {

    }

    public void sendPieceMessage() {

    }
}
