import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// Фейкова реалізація відправки, що просто виводить інформацію на екран
public class Sender extends Thread {

    public Sender() {

    }

    public static void sendMessage(byte[] message, byte id, InetAddress target) {
        byte[] longNumber = Arrays.copyOfRange(message, 2, 10);
        ByteBuffer b = ByteBuffer.allocate(8);
        b.put(longNumber);
        b.flip();
        long messageId = b.getLong();
        Database.processedMessages.put(messageId, message);
    }
}
