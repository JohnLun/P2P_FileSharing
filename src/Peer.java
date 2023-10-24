

import java.util.Vector;

public class Peer {
    private int peerId;
    private String hostName;
    private int port;
    private boolean hasFile;


    public Peer(int peerId, String hostName, int port, boolean hasEntireFile) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasEntireFile;
    }

    public int getPeerId() {
        return this.peerId;
    }
    public String getHostName() {
        return this.hostName;
    }
    public int getPort() {
        return this.port;
    }
    public boolean hasEntireFile() {
        return this.hasFile;
    }
}
