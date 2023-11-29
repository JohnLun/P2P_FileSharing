import java.util.*;

public class OptimisticallyUnchokedNeighborHandler implements Runnable{
    private Vitals vitals;
    private Random random;

    //TODO: Not sure if there needs to be more implemented to insure this is integrated with the rest of the code

    public void run() {

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
                PeerWorker selectedPeer = candidates.get(random.nextInt(candidates.size()));
                selectedPeer.sendUnchokeMessage();
                this.vitals.setOptimisticallyUnchokedPeer(selectedPeer.getPeerId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
