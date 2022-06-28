import java.net.InetAddress;
import java.net.UnknownHostException;

public class EncryptingWorker implements Runnable {

    private byte[] message;
    private byte id;

    public EncryptingWorker(byte[] message, byte id) {
        this.message = message;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            Sender.sendMessage(message, id, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
