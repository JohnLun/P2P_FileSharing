

import java.util.Vector;

public class Peer {
    private int peerId;
    private String hostName;
    private int port;
    private boolean hasFile;

    private boolean isInterested;

    private boolean isChoked;


    public Peer(int peerId, String hostName, int port, boolean hasEntireFile) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasEntireFile;
        this.isInterested = false;
        this.isChoked = false;
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

    public boolean getInterested() {
        return this.isInterested;
    }

    public boolean getChoked() {
        return this.isChoked;
    }

    public void setInterested(boolean isInterested) {
        this.isInterested = isInterested;
    }

    public void setChoked(boolean isChoked) {
        this.isChoked = isChoked;
    }
}
