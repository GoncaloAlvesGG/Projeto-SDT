import java.rmi.RemoteException;

public class Node {

    protected static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    protected static final int PORT = 8888; // Porta Multicast

    // Array com três mensagens de exemplo
    private static final String[] MESSAGES = {
            "HEARTBEAT - 1",
            "HEARTBEAT - 2",
            "HEARTBEAT - 3"
    };

    private final boolean isLeader;
    //private final Set<String> receiveAcks = new HashSet<>(); // Armazena UUIDs dos elementos que enviaram ACKs

    public Node(boolean isLeader) throws RemoteException {
        this.isLeader = isLeader;

    }


    public void start() {
        if (isLeader) {
            System.out.println("Este nó é o líder. Inicie classe LeaderNode.");
        } else {
            ListenTransmitter listener = new ListenTransmitter(MULTICAST_ADDRESS, PORT, this);
            listener.start();
        }
    }
}