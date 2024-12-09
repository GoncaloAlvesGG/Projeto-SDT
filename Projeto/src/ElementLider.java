import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ElementLider extends Element implements LeaderInterface, Serializable {
    private final Set<String> elementos = new HashSet<>();
    private final MessageList messageList = new MessageList();
    private FileManager fileManager = new FileManager();
    private final Map<String, Long> lastResponseTime = new HashMap<>();
    private static final long TIMEOUT = 15000; // 15 seconds

    // Map to store ACKs received for each heartbeat ID
    private final Map<String, Set<String>> ackMap = new HashMap<>();

    public ElementLider() throws RemoteException {
        super(true); // Define isLeader como true
        UnicastRemoteObject.exportObject(this, 0);
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("Leader", this);
            System.out.println("Líder registrado no RMI com o nome 'Leader'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    // Cria um ID único para o heartbeat
                    String heartbeatId = UUID.randomUUID().toString();
                    Message heartbeatMessage = new Message("HEARTBEAT", heartbeatId);

                    // Envia o heartbeat por multicast
                    MessageList tempMessageList = new MessageList();
                    tempMessageList.addMessage(heartbeatMessage);
                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, tempMessageList);
                    transmitter.start();

                    System.out.println("Heartbeat enviado para os membros com ID: " + heartbeatId);

                    // Remove elementos que não responderam dentro do tempo aceitável
                    checkTimeouts();

                    // Aguarda 5 segundos antes de enviar o próximo heartbeat
                    Thread.sleep(5000); // Espera 5 segundos
                } catch (InterruptedException e) {
                    System.out.println("Erro ao enviar heartbeat: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void checkTimeouts() {
        long currentTime = System.currentTimeMillis();
        elementos.removeIf(uuid -> {
            Long lastResponse = lastResponseTime.get(uuid);
            if (lastResponse == null || (currentTime - lastResponse) > TIMEOUT) {
                System.out.println("Elemento " + uuid + " removido por timeout.");
                return true;
            }
            return false;
        });
    }

    @Override
    public void sendAck(String uuid, String messageType) throws RemoteException {
        System.out.println("ACK recebido via RMI: " + uuid + " para mensagem do tipo: " + messageType);
        lastResponseTime.put(uuid, System.currentTimeMillis());

        // Store the ACK UUID in the ackMap
        ackMap.computeIfAbsent(messageType, k -> new HashSet<>()).add(uuid);

        if (messageType.startsWith("HEARTBEAT")) {
            String heartbeatId = messageType.split(":")[1];
            ackMap.computeIfAbsent(heartbeatId, k -> new HashSet<>()).add(uuid);
            if (ackMap.get(heartbeatId).size() > elementos.size() / 2) {
                sendCommit();

            }
        }
    }

    public void sendSetup(String uuid) throws RemoteException {
        elementos.add(uuid);
        lastResponseTime.put(uuid, System.currentTimeMillis());
        System.out.println("A enviar ficheiros de setup para o elemento - " + uuid);
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }

    @Override
    public void uploadFile(File file) throws RemoteException {
        System.out.println("Ficheiro enviado pelo cliente: " + file);
        this.fileManager.saveFile(file);
        this.messageList.addMessage(new Message("FILE", file));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
        System.out.println("Ficheiro enviado por multicast para os membros.");
    }

    private void sendCommit() {
        System.out.println("Enviando commit para todos os membros...");
        MessageList messageList = new MessageList();
        messageList.addMessage(new Message("COMMIT"));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }

    // Method to get ACKs for a specific message type
    public Set<String> getAcksForMessageType(String messageType) {
        return ackMap.getOrDefault(messageType, Collections.emptySet());
    }
}