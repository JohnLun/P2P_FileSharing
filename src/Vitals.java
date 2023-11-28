// This class will have all important information that will be used by the threads of the running process
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Vitals {
    private CommonConfigHelper commonConfigHelper;
    private PeerInfoConfigHelper peerInfoConfigHelper;
    private HashMap<Integer, Peer> mapOfPeers;
    private HashMap<Integer, PeerWorker> mapOfWorkers;

    private HashMap<Integer, Double> mapOfDownloadRates;
    private HashMap<Integer, PeerWorker> unchokedPeers;
    private HashMap<Integer, PeerWorker> interestedPeers;
    private HashMap<Integer, Socket> mapOfSockets;
    public HashMap<Integer, BitSet> mapOfPeerBitfields; // Map of bitfields for all peers, including this one
    private Peer peer;
    private BitSet bitfield;
    private int peerId;
    private int numPiecesInFile;
    private int numPiecesDownloaded;
    private ServerSocket listener;

    private Vector<Peer> preferredNeighbors;
    private PeerLogger peerLogger;
    private byte[] data;

    private volatile boolean shouldTerminate = false;



    // Constructor
    public Vitals(int peerId, CommonConfigHelper commonConfigHelper, PeerInfoConfigHelper peerInfoConfigHelper, ServerSocket listener) {
        this.peerId = peerId;
        this.commonConfigHelper = commonConfigHelper;
        this.peerInfoConfigHelper = peerInfoConfigHelper;
        this.listener = listener;
        preferredNeighbors = new Vector<Peer>();
        data = new byte[commonConfigHelper.getFileSize()];
        this.initVitals();
        this.interestedPeers = new HashMap<Integer, PeerWorker>();
        this.unchokedPeers = new HashMap<Integer, PeerWorker>();
        this.mapOfDownloadRates = new HashMap<Integer, Double>();
    }

    // Content Functions ///////////////////////////////////////////////////////////////////////////////////////////////

    // Initiate basic vitals
    private void initVitals() {
        this.mapOfPeers = peerInfoConfigHelper.getMapOfPeers();
        this.mapOfPeerBitfields = new HashMap<>();
        this.peer = this.mapOfPeers.get(this.peerId);
        this.mapOfSockets = new HashMap<Integer, Socket>();
        this.peerLogger = new PeerLogger(this);
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

        // Put this peer's bitfield in the map of all bitfields
        this.mapOfPeerBitfields.put(this.peerId, this.bitfield);

        // Initialize neighbor bitfields and put them into the map of all bitfields
        for (Peer peer:this.getListOfPeers()) {
            if (peer.getPeerId() != this.peerId) {
                this.mapOfPeerBitfields.put(peer.getPeerId(), new BitSet());
            }
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

    // This function returns an array with the first four bytes as the index and the rest as the piece data
    public byte[] getPiecePayload(int index) {
        // Get the actual piece
        byte[] piece = this.getPiece(index);

        // Convert the index to an int
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(index);
        byte[] indexAsBytes = b.array();

        // Create final return object
        byte[] finalPayload = new byte[indexAsBytes.length + piece.length];

        // Copy index and piece into final return object
        System.arraycopy(indexAsBytes, 0, finalPayload, 0, 4);
        System.arraycopy(piece, 0, finalPayload, 4, piece.length);

        return finalPayload;
    }
    private byte[] getPiece(int index) {
        // If the requested piece is the last piece in the file AND the last piece is smaller than a normal piece
        if (index == this.numPiecesInFile - 1 &&
                this.commonConfigHelper.getFileSize() % this.commonConfigHelper.getPieceSize() != 0) {
            return this.getLastPiece(index);
        }
        else {
            // Create an empty byte array of piece size and copy the piece over from this.data
            byte[] piece = new byte[this.commonConfigHelper.getPieceSize()];
            int startIndex = index * this.commonConfigHelper.getPieceSize();
            System.arraycopy(this.data, startIndex, piece, 0, this.commonConfigHelper.getPieceSize());
            return piece;
        }
    }

    private byte[] getLastPiece(int index) {
        // Get the length of the last piece
        int lastPieceLength = this.commonConfigHelper.getFileSize() % this.commonConfigHelper.getPieceSize();

        // Get the piece data
        byte[] piece = new byte[lastPieceLength];
        int startIndex = index * this.commonConfigHelper.getPieceSize();
        System.arraycopy(this.data, startIndex, piece, 0, lastPieceLength);
        return piece;
    }

    public void putPiece(int index, byte[] piece) {
        this.numPiecesDownloaded = this.bitfield.cardinality();
        if (this.numPiecesDownloaded == this.numPiecesInFile) {
            this.peerLogger.completeDownload();
        }
        int offset = index * this.commonConfigHelper.getPieceSize();
        System.arraycopy(piece, 0, this.data, offset, piece.length);
    }

    public synchronized boolean areAllPeersComplete() {
        for (BitSet bitfield : this.mapOfPeerBitfields.values()) {
            if (bitfield.cardinality() != this.numPiecesInFile) {
                return false;
            }
        }
        return true;
    }

    // Getters /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Peer getPeer(int peerId) {
        return this.mapOfPeers.get(peerId);
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

    public int getNumPiecesInFile() {
        return this.numPiecesInFile;
    }

    public HashMap<Integer, PeerWorker> getMapOfWorkers() {
        return this.mapOfWorkers;
    }

    public PeerWorker getWorker(int neighborPeerId) {
        return this.mapOfWorkers.get(neighborPeerId);
    }

    // Get the port number of a specified peer
    public int getPort(int peerId) {
        return this.mapOfPeers.get(peerId).getPort();
    }

    public int getNumPiecesDownloaded() {
        return this.numPiecesDownloaded;
    }

    public int getNumPreferredNeighbors() {
        return this.commonConfigHelper.getNumPreferredNeighbors();
    }

    public int getUnchokingInterval() {
        return this.commonConfigHelper.getUnchokingInterval();
    }

    public int getOptimisticallyUnchokedInterval() {
        return this.commonConfigHelper.getOptimisticUnchokingInterval();
    }

    public PeerLogger getPeerLogger() {
        return this.peerLogger;
    }

    /// Setters ////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Adds workers to hashmap as they are created
    public void addWorkerToMap(int neighborPeerId, PeerWorker peerWorker) {
        this.mapOfWorkers.put(neighborPeerId, peerWorker);
    }

    // Adds sockets to hashmap as they are created
    public void addSocketToMap(int neighborPeerId, Socket socket) {
        this.mapOfSockets.put(neighborPeerId, socket);
    }

    // Set PeerLogger
    public void setPeerLogger(PeerLogger peerLogger) {
        this.peerLogger = peerLogger;
    }

    public void addToInterested(int neighborPeerId) {
        this.interestedPeers.put(neighborPeerId, mapOfWorkers.get(neighborPeerId));
    }

    public void removeFromInterested(int neighborPeerId) {
        if (!this.interestedPeers.isEmpty()) {
            this.interestedPeers.remove(neighborPeerId);
        }
    }

    public void addToUnchoked(int neighborPeerId) {
        this.unchokedPeers.put(neighborPeerId, mapOfWorkers.get(neighborPeerId));
    }

    public HashMap<Integer, PeerWorker> getUnchokedPeers() {
        return this.unchokedPeers;
    }

    public HashMap getInterestedWorkers() {
        return this.interestedPeers;
    }

    public void sortDownloadRates() {
        Iterator iter = mapOfPeers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry peerEntry = (Map.Entry)iter.next();
            mapOfDownloadRates.put((int)peerEntry.getKey(), ((PeerWorker)peerEntry.getValue()).getDownloadRate());
        }

        List<Map.Entry<Integer, Double>> list = new LinkedList<>(mapOfDownloadRates.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        LinkedHashMap<Integer, Double> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        this.mapOfDownloadRates = sortedHashMap;
    }

    public HashMap<Integer, Double> getMapOfDownloadRates() {
        sortDownloadRates();
        return this.mapOfDownloadRates;
    }

    public void setUnchokedPeers(HashMap<Integer, PeerWorker> unchokedPeers) {
        this.unchokedPeers = unchokedPeers;
    }
}
