import java.util.*;

public class NeighborHandler implements Runnable{

    private Vitals vitals;

    private Vector<Peer> preferredNeighbors;
    private OptimisticallyUnchokedNeighborHandler optimisticallyUnchokedNeighborHandler;
    private HashMap<Integer, PeerWorker> mapOfWorkers;
    private PeerLogger peerLogger;

    public NeighborHandler(Vitals vitals, OptimisticallyUnchokedNeighborHandler optimisticallyUnchokedNeighborHandler) {
        this.vitals = vitals;
        this.preferredNeighbors = new Vector<>();
        this.mapOfWorkers = this.vitals.getMapOfWorkers();
        this.optimisticallyUnchokedNeighborHandler = optimisticallyUnchokedNeighborHandler;
        this.peerLogger = vitals.getPeerLogger();
    }

    public void run() {
        this.initNeighborHandler();
    }

    //TODO: Logging for preferred neighbors
    public void initNeighborHandler() {
       try {
           //empty
           HashMap<Integer, PeerWorker> unchokedPeers = this.vitals.getUnchokedPeers();
           Vector<PeerWorker> newNeighbors = new Vector<PeerWorker>();
           HashMap<Integer, PeerWorker> interestedPeers = this.vitals.getInterestedWorkers();
           if (!interestedPeers.isEmpty() && checkIfAllConnected()) {
               int iter = Math.min(this.vitals.getNumPreferredNeighbors(), interestedPeers.size());
               if (checkIfCompletedFile()) {
                   for (int i = 0; i < iter; i++) {
                       Random random = new Random();
                       PeerWorker nextPeer = interestedPeers.get(random.nextInt(iter)+1001);
                       while (this.vitals.getThisPeerId() != nextPeer.getPeerId()) {
                           random = new Random();
                           nextPeer = interestedPeers.get(random.nextInt(interestedPeers.size()));
                       }
                       if (!unchokedPeers.containsKey(nextPeer.getPeerId()) && nextPeer.getPeerId() != this.optimisticallyUnchokedNeighborHandler.getOptUnchokedId()) {
                           nextPeer.sendUnchokeMessage();
                       } else {
                           unchokedPeers.remove(nextPeer.getPeerId());
                       }

                       newNeighbors.add(nextPeer);
                       interestedPeers.remove(nextPeer.getPeerId());

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
                           if (!unchokedPeers.containsKey(ent.getKey()) && nextPeer.getPeerId() != this.optimisticallyUnchokedNeighborHandler.getOptUnchokedId()) {
                               nextPeer.sendUnchokeMessage();
                           }
                       } else {
                           this.vitals.getUnchokedPeers().remove(ent.getKey());
                       }
                       newNeighbors.add(this.vitals.getWorker(ent.getKey()));
                       nextPeer.setDownloadRate(0.0);
                       counter++;
                   }
               }
               this.vitals.getUnchokedPeers().clear();
               for (int i = 0; i < newNeighbors.size(); i++) {
                   this.vitals.addToUnchoked(newNeighbors.get(i).getPeerId());
               }
               for (Map.Entry<Integer, PeerWorker> worker : this.mapOfWorkers.entrySet()) {
                   if (!newNeighbors.contains(worker.getValue()) && worker.getValue().getPeerId() != this.optimisticallyUnchokedNeighborHandler.getOptUnchokedId()) {
                       worker.getValue().sendChokeMessage();
                   }
               }
           }
           convertVectorOfPeers(newNeighbors);
           this.vitals.setPreferredNeighbors(this.preferredNeighbors);
           if (newNeighbors.size() > 0) {
               this.peerLogger.changePreferredNeighbors();
           }
       } catch (Exception e) {
            e.printStackTrace();
       }
    }

    public double calculateDownloadingRate() {
        return 0.0;
    }

    public boolean checkIfAllConnected() {
        Vector<Peer> listOfPeers = this.vitals.getListOfPeers();
        for (Peer peer : listOfPeers) {
            if (this.vitals.getThisPeer().getPeerId() != peer.getPeerId()) {
                if (!this.mapOfWorkers.containsKey(peer.getPeerId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public void convertVectorOfPeers(Vector<PeerWorker> newNeighbors) {
        this.preferredNeighbors.clear();
        for (PeerWorker neighbor : newNeighbors) {
            this.preferredNeighbors.add(this.vitals.getPeer(neighbor.getPeerId()));
        }
    }


    public boolean checkIfCompletedFile() {
        if (this.vitals.getBitSet().cardinality() != this.vitals.getNumPiecesInFile()) {
            return false;
        }
        return true;
//        for (int i = 0; i < vitals.getBitSet().length(); i++) {
//            if (!vitals.getBitSet().get(i)) {
//                return false;
//            }
//        }
//        return true;
    }
}
