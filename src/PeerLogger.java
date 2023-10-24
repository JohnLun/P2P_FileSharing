

import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class PeerLogger {
    private Vitals vitals;
    private Logger logger;

    private FileHandler fileHandler;

    private LoggerHelper loggerHelper;

    public PeerLogger(Vitals vitals) {
        this.vitals = vitals;
        loggerHelper = new LoggerHelper(vitals.getPeer().getPeerId());
        logger = Logger.getLogger("log_peer_" + vitals.getPeer().getPeerId());
        try {
            fileHandler = new FileHandler("log_peer_" + vitals.getPeer().getPeerId() + ".log");
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toTcpConnection(int neighborId) {
        logger.info(loggerHelper.makeTcpConnectionLog(neighborId));
    }

    public void fromTcpConnection(int neighborId) {
        logger.info(loggerHelper.receiveTcpConnectionLog(neighborId));
    }

    public void changePreferredNeighbors() {
        logger.info(loggerHelper.changePreferredNeighborsLog(vitals.getPreferredNeighbors()));
    }

    public void changeOptUnchokedNeighbor(int neighborId) {
        logger.info(loggerHelper.changeOptimisticallyUnchokedNeighborLog(neighborId));
    }

    public void unchoke(int neighborId) {
        logger.info(loggerHelper.getUnchokedLog(neighborId));
    }

    public void choke(int neighborId) {
        logger.info(loggerHelper.getChokedLog(neighborId));
    }

    public void receiveHave(int neighborId, int pieceIndex) {
        logger.info(loggerHelper.receiveHaveMsgLog(neighborId, pieceIndex));
    }

    public void receiveInterested(int neighborId) {
        logger.info(loggerHelper.receiveInterestedMsgLog(neighborId));
    }

    public void receiveNotInterested(int neighborId) {
        logger.info(loggerHelper.receiveNotInterestedMsgLog(neighborId));
    }

    public void downloadPiece(int neighborId, int pieceIndex, int numPieces) {
        logger.info(loggerHelper.downloadPieceLog(neighborId, pieceIndex, numPieces));
    }

    public void completeDownload() {
        logger.info(loggerHelper.completionOfDownloadLog());
    }
}
