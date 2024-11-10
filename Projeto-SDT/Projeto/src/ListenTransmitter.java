import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ListenTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final Node node;

    public ListenTransmitter(String multicastAddress, int port, Node node) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.node = node;
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
                if (!receivedMessage.equals("COMMIT")) {
                    String uuid = java.util.UUID.randomUUID().toString();

                    try {
                        LeaderInterface leader = (LeaderInterface) Naming.lookup("rmi://localhost/Leader");
                        leader.sendAck(uuid);
                        System.out.println("ACK enviado via RMI: " + uuid);
                    } catch (NotBoundException | RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Commit recebido: Atualizando versão do documento.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
