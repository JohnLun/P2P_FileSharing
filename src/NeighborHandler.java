import java.util.*;

public class NeighborHandler implements Runnable{
    private Vitals vitals;

    private Vector<Peer> preferredNeighbors;

    private int peerId;

    private int neighborId;

    private int unchokeInterval;
    private int optimisticallyUnchokeInterval;
    private OptimisticallyUnchokedNeighborHandler optimisticallyUnchokedNeighborHandler;
    private HashMap<Integer, PeerWorker> mapOfWorkers;

    public NeighborHandler(Vitals vitals, OptimisticallyUnchokedNeighborHandler optimisticallyUnchokedNeighborHandler) {
        this.vitals = vitals;
        this.unchokeInterval = this.vitals.getUnchokingInterval();
        this.optimisticallyUnchokeInterval = this.vitals.getOptimisticallyUnchokedInterval();
        this.peerId = this.vitals.getThisPeerId();
        this.preferredNeighbors = this.vitals.getPreferredNeighbors();
        this.mapOfWorkers = this.vitals.getMapOfWorkers();
        this.optimisticallyUnchokedNeighborHandler = optimisticallyUnchokedNeighborHandler;
    }

    public void run() {

    }

    //TODO: Logging for preferred neighbors
    public void initNeighborHandler() {
       try {
           //empty
           HashMap<Integer, PeerWorker> unchokedPeers = this.vitals.getUnchokedPeers();
           Vector<PeerWorker> newNeighbors = new Vector<PeerWorker>();
           HashMap<Integer, PeerWorker> interestedPeers = this.vitals.getInterestedWorkers();
           if (!this.preferredNeighbors.isEmpty()) {
               int iter = Math.min(this.vitals.getNumPreferredNeighbors(), interestedPeers.size());
               if (checkIfCompletedFile()) {
                   for (int i = 0; i < iter; i++) {
                       Random random = new Random();
                       PeerWorker nextPeer = interestedPeers.get(random.nextInt(interestedPeers.size()));
                       while (newNeighbors.equals(nextPeer)) {
                           random = new Random();
                           nextPeer = interestedPeers.get(random.nextInt(interestedPeers.size()));
                       }
                       if (!unchokedPeers.containsKey(nextPeer.getPeerId())) {
                           nextPeer.sendUnchokeMessage();
                       } else {
                           unchokedPeers.remove(nextPeer.getPeerId());
                       }

                       newNeighbors.add(nextPeer);

                       nextPeer.setDownloadRate(0.0);
                   }
               } else {
                    LinkedHashMap<Integer, Double> mapOfDownloadRates = this.vitals.getMapOfDownloadRates();
                    Iterator<Map.Entry<Integer, Double>> iterator = mapOfDownloadRates.entrySet().iterator();
                    int counter = 0;
                    while (counter < iter && iterator.hasNext()) {
                        Map.Entry<Integer, Double> ent = iterator.next();
                        PeerWorker nextPeer = this.vitals.getWorker(ent.getKey());
                        if (interestedPeers.containsKey(ent.getKey())) {
                            if (!unchokedPeers.containsKey(ent.getKey()) || nextPeer.getPeerId() != this.optimisticallyUnchokedNeighborHandler.getOptUnchokedId()) {
                                nextPeer.sendUnchokeMessage();
                            }
                        } else {
                            this.vitals.getUnchokedPeers().remove(ent.getKey());
                        }
                        newNeighbors.add(ent.getKey(), this.vitals.getWorker(ent.getKey()));
                        nextPeer.setDownloadRate(0.0);
                        counter++;
                    }
               }
               this.vitals.getUnchokedPeers().clear();
               for(int i = 0; i < newNeighbors.size(); i++) {
                   this.vitals.addToUnchoked(newNeighbors.get(i).getPeerId());
               }
               for (Map.Entry<Integer, PeerWorker> worker : this.mapOfWorkers.entrySet()) {
                   if (!newNeighbors.contains(worker.getValue()) && worker.getValue().getPeerId() != this.optimisticallyUnchokedNeighborHandler.getOptUnchokedId()) {
                       worker.getValue().sendChokeMessage();
                   }
               }

           } else {

           }


       } catch (Exception e) {
            e.printStackTrace();
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
