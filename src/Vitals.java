

// This class will have all important information that will be used by the threads of the running process
import java.net.ServerSocket;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.SocketHandler;

public class Vitals {
    //TODO: Add getters and setters to private vars
    private int peerId;
    private CommonConfigHelper commonConfigHelper;
    private PeerInfoConfigHelper peerInfoConfigHelper;
    private HashMap<Integer, Peer> mapOfPeers;
    private Peer peer;
    private BitSet bitfield;
    private int numPiecesInFile;
    private int numPiecesDownloaded;
    private ServerSocket listener;
    private Vector<Integer> preferredNeighbors;

    private PeerLogger peerLogger;

    // Map of PeerWorker threads, where the key is the Neighbor peerId
    // TODO: May need to be modified
    private HashMap<Integer, PeerWorker> mapOfWorkers;

    // TODO: add interested, not interested, etc

    public Vitals(int peerId, CommonConfigHelper commonConfigHelper, PeerInfoConfigHelper peerInfoConfigHelper, ServerSocket listener) {
        this.peerId = peerId;
        this.commonConfigHelper = commonConfigHelper;
        this.peerInfoConfigHelper = peerInfoConfigHelper;
        this.listener = listener;
        this.initVitals();
        preferredNeighbors = new Vector<Integer>();
    }


    public void createPreferredNeighbors() {
        int randomNum = 0;
        Vector<Integer> listOfPeers = peerInfoConfigHelper.getListOfPeers();
        int k = commonConfigHelper.getNumPreferredNeighbors();
        for(int i = 0; i < k; i++) {
            while(listOfPeers.get(randomNum) != this.peer.getPeerId()) {
                randomNum = ThreadLocalRandom.current().nextInt(0, listOfPeers.size());
            }
            this.preferredNeighbors.add(listOfPeers.get(randomNum));
        }
    }

    // Initiate basic vitals
    private void initVitals() {
        this.numPiecesDownloaded = 0;
        this.mapOfPeers = peerInfoConfigHelper.getMapOfPeers();
        this.peer = this.mapOfPeers.get(this.peerId);
        this.initBitField();
    }

    // Create bitfield for this peer
    private void initBitField() {
        this.numPiecesInFile = (int)Math.ceil((double)commonConfigHelper.getFileSize() / commonConfigHelper.getPieceSize());
        this.bitfield = new BitSet(this.numPiecesInFile);

        // Set all bits to 1 if this peer has the entire file
        if (this.peer.hasEntireFile()) {
            this.bitfield.set(0, numPiecesInFile - 1, true);
            this.numPiecesDownloaded = this.numPiecesInFile;
        }
    }

    // Get the port number of a specified peer
    public int getPort(int peerId) {
        return this.mapOfPeers.get(peerId).getPort();
    }

    // Adds workers to hashmap as they are created
    public void addWorkerToMap(int neighborPeerId, PeerWorker peerWorker) {
        this.mapOfWorkers.put(neighborPeerId, peerWorker);
    }

    public Peer getPeer() {
        return this.peer;
    }

    public Vector<Integer> getPreferredNeighbors() {
        return this.preferredNeighbors;
    }
}
