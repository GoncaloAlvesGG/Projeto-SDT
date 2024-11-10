import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LeaderInterface extends Remote {
    void sendAck(String uuid) throws RemoteException;
}
