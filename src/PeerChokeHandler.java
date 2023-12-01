import java.util.*;

public class PeerChokeHandler implements Runnable{
    private Vitals vitals;
    private PeerLogger peerLogger;
    public PeerChokeHandler (Vitals vitals) {
        this.vitals = vitals;
        this.peerLogger = this.vitals.getPeerLogger();
    }

    public void run() {
        if (this.checkIfAllConnected()) {
            this.runPeerChokeHandler();
        }
    }

    private void runPeerChokeHandler() {
        // Get current interested list
        HashSet<Integer> interestedNeighbors = (HashSet<Integer>) this.vitals.getSetOfInterestedPeers().clone();
        HashSet<Integer> newPreferredNeighbors = new HashSet<>();
        int numPreferredNeighbors = Math.min(this.vitals.getNumPreferredNeighbors(), interestedNeighbors.size());

        if (interestedNeighbors == null || interestedNeighbors.isEmpty()) {
            return;
        }

        // If this peer has the entire file
        if (this.vitals.getBitSet().cardinality() == this.vitals.getNumPiecesInFile()) {
            // Loop through the determined number of preferred neighbors
            for (int i = 0; i < numPreferredNeighbors; i++) {
                int randomInterestedNeighbor = this.getRandomValueFromSet(interestedNeighbors);

                // Remove the randomly chosen neighbor from interestedNeighbors so that it does not get chosen again
                interestedNeighbors.remove(randomInterestedNeighbor);
                newPreferredNeighbors.add(randomInterestedNeighbor);

                if (this.vitals.getWorker(randomInterestedNeighbor).isNeighborChoked()) {
                    this.vitals.getWorker(randomInterestedNeighbor).sendUnchokeMessage();
                }
            }
        }
        // If this peer does not have the entire file, choose preferred neighbors based on download rates
        else {
            LinkedHashMap<Integer, Double> mapOfDownloadRates = this.vitals.getMapOfDownloadRates();
            Iterator<Map.Entry<Integer, Double>> iterator = mapOfDownloadRates.entrySet().iterator();
            int counter = 0;
            while (counter < numPreferredNeighbors && iterator.hasNext()) {
                Map.Entry<Integer, Double> mapEntry = iterator.next();

                // If the neighbor is not interested, continue
                if (!interestedNeighbors.contains(mapEntry.getKey())) {
                    continue;
                }

                // Remove the chosen neighbor from interestedNeighbors so that it does not get chosen again (will never happen but just for peace of mind)
                interestedNeighbors.remove(mapEntry.getKey());
                newPreferredNeighbors.add(mapEntry.getKey());
                if (this.vitals.getWorker(mapEntry.getKey()).isNeighborChoked()) {
                    this.vitals.getWorker(mapEntry.getKey()).sendUnchokeMessage();
                }
                counter += 1;
            }
        }
        // Reset the map of download rates since it is a new cycle
        this.vitals.resetDownloadRates();

        // Send choke messages to all neighbors who have not been newly preferred.
        this.sendChokeMessages(newPreferredNeighbors);

        this.setPreferredNeighborsInVitals(newPreferredNeighbors);

        this.peerLogger.changePreferredNeighbors();
    }

    private void sendChokeMessages(HashSet<Integer> newPreferredNeighbors) {
        // Choke every neighbor that isn't in our newPreferredNeighbors set
        for (Peer peer : this.vitals.getListOfPeers()) {
            // Ignore this peer and neighbors that are new preferred neighbors
            if (peer.getPeerId() == this.vitals.getThisPeerId() || newPreferredNeighbors.contains(peer.getPeerId())) {
                continue;
            }

            // If the neighbor is not choked, send a choke message
            if (!this.vitals.getWorker(peer.getPeerId()).isNeighborChoked()) {
                this.vitals.getWorker(peer.getPeerId()).sendChokeMessage();
            }
        }
    }

    private int getRandomValueFromSet(HashSet<Integer> set) {
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

    private void setPreferredNeighborsInVitals(HashSet<Integer> newPreferredNeighbors) {
        Vector<Peer> vectorOfPreferredNeighbors = new Vector<>();
        for (Peer peer : this.vitals.getListOfPeers()) {
            // If the peer id is in newPreferredNeighbors, add it to the vector
            if (peer.getPeerId() != this.vitals.getThisPeerId() && newPreferredNeighbors.contains(peer.getPeerId())) {
                vectorOfPreferredNeighbors.add(peer);
            }
        }

        this.vitals.setPreferredNeighbors(vectorOfPreferredNeighbors);
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

    private void printMapOfDownloadRates(LinkedHashMap<Integer, Double> mapOfDownloadedRates) {
        for (Map.Entry<Integer, Double> downloadRate : mapOfDownloadedRates.entrySet()) {
            System.out.println("Id:" + downloadRate.getKey() + "\tDownload Rate: " + downloadRate.getValue());
        }
    }
}
