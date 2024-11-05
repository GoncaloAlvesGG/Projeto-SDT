import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;

    public ListenTransmitter(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            socket.joinGroup(group);

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
}
