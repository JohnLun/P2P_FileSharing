import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class PeerProcess {
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                int peerId = Integer.valueOf(args[0]);
                PeerManager peerManager = new PeerManager(peerId);
                peerManager.run();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("The program has successfully compiled!");
            int tmp = 1001;
            //byte[] a = Integer.toString(tmp).getBytes(StandardCharsets.UTF_8);
            //byte[] a = ByteBuffer.allocate(4).putInt(tmp).array();
            byte[] a = new byte[] {(byte)55, (byte)13, (byte)10, (byte)49};
            ByteBuffer buffer = ByteBuffer.wrap(a);
            System.out.println(a.length);
            int pieceIndex = buffer.getInt();
            System.out.println(pieceIndex);
        }
    }
}