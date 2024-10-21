import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Main {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    private static final int PORT = 8888; // Porta Multicast
    private static final boolean IS_LEADER = true   ; // Define se este nó é o líder (estático)

    // Array com três mensagens de exemplo
    private static final String[] MESSAGES = {
            "HEARTBEAT - 1",
            "HEARTBEAT - 2",
            "HEARTBEAT - 3"
    };

    public static void main(String[] args) {
        // Se for o líder, envia heartbeat
        if (IS_LEADER) {
            Thread sendThread = new Thread(() -> sendHeartbeats());
            sendThread.start();
        } else {
            // Thread que ouvirá as mensagens multicast
            Thread receiveThread = new Thread(() -> listenForHeartbeats());
            receiveThread.start();
        }
    }

    // Thread para ouvir as mensagens de heartbeat
    private static void listenForHeartbeats() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
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
    }

    // Thread para enviar heartbeats (se for o líder)
    private static void sendHeartbeats() {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            while (true) {
                for (String message : MESSAGES) {
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(packet);
                    System.out.println("Mensagem enviada: " + message);

                    // Espera 5 segundos antes de enviar a próxima mensagem
                    Thread.sleep(5000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
