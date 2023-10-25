import com.sun.xml.internal.ws.api.message.Message;

public class PeerWorker implements Runnable{
    private Vitals vitals;
    private PeerLogger logger;
    private Vitals receiverPeer;
    private boolean isSender;

    public PeerWorker(Vitals vitals) {
        this.vitals = vitals;
        logger = new PeerLogger(this.vitals);
        this.isSender = false;
    }

    public void init() {
        for(Peer peer : vitals.getAllPeers()) {
            peer.setChoked(false);
        }
    }

    public void run() {
        this.runPeerWorker();
    }

    private void runPeerWorker() {

    }

    public void sendHandshakeMessage() {

    }

    public void sendChokeMessage() {

    }

    public void sendUnchokeMessage() {

    }

    public void sendInterestedMessage() {
        byte[] message = MessageCreator.createActualMessage((byte)0x02, null);
    }

    public void sendNotInterestedMessage() {
        byte[] message = MessageCreator.createActualMessage((byte)0x03, null);
    }

    public void sendHaveMessage(int index) {
        byte[] message = MessageCreator.createActualMessage((byte)0x04, vitals.convertToPiece(index));
    }

    public void sendBitfieldMessage() {
        if(!(vitals.getBitSet().isEmpty())) {
            byte[] message = MessageCreator.createActualMessage((byte)0x05, vitals.convertToByteArr());

        }
    }
    public void sendRequestMessage() {

    }

    public void sendPieceMessage() {
        byte[] message = MessageCreator.createActualMessage((byte)0x07, vitals.convertToPiece(0));
    }

    public void checkIfHave() {
        for(int i = 0; i < vitals.getBitSet().length(); i++) {
            if(vitals.getBitSet().get(i) && !receiverPeer.getBitSet().get(i)) {
                sendHaveMessage(i);
                break;
            }
        }
    }
    public void checkMissingPieces() {
        boolean foundMissingPiece = false;
        for(int i = 0; i < vitals.getBitSet().length(); i++) {
            if(!(vitals.getBitSet().get(i)) && receiverPeer.getBitSet().get(i)) {
                vitals.getPeer().setInterested(true);
                foundMissingPiece = true;
                sendInterestedMessage();
                break;
            }
        }

        if(!foundMissingPiece) {
            sendNotInterestedMessage();
        }
    }
}
