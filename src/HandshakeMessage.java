import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HandshakeMessage {
    private String handshakeHeader;

    private byte[] zeroBits;
    private int peerID;
    private final int MESSAGE_LENGTH = 32;
    private final int HEADER_LENGTH = 18;

    public HandshakeMessage(int peerID) {
        this.handshakeHeader = "P2PFILESHARINGPROJ";
        this.zeroBits = new byte[10];
        this.peerID = peerID;
    }

    public String getHandshakeHeader() {
        return handshakeHeader;
    }

    public void setHandshakeHeader(String handshakeHeader) {
        this.handshakeHeader = handshakeHeader;
    }

    public byte[] getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits = zeroBits;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }
    public int getMessageLength() {
        return this.MESSAGE_LENGTH;
    }

    public int getHeaderLength() {
        return this.HEADER_LENGTH;
    }

    public byte[] writeHandshakeMessage() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            result.write(this.handshakeHeader.getBytes(StandardCharsets.UTF_8));
            result.write(this.zeroBits);
            result.write(this.peerID);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result.toByteArray();
    }

    public void readHandshakeMessage(byte[] message) {
        String readMsg = new String(message, StandardCharsets.UTF_8);
        this.setHandshakeHeader(readMsg.substring(0, 18));
        this.setZeroBits(readMsg.substring(18, 28).getBytes());
        this.setPeerID(Integer.parseInt(readMsg.substring(28,32)));
    }
}
