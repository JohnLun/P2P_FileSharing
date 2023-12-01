
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageCreator {

    // This function uses a HandshakeMessage object to create the byte array for a handshake message
    public static byte[] createHandshakeMessage(int peerId) {
        // Create handshake msg
        HandshakeMessage handshakeMessage = new HandshakeMessage(peerId);

        // Get header as bytes
        byte[] handshakeHeaderAsBytes = handshakeMessage.getHandshakeHeader().getBytes();

        // Create empty byte array that will be returned
        byte[] handshakeMessageAsBytes = new byte[handshakeMessage.getMessageLength()];

        // Add header to byte array
        System.arraycopy(handshakeHeaderAsBytes, 0, handshakeMessageAsBytes, 0,
                handshakeHeaderAsBytes.length);

        // Add 10-byte zero bits to byte array
        System.arraycopy(handshakeMessage.getZeroBits(), 0, handshakeMessageAsBytes, handshakeHeaderAsBytes.length, 10);

        // Add peerId to byte array
        //ByteBuffer b = ByteBuffer.allocate(4);
        //b.putInt(peerId);
        byte[] peerIdAsBytes = ByteBuffer.allocate(4).putInt(peerId).array();
        //byte[] peerIdAsBytes = Integer.toString(peerId).getBytes(StandardCharsets.UTF_8);
        System.arraycopy(peerIdAsBytes, 0, handshakeMessageAsBytes, handshakeHeaderAsBytes.length + 10, 4);

        return handshakeMessageAsBytes;
    }

    // This function uses an ActualMessage object to create the byte array for an actual message
    public static byte[] createActualMessage(byte messageType, byte[] messagePayload) {
        // Create ActualMessage object
        ActualMessage actualMessage = new ActualMessage(messageType, messagePayload);

        // Create empty byte array that will be returned
        byte[] actualMessageAsBytes = new byte[4 + actualMessage.getMessageLength()];

        // Add length field to byte array
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(actualMessage.getMessageLength());
        byte[] peerIdAsBytes = b.array();
        System.arraycopy(peerIdAsBytes, 0, actualMessageAsBytes, 0, 4);

        // Add message type to byte array
        actualMessageAsBytes[4] = messageType;

        //Add message payload if it exists
        if (messagePayload != null || messagePayload.length > 0) {
            System.arraycopy(messagePayload, 0, actualMessageAsBytes, 5, messagePayload.length);
        }

        return actualMessageAsBytes;
    }

}
