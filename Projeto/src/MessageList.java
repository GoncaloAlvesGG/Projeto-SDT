import java.util.ArrayList;
import java.util.List;

public class MessageList {
    private final ArrayList<Message> messages = new ArrayList<>();

    public synchronized void addMessage(Message message) {
            messages.add(message);
    }

    public synchronized void removeMessage(Message message) {
        messages.remove(message);

    }

    public List<Message> getMessages() {
            return new ArrayList<>(messages);

    }

}
