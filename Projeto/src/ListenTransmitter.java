import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class ListenTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final Element node;
    private FileManager fileManager;
    private File tempFile;
    private Queue<Message> mensagensPendentes = new LinkedList<>();
    private boolean sincronizacaoInicialCompleta = false;

    public ListenTransmitter(String multicastAddress, int port, Element node) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.fileManager =  new FileManager();
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

                Message receivedMessage = deserializeMessage(packet.getData(), packet.getLength());
                System.out.println("Mensagem recebida: " + receivedMessage.getType());

                if(receivedMessage.getType().equals("FILE")) {
                    if (sincronizacaoInicialCompleta) {
                        fileManager.saveFile(receivedMessage.getFile());
                    } else {
                        mensagensPendentes.offer(receivedMessage); // Armazena mensagens na fila
                    }
                    tempFile = receivedMessage.getFile();
                    String uuid = java.util.UUID.randomUUID().toString();
                    try {
                        LeaderInterface leader = (LeaderInterface) Naming.lookup("rmi://localhost/Leader");
                        leader.sendAck(uuid);
                        System.out.println("ACK enviado via RMI: " + uuid);
                    } catch (NotBoundException | RemoteException e) {
                        e.printStackTrace();
                    }

                }
                if (receivedMessage.getType().equals("COMMIT")) {
                    System.out.println("Commit recebido: A atualizar versão do documento.");
                    fileManager.saveFile(tempFile);
                }
                if (receivedMessage.getType().equals("SETUP")) { // Nova condição
                    // Inicia a sincronização inicial
                    sincronizacaoInicialCompleta = false;
                    mensagensPendentes.clear(); // Limpa a fila

                    try {
                        LeaderInterface leader = (LeaderInterface) Naming.lookup("rmi://localhost/Leader");
                        List<File> listaDocumentos = leader.getFileList(); // Obtém lista do líder

                        // Itera sobre a lista de documentos
                        for (File remoteFile : listaDocumentos) {
                            File localFile = fileManager.getFile(remoteFile.getTitle());
                            if (localFile == null || remoteFile.getVersion() > localFile.getVersion()) {
                                System.out.println("Solicitando arquivo: " + remoteFile.getTitle());

                                try {
                                    // 1. Solicita o conteúdo do arquivo ao líder via RMI
                                    byte[] fileContent = leader.getFileContent(remoteFile.getTitle()); // Assumindo que LeaderInterface tenha este método

                                    // 2. Cria um novo objeto File com o conteúdo recebido
                                    File receivedFile = new File(remoteFile.getTitle(), remoteFile.getVersion(), fileContent); // Assumindo que File tenha este construtor

                                    // 3. Salva o arquivo usando o FileManager
                                    fileManager.saveFile(receivedFile);
                                } catch (RemoteException e) {
                                    // Trata a exceção de forma apropriada (registrar, tentar novamente, etc.)
                                    e.printStackTrace();
                                }
                            }
                        }

                        sincronizacaoInicialCompleta = true; // Sincronização inicial completa
                        processarMensagensPendentes(); // Processa mensagens pendentes
                    } catch (NotBoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processarMensagensPendentes() {
        while (!mensagensPendentes.isEmpty()) {
            Message message = mensagensPendentes.poll();
            if (message.getType().equals("FILE")) {
                fileManager.saveFile(message.getFile());
            }
        }
    }
    private Message deserializeMessage(byte[] data, int length) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data, 0, length);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (Message) objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
