
import java.io.IOException;
import java.net.ServerSocket;

// The PeerManager class manages this process's peer and all of its threads
// There will be a thread for each connection with another peer, along with a threat that accepts new connections
public class PeerManager {
    private int peerId;
    private CommonConfigHelper commonConfigHelper;
    private PeerInfoConfigHelper peerInfoConfigHelper;
    private Vitals vitals;
    private ServerSocket listener;

    public PeerManager(int peerId) {
        this.peerId = peerId;
        this.commonConfigHelper = new CommonConfigHelper("Common.cfg");
        this.peerInfoConfigHelper = new PeerInfoConfigHelper("PeerInfo.cfg");
    }

    public void run() {
        this.executePeerManager();
    }
    private void executePeerManager() {
        try {
            this.vitals = new Vitals(peerId, this.commonConfigHelper, this.peerInfoConfigHelper, this.listener);
            this.listener = new ServerSocket(this.vitals.getPort(this.peerId));
            this.runPeerConnectionHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create and run peerConnectionHandler
    // This thread makes connections with already existing peers and listens for future connections
    private void runPeerConnectionHandler() {
        PeerConnectionHandler peerConnectionHandler = new PeerConnectionHandler(this.peerId, listener, this.vitals);
        Thread thread = new Thread(peerConnectionHandler);
        thread.start();
    }

}
