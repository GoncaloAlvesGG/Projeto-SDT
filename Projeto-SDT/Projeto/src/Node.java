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
/*
    // Ouve mensagens de heartbeat se for um membro
    private void listenForHeartbeats() {
        Thread receiveThread = new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(8888)) {
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group); // Junta-se ao grupo multicast

                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Mensagem recebida: " + receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }
}
*/