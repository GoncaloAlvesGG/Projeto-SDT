import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Set;

public class LeaderNode extends Node implements LeaderInterface {
    private final Set<String> receivedAcks = new HashSet<>();

    public LeaderNode() throws RemoteException {
        super(true); // Define isLeader como true
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
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, new String[]{"Doc1 v2", "Doc2 v2"}, this);
        transmitter.start();
    }

    @Override
    public void sendAck(String uuid) throws RemoteException {
        System.out.println("ACK recebido via RMI: " + uuid);
        receivedAcks.add(uuid);

        // Verifica se a maioria enviou ACK
        if (receivedAcks.size() >= 2) { // Supondo maioria de 2 em um grupo de 3 membros
            sendCommit();
            receivedAcks.clear();
        }
    }

    private void sendCommit() {
        System.out.println("Enviando commit para todos os membros...");
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, new String[]{"COMMIT"}, this);
        transmitter.start();
    }

}
