import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LeaderInterface extends Remote {
    void sendAck(String uuid) throws RemoteException;

    List<File> getFileList() throws RemoteException;

    File getFile(String nomeArquivo) throws RemoteException;

    byte[] getFileContent(String fileName) throws RemoteException;
}
