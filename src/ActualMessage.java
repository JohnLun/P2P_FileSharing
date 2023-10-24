

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
        this.messageLength = this.messagePayload.length + 1;
        this.messageType = messageType;
        this.messagePayload = messagePayload;
    }

    public ActualMessage(byte messageType) {
        this.messageLength = 1;
        this.messageType = messageType;
        this.messagePayload = new byte[0];
    }

    public ActualMessage() {
        this.messageLength = 1;
        this.messageType = 0;
        this.messagePayload = new byte[0];
    }

    // TODO: Note, reading and writing actual messages may need to be in another class, since we need to read an actual message before creating an actual message object (delete this after it is resolved)
    public byte[] writeActualMessage() {
        //TODO: Implement writeActualMessage()
        return messagePayload;
    }

    public void readActualMessage() {
        //TODO: Implement readActualMessage()
    }

    public int retrieveMessageLength() {
        //TODO: implement method to retrieve message length from payload
        return 0;
    }

    public byte retrieveMessageType() {
        //TODO: implement method to retrieve message type from payload
        return messageType;
    }


}
