

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class LoggerHelper {
    private int peerId;
    private DateTimeFormatter dtf;

    public LoggerHelper(int peerId) {
        this.peerId = peerId;
        this.dtf = DateTimeFormatter.ofPattern("yyyy/mm/dd HH:mm:ss");
    }

    public String makeTcpConnectionLog(int neighborPeerId) {
        // Get the current time
        String now = dtf.format(LocalDateTime.now());

        // Create String to be logged
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] makes a connection to Peer [" + neighborPeerId + "].";
        return returnVal;
    }

    public String receiveTcpConnectionLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] is connected from Peer [" + neighborPeerId + "].";
        return returnVal;
    }

    // TODO: data structure for preferred neighbors may be changed
    public String changePreferredNeighborsLog(Vector<Peer> preferredNeighbors) {
        String now = dtf.format(LocalDateTime.now());
        String commaSeparatedList = preferredNeighbors.toString();

        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] has the preferred neighbors " + commaSeparatedList + ".";
        return returnVal;
    }

    public String changeOptimisticallyUnchokedNeighborLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] has the optimistically unchoked neighbor [" + neighborPeerId + "].";
        return returnVal;
    }

    public String getUnchokedLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] is unchoked by  [" + neighborPeerId + "].";
        return returnVal;
    }

    public String getChokedLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] is choked by  [" + neighborPeerId + "].";
        return returnVal;
    }

    public String receiveHaveMsgLog(int neighborPeerId, int pieceIndex) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] received the 'have' message from" +
                " [" + neighborPeerId + "] for the piece " + pieceIndex + ".";
        return returnVal;
    }

    public String receiveInterestedMsgLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] received the 'interested' message from" +
                " [" + neighborPeerId + "].";
        return returnVal;
    }

    public String receiveNotInterestedMsgLog(int neighborPeerId) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] received the 'not interested' message from" +
                " [" + neighborPeerId + "].";
        return returnVal;
    }

    public String downloadPieceLog(int neighborPeerId, int pieceIndex, int numPiecesInPossession) {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] has downloaded the piece " + pieceIndex + " from" +
                " [" + neighborPeerId + "]. Now the number of pieces it has is " + numPiecesInPossession + ".";
        return returnVal;
    }

    public String completionOfDownloadLog() {
        String now = dtf.format(LocalDateTime.now());
        String returnVal = "[" + now + "]: Peer [" + this.peerId + "] has downloaded the complete file.";
        return returnVal;
    }
}
