

public class ActualMessage {
    private int messageLength;
    private byte messageType;
    private byte[] messagePayload;

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte[] getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(byte[] messagePayload) {
        this.messagePayload = messagePayload;
    }

    public ActualMessage(byte messageType, byte[] messagePayload) {
        if (messagePayload == null) {
            messagePayload = new byte[0];
        }
        this.messageLength = messagePayload.length + 1;
        this.messageType = messageType;
        this.messagePayload = messagePayload;
    }

    // Constructor for an actual message read through a socket. This message will have the message type and payload as a byte array
    public ActualMessage(byte[] message) {
        this.messageLength = message.length - 1;
        this.messageType = message[0];
        this.messagePayload = new byte[this.messageLength];
        System.arraycopy(message, 1, this.messagePayload, 0, this.messageLength);
    }


}
