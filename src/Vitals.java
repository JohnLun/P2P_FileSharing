

// This class will have all important information that will be used by the threads of the running process
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

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

    private HashMap<Integer, Socket> mapOfSockets;
    private Vector<Peer> preferredNeighbors;

    private PeerLogger peerLogger;
    private byte[] data;



    // Map of PeerWorker threads, where the key is the Neighbor peerId
    // TODO: May need to be modified
    private HashMap<Integer, PeerWorker> mapOfWorkers;

    // TODO: add interested, not interested, etc

    public Vitals(int peerId, CommonConfigHelper commonConfigHelper, PeerInfoConfigHelper peerInfoConfigHelper, ServerSocket listener) {
        this.peerId = peerId;
        this.commonConfigHelper = commonConfigHelper;
        this.peerInfoConfigHelper = peerInfoConfigHelper;
        this.listener = listener;
        preferredNeighbors = new Vector<Peer>();
        data = new byte[commonConfigHelper.getFileSize()];
        this.initVitals();
    }

    // Initiate basic vitals
    private void initVitals() {
        this.mapOfPeers = peerInfoConfigHelper.getMapOfPeers();
        this.peer = this.mapOfPeers.get(this.peerId);
        this.mapOfSockets = new HashMap<Integer, Socket>();
        this.initBitFieldAndData();
    }

    // Create bitfield for this peer and the data array if this peer has the entire file
    private void initBitFieldAndData() {
        this.numPiecesInFile = (int)Math.ceil((double)commonConfigHelper.getFileSize() / commonConfigHelper.getPieceSize());
        this.bitfield = new BitSet(this.numPiecesInFile);

        // Set all bits to 1 if this peer has the entire file
        if (this.peer.hasEntireFile()) {
            this.bitfield.set(0, numPiecesInFile - 1, true);
            this.numPiecesDownloaded = this.numPiecesInFile;
            
            // If our peer has the entire file according to the config, read the file into our data array
            this.readEntireFile(this.commonConfigHelper.getFileName());
        }
        else {
            this.numPiecesDownloaded = 0;
        }
    }

    // If a peer has the entire file, this function reads it
    private void readEntireFile(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(this.data);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPreferredNeighbors() {
        int randomNum = 0;
        Vector<Peer> listOfPeers = peerInfoConfigHelper.getListOfPeers();
        int k = commonConfigHelper.getNumPreferredNeighbors();
        for(int i = 0; i < k; i++) {
            while(listOfPeers.get(randomNum).getPeerId() != this.peer.getPeerId()) {
                randomNum = ThreadLocalRandom.current().nextInt(0, listOfPeers.size());
            }
            this.preferredNeighbors.add(listOfPeers.get(randomNum));
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

    // Adds sockets to hashmap as they are created
    public void addSocketToMap(int neighborPeerId, Socket socket) {
        this.mapOfSockets.put(neighborPeerId, socket);
    }

    public Peer getThisPeer() {
        return this.peer;
    }

    public int getThisPeerId() {
        return this.peerId;
    }

    public Vector<Peer> getPreferredNeighbors() {
        return this.preferredNeighbors;
    }

    public Vector<Peer> getListOfPeers() {
        return peerInfoConfigHelper.getListOfPeers();
    }

    public BitSet getBitSet() {
        return this.bitfield;
    }
}
