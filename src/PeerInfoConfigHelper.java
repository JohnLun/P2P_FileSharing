

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PeerInfoConfigHelper {
    HashMap<Integer, Peer> mapOfPeers;
    Vector<Peer> listOfPeers;
    private String filePath;

    public PeerInfoConfigHelper(String filePath) {
        this.filePath = filePath;
        this.mapOfPeers = new HashMap<>();
        this.listOfPeers = new Vector<>();
        this.processFile();
    }

    private void processFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.filePath));
            String line = reader.readLine();

            // Loop through each line in file
            while (line != null) {
                String[] splitLine = line.split(" ");
                int peerId = Integer.parseInt(splitLine[0]);
                String hostName = splitLine[1];
                int port = Integer.parseInt(splitLine[2]);
                int tmpBool = Integer.parseInt(splitLine[3]);
                boolean hasFile = (tmpBool == 1);
                Peer newPeer = new Peer(peerId, hostName, port, hasFile);
                mapOfPeers.put(peerId, newPeer);
                listOfPeers.add(newPeer);

                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Peer> getMapOfPeers() {
        return this.mapOfPeers;
    }

    public Vector<Peer> getListOfPeers() {
        return this.listOfPeers;
    }

    public int getPeerSize() {
        return this.listOfPeers.size();
    }
}
