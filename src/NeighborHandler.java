import java.util.HashMap;
import java.util.Vector;

public class NeighborHandler {
    private Vitals vitals;

    private Vector<Peer> preferredNeighbors;

    private int peerId;

    private int neighborId;

    private int unchokeInterval;
    private int optimisticallyUnchokeInterval;

    private HashMap<Integer, PeerWorker> mapOfWorkers;

    public NeighborHandler(Vitals vitals) {
        this.vitals = vitals;
        this.unchokeInterval = this.vitals.getUnchokingInterval();
        this.optimisticallyUnchokeInterval = this.vitals.getOptimisticallyUnchokedInterval();
        this.peerId = this.vitals.getThisPeerId();
        this.preferredNeighbors = this.vitals.getPreferredNeighbors();
        this.mapOfWorkers = this.vitals.getMapOfWorkers();
    }

    public void initNeighborHandler() {
        for(int i = 0; i < preferredNeighbors.size(); i++) {
            mapOfWorkers.get(preferredNeighbors.get(i).getPeerId()).setChoked(false);
        }
        while (!checkIfCompletedFile()) {
            try {
                Thread.sleep(unchokeInterval*1000);

            } catch(Exception e) {

            }

        }
    }

    public double calculateDownloadingRate() {
        return 0.0;
    }



    public boolean checkIfCompletedFile() {
        for (int i = 0; i < vitals.getBitSet().length(); i++) {
            if (!vitals.getBitSet().get(i)) {
                return false;
            }
        }
        return true;
    }
}
