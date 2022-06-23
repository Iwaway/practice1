import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

// Фейкова реалізація відправки, що просто виводить інформацію на екран
public class Sender extends Thread {

    public Sender() {

    }

    public static void sendMessage(byte[] message, InetAddress target) {
        System.out.println("OK");
    }

    @Override
    public void run() {
        boolean flag = false;
        while (true) {
            if (Processor.answQueue.isEmpty())
                flag = true;
            if (flag) {
                break;
            }
            try {
                byte[] answ = Processor.answQueue.poll(1000, TimeUnit.MILLISECONDS);
                sendMessage(answ, null);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
