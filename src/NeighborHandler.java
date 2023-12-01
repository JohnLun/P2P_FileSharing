import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
           HashMap<Integer, PeerWorker> newNeighbors = new HashMap<>();
           HashMap<Integer, PeerWorker> interestedPeers = this.vitals.getInterestedWorkers();
           Vector<Integer> interestedPeerIds = new Vector<>();
           for (Map.Entry<Integer, PeerWorker> entry : interestedPeers.entrySet()) {
               interestedPeerIds.add(entry.getKey());
           }
           if (!interestedPeers.isEmpty() && checkIfAllConnected()) {
               int iter = Math.min(this.vitals.getNumPreferredNeighbors(), interestedPeers.size());
               if (checkIfCompletedFile()) {
                   for (int i = 0; i < iter; i++) {
                       int randomIndex = ThreadLocalRandom.current().nextInt(0, iter);
                       PeerWorker nextPeer = this.vitals.getWorker(interestedPeerIds.get(randomIndex));
                       // TODO: while loop is useless
                       while (this.vitals.getThisPeerId() == nextPeer.getNeighborPeerId()) {
                           randomIndex = ThreadLocalRandom.current().nextInt(0, iter);
                           nextPeer = this.vitals.getWorker(interestedPeerIds.get(randomIndex));
                       }
                       if (!unchokedPeers.containsKey(nextPeer.getNeighborPeerId()) && nextPeer.getChoked()) {
                           nextPeer.sendUnchokeMessage();
                       }
                       newNeighbors.put(nextPeer.getNeighborPeerId(), nextPeer);
                       interestedPeers.remove(nextPeer.getNeighborPeerId());
                       interestedPeerIds.remove(randomIndex);
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
                           if (!unchokedPeers.containsKey(ent.getKey()) && nextPeer.getChoked()) {
                               nextPeer.sendUnchokeMessage();
                               PeerWorker tempWorker = this.vitals.getWorker(ent.getKey());
                               newNeighbors.put(tempWorker.getNeighborPeerId(), tempWorker);
                               counter++;
                           }
                       }


                       nextPeer.setDownloadRate(0.0);

                   }
               }
               this.vitals.getUnchokedPeers().clear();
               for(Map.Entry<Integer, PeerWorker> neighbor : newNeighbors.entrySet()) {
                   this.vitals.addToUnchoked(neighbor.getKey());
               }
               for (Map.Entry<Integer, PeerWorker> worker : this.mapOfWorkers.entrySet()) {
                   if (!newNeighbors.containsKey(worker.getValue().getNeighborPeerId())) {
                       if(!worker.getValue().getChoked()) {
                           worker.getValue().sendChokeMessage();
                       }

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

    public void convertVectorOfPeers(HashMap<Integer, PeerWorker> newNeighbors) {
        this.preferredNeighbors.clear();
        for (Map.Entry<Integer, PeerWorker> neighbor: newNeighbors.entrySet()) {
            this.preferredNeighbors.add(this.vitals.getPeer(neighbor.getKey()));
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
