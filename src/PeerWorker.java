
public class PeerWorker implements Runnable{
    private Vitals vitals;
    private boolean isChoked;
    private boolean isInterested;
    private PeerLogger logger;
    private int receiverId;
    private boolean isSender;

    public PeerWorker(Vitals vitals) {
        this.vitals = vitals;
        logger = new PeerLogger(this.vitals);
        this.isSender = false;
    }

    public void run() {
        this.runPeerWorker();
    }

    private void runPeerWorker() {

    }

    public void sendHandshakeMessage() {

    }

    public void sendActualMessage() {

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
