import java.rmi.RemoteException;

public class Client {

    protected static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    protected static final int PORT = 8888; // Porta Multicast

    private final boolean isLeader;

    public Client(boolean isLeader) throws RemoteException {
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