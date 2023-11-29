
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// The PeerManager class manages this process's peer and all of its threads
// There will be a thread for each connection with another peer, along with a threat that accepts new connections
public class PeerManager {
    private int peerId;
    private CommonConfigHelper commonConfigHelper;
    private PeerInfoConfigHelper peerInfoConfigHelper;
    private volatile Vitals vitals;
    private ServerSocket listener;

    private Thread connectionHandlerThread;
    private ScheduledExecutorService scheduler1;
    private ScheduledExecutorService scheduler2;
    private NeighborHandler neighborHandler;
    private OptimisticallyUnchokedNeighborHandler optimisticallyUnchokedNeighborHandler;

    public PeerManager(int peerId) {
        this.peerId = peerId;
        this.commonConfigHelper = new CommonConfigHelper("Common.cfg");
        this.peerInfoConfigHelper = new PeerInfoConfigHelper("PeerInfo.cfg");
        this.scheduler1 = Executors.newSingleThreadScheduledExecutor();
        this.scheduler2 = Executors.newSingleThreadScheduledExecutor();
    }

    public void run() {
        this.runPeerManager();
    }
    private void runPeerManager() {
        try {
            this.vitals = new Vitals(peerId, this.commonConfigHelper, this.peerInfoConfigHelper, this.listener);
            this.listener = new ServerSocket(this.vitals.getPort(this.peerId));
            this.neighborHandler = new NeighborHandler(this.vitals);
            this.optimisticallyUnchokedNeighborHandler = new OptimisticallyUnchokedNeighborHandler();
            this.scheduler1.scheduleAtFixedRate(this.neighborHandler, 6, this.vitals.getUnchokingInterval(), TimeUnit.SECONDS);
            this.scheduler2.scheduleAtFixedRate(this.optimisticallyUnchokedNeighborHandler, 6, this.vitals.getOptimisticallyUnchokedInterval(), TimeUnit.SECONDS);
            this.runPeerConnectionHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create and run peerConnectionHandler
    // This thread makes connections with already existing peers and listens for future connections
    private void runPeerConnectionHandler() {
        PeerConnectionHandler peerConnectionHandler = new PeerConnectionHandler(this.peerId, listener, this, this.vitals);
        connectionHandlerThread = new Thread(peerConnectionHandler);
        connectionHandlerThread.start();
    }

    // Send a have message to all neighbors
    public synchronized void sendHaveMessageToAllNeighbors(int piece) {
        for (Peer peer:this.vitals.getListOfPeers()) {
            int peerId = peer.getPeerId();

            // There is no worker where the key is this peer's peer id, so skip this peer id
            if (peerId != this.peerId) {
                this.vitals.getWorker(peerId).sendHaveMessage(piece);
            }
        }
    }

    public void terminate() {
        try {
            // Signal all peer workers to stop
            for (Peer peer : vitals.getListOfPeers()) {
                PeerWorker worker = vitals.getWorker(peer.getPeerId());
                if (worker != null) {
                    worker.killWorker();
                }
            }

            connectionHandlerThread.interrupt();

            // Close the server socket to release the port
            if (listener != null && !listener.isClosed()) {
                listener.close();
            }

            this.scheduler1.shutdownNow();
            this.scheduler2.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
