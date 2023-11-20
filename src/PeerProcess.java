import java.util.BitSet;

public class PeerProcess {
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                int peerId = Integer.valueOf(args[0]);
                //PeerManager peerManager = new PeerManager(peerId);
                //peerManager.run();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("The program has successfully compiled!");
            BitSet tmp1 = new BitSet(4);
            BitSet tmp2 = new BitSet(4);
            tmp1.set(0,2);
            tmp2.set(1,3);
            System.out.println(tmp1);
            System.out.println(tmp2);
            tmp1.andNot(tmp2);
            System.out.println(tmp1);
        }
    }
}