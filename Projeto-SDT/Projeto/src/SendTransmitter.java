import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SendTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final String[] messages;
    private Node node;

    public SendTransmitter(String multicastAddress, int port, String[] messages, Node node) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.messages = messages;
    }

    public void run() {
        int messageIndex = 0;
        while (true) {
            sendMessage(messages[messageIndex]);
            messageIndex = (messageIndex + 1) % messages.length;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
            System.out.println("Mensagem enviada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
