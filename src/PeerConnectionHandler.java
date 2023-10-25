import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class PeerConnectionHandler implements Runnable{
    private int peerId;
    private ServerSocket listener;
    private Vitals vitals;
    public PeerConnectionHandler(int peerId, ServerSocket listener, Vitals vitals) {
        this.peerId = peerId;
        this.listener = listener;
        this.vitals = vitals;
    }

    public void run() {
        this.executePeerConnectionHandler();
    }

    private void executePeerConnectionHandler() {

    }

    // Establish a connection with all peers before this one in the PeerConfig file
    private void connectToExistingNeighbors() {
        try {
            Thread.sleep(5000);
            Vector<Peer> listOfPeers = vitals.getListOfPeers();
            for (Peer neighbor:listOfPeers) {
                if (neighbor.getPeerId() == this.peerId) {
                    break;
                }
                Socket socket = new Socket(neighbor.getHostName(), neighbor.getPort());
                vitals.addSocketToMap(neighbor.getPeerId(), socket);
                PeerWorker peerWorker = new PeerWorker(vitals, socket);
                Thread thread = new Thread(peerWorker);
                thread.start();
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewConnections() {

    }

}
