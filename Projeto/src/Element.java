import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class Element {

    protected static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    protected static final int PORT = 8888; // Porta Multicast
    protected static final long ELECTION_TIMEOUT = 1500; // Timeout base para eleições (ms)

    private final String uuid = UUID.randomUUID().toString();
    public String getUUID() {
        return this.uuid; // Certifique-se de que a variável `uuid` existe e está inicializada
    }

    private String currentLeader = null;
    private int currentTerm = 0;
    private boolean votedInTerm = false;

    private Thread listenerThread;

    public int getCurrentTerm() {
        return currentTerm;
    }

    public Element() {
        System.out.println("Elemento iniciado com UUID: " + uuid);
    }

    public void start() {
        // Inicia o listener para mensagens multicast
        listenerThread = new Thread(this::listenToMessages);
        listenerThread.start();
    }

    private void listenToMessages() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Message message = deserializeMessage(packet.getData());
                if (message != null) {
                    processMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case "HEARTBEAT":
                handleHeartbeat(message);
                break;
            case "REQUEST_VOTE":
                handleVoteRequest(message);
                break;
            case "VOTE":
                handleVote(message);
                break;
            default:
                System.out.println("Mensagem desconhecida recebida: " + message.getType());
        }
    }



    private void handleHeartbeat(Message message) {
        currentLeader = message.getSenderId();
        currentTerm = message.getTerm();
        System.out.println("Heartbeat recebido do líder " + currentLeader + " no termo " + currentTerm);
    }

    private void handleVoteRequest(Message message) {
        if (message.getTerm() > currentTerm) {
            currentTerm = message.getTerm();
            votedInTerm = false;
        }

        if (!votedInTerm && message.getTerm() == currentTerm) {
            votedInTerm = true;
            sendVote(message.getSenderId());
            System.out.println("Voto concedido para " + message.getSenderId() + " no termo " + currentTerm);
        }
    }

    private void handleVote(Message message) {
        System.out.println("Voto recebido de " + message.getSenderId() + " para o termo " + message.getTerm());
    }

    private void sendVote(String candidateId) {
        sendMessage(new Message("VOTE", uuid, currentTerm));
    }

    protected void sendMessage(Message message) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            byte[] buffer = serializeMessage(message);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] serializeMessage(Message message) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(message);
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private Message deserializeMessage(byte[] data) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (Message) objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
