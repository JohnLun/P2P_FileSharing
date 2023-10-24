
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
        this.initPeerManager();
    }

    private void initPeerManager() {
        try {
            this.listener = new ServerSocket(vitals.getPort(peerId));
            this.vitals = new Vitals(peerId, this.commonConfigHelper, this.peerInfoConfigHelper, this.listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
