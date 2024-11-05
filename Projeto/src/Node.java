import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Node {

    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    private static final int PORT = 8888; // Porta Multicast

    // Array com três mensagens de exemplo
    private static final String[] MESSAGES = {
            "HEARTBEAT - 1",
            "HEARTBEAT - 2",
            "HEARTBEAT - 3"
    };

    private final boolean isLeader;

    public Node(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public void start() {
        if (isLeader) {
            SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, MESSAGES);
            transmitter.start();
        } else {
            ListenTransmitter listener = new ListenTransmitter(MULTICAST_ADDRESS, PORT);
            listener.start();
        }
    }


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
