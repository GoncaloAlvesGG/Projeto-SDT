import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Server extends Client implements LeaderInterface, Serializable {
    private final Set<String> receivedAcks = new HashSet<>();
    private final MessageList messageList = new MessageList();

    private List<File> arquivos; // Lista para armazenar os arquivos

    public Server() throws RemoteException {
        super( true); // Define isLeader como true
        UnicastRemoteObject.exportObject(this, 0);
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("Leader", this);
            System.out.println("Líder registrado no RMI com o nome 'Leader'");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializa a lista de arquivos
        this.arquivos = new ArrayList<>();
        this.arquivos.add(new File(1, "DOC 1", "oLÁ"));
        this.arquivos.add(new File(2, "DOC 1", "Adeus"));
        this.arquivos.add(new File(1, "DOC 3", "Viva"));
        this.arquivos.add(new File(2, "DOC 2", "Adeus"));
        this.arquivos.add(new File(3, "DOC 2", "Adeus"));
    }

    @Override
    public void start() {

        messageList.addMessage(new Message("FILE", new File(1,"DOC 1","oLÁ")));
        messageList.addMessage(new Message("FILE", new File(2,"DOC 1","Adeus")));
        messageList.addMessage(new Message("FILE", new File(1,"DOC 3","Viva")));
        messageList.addMessage(new Message("FILE", new File(2,"DOC 2","Adeus")));
        messageList.addMessage(new Message("FILE", new File(3,"DOC 2","Adeus")));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }

    @Override
    public void sendAck(String uuid) throws RemoteException {
        System.out.println("ACK recebido via RMI: " + uuid);
        receivedAcks.add(uuid);
        //TODO: DINAMICO
        if (receivedAcks.size() >= 2) {
            sendCommit();
            receivedAcks.clear();
        }
    }

    private void sendCommit() {
        System.out.println("Enviando commit para todos os membros...");
        MessageList messageList = new MessageList();
        messageList.addMessage(new Message("COMMIT"));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }
    @Override
    public List<File> getFileList() throws RemoteException {
        // Retorna a lista de arquivos
        return this.arquivos;
    }

    @Override
    public File getFile(String nomeArquivo) throws RemoteException {
        // Implemente a lógica para encontrar o arquivo pelo nome
        // e retorná-lo
        for (File arquivo : this.arquivos) {
            if (arquivo.getTitle().equals(nomeArquivo)) {
                return arquivo;
            }
        }
        return null; // Ou lance uma exceção se o arquivo não for encontrado
    }

    @Override
    public byte[] getFileContent(String fileName) throws RemoteException {
        return new byte[0];
    }

}
