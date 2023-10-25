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
        }
    }
}