import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Vector;

public class PeerConnectionHandler implements Runnable{
    private int peerId;
    private boolean isAlive;
    private ServerSocket listener;
    private PeerManager peerManager;
    private Vitals vitals;
    public PeerConnectionHandler(int peerId, ServerSocket listener, PeerManager peerManager, Vitals vitals) {
        this.peerId = peerId;
        this.isAlive = true;
        this.listener = listener;
        this.peerManager = peerManager;
        this.vitals = vitals;
    }

    public void run() {
        this.executePeerConnectionHandler();
    }

    private void executePeerConnectionHandler() {
        this.connectToExistingNeighbors();
        this.listenForNewConnections();
    }

    // Establish a connection with all peers before this one in the PeerConfig file
    private void connectToExistingNeighbors() {
        try {
            Thread.sleep(0000);
            Vector<Peer> listOfPeers = vitals.getListOfPeers();
            for (Peer neighbor:listOfPeers) {
                if (neighbor.getPeerId() == this.peerId) {
                    break;
                }
                Socket socket = new Socket(neighbor.getHostName(), neighbor.getPort());
                PeerWorker peerWorker = new PeerWorker(peerManager, vitals, socket, this.peerId, Optional.of(neighbor.getPeerId()));
                vitals.addSocketToMap(neighbor.getPeerId(), socket);
                vitals.addWorkerToMap(neighbor.getPeerId(), peerWorker);
                Thread thread = new Thread(peerWorker);
                thread.start();
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    // Listen for incoming connections from peers that are after this peer in the PeerConfig file
    // Since the neighborId is unknown, the map of sockets and map of workers will be updated within the worker thread, and NOT here
    private void listenForNewConnections() {
        try {
            System.out.println("Checking for incoming connections");
            while (isAlive) {
                System.out.println("listening for new connections");
                Socket socket = this.listener.accept();
                PeerWorker peerWorker = new PeerWorker(peerManager, vitals, socket, this.peerId, Optional.empty());
                Thread thread = new Thread(peerWorker);
                thread.start();
            }
        } catch (IOException e) {
            //TODO most exception prints stem from here
            e.printStackTrace(); // most errors will come from here
        }
    }

    public void kill() {
        this.isAlive = false;
    }
}
