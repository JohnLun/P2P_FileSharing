import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

public class PeerOptUnchokeHandler implements Runnable{
    private Vitals vitals;
    private PeerLogger peerLogger;

    public PeerOptUnchokeHandler(Vitals vitals) {
        this.vitals = vitals;
        this.peerLogger = this.vitals.getPeerLogger();
    }

    public void run() {
        if (this.checkIfAllConnected()) {
            this.runPeerOptUnchokeHandler();
        }
    }

    private void runPeerOptUnchokeHandler() {
        HashSet<Integer> interestedNeighbors = (HashSet<Integer>) this.vitals.getSetOfInterestedPeers().clone();
        int randomInterestedNeighbor = this.getRandomValueFromSet(interestedNeighbors);

        // While the randomly chosen neighbor is already unchoked, find a new random interested neighbor
        while (randomInterestedNeighbor != -1 && !this.vitals.getWorker(randomInterestedNeighbor).isNeighborChoked()) {
            // Remove neighbor from interested set so it is not found again
            interestedNeighbors.remove(randomInterestedNeighbor);
            randomInterestedNeighbor = this.getRandomValueFromSet(interestedNeighbors);
        }

        // If the random neighbor peer id is -1, there is no possible optimistically unchoked neighbor
        // If there is a valid opt unchoked neighbor, log it and send an unchoke message
        if (randomInterestedNeighbor != -1) {
            this.vitals.getWorker(randomInterestedNeighbor).sendUnchokeMessage();
        }
        this.vitals.setOptimisticallyUnchokedPeer(randomInterestedNeighbor);
    }

    // If the passed in set is empty, return -1
    private int getRandomValueFromSet(HashSet<Integer> set) {
        if (set.size() == 0) {
            return -1;
        }
        int randomIndex = new Random().nextInt(set.size());
        int i = 0;
        for (Integer element : set) {
            if (i == randomIndex) {
                return element;
            }
            i++;
        }
        throw new IllegalStateException("Something went wrong while picking a random element.");
    }

    public boolean checkIfAllConnected() {
        Vector<Peer> listOfPeers = this.vitals.getListOfPeers();
        for (Peer peer : listOfPeers) {
            if (this.vitals.getThisPeer().getPeerId() == peer.getPeerId()) {
                continue;
            }
            if (!this.vitals.getMapOfWorkers().containsKey(peer.getPeerId())) {
                return false;
            }
        }
        return true;
    }
}
