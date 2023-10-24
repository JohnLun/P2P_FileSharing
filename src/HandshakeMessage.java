

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
        //TODO: Write method to generate handshake message
        return getZeroBits();
    }

    public void readHandshakeMessage(byte[] message) {
        //TODO: Write method to read handshake message
    }
}
