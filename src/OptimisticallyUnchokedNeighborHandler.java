import java.util.*;

public class OptimisticallyUnchokedNeighborHandler implements Runnable{
    private Vitals vitals;
    private int optUnchokedId;
    private Random random;

    //TODO: Not sure if there needs to be more implemented to insure this is integrated with the rest of the code
    public OptimisticallyUnchokedNeighborHandler(Vitals vitals) {
        this.vitals = vitals;
    }
    public void run() {
        this.initOptimisticallyUnchokedNeighborHandler();
    }

    public void initOptimisticallyUnchokedNeighborHandler() {
        try {
            HashMap<Integer, PeerWorker> interestedPeers = this.vitals.getInterestedWorkers();
            HashMap<Integer, PeerWorker> unchokedPeers = this.vitals.getUnchokedPeers();
            List<PeerWorker> candidates = new ArrayList<>();

            // Filter for interested but choked peers
            for (Map.Entry<Integer, PeerWorker> entry : interestedPeers.entrySet()) {
                if (!unchokedPeers.containsKey(entry.getKey())) {
                    candidates.add(entry.getValue());
                }
            }

            // Randomly select one peer to be optimistically unchoked
            if (!candidates.isEmpty()) {
                this.random = new Random();
                PeerWorker selectedPeer = candidates.get(random.nextInt(candidates.size()));
                selectedPeer.sendUnchokeMessage();
                this.optUnchokedId = selectedPeer.getNeighborPeerId();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getOptUnchokedId() {
        return this.optUnchokedId;
    }
}
