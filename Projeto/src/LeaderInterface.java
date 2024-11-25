import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LeaderInterface extends Remote {
    void sendAck(String uuid, String messageType) throws RemoteException;
    void uploadFile(File file) throws RemoteException;
    void sendSetup(String uuid) throws RemoteException;
}