import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Encriptor {

    private static ExecutorService executor = Executors.newFixedThreadPool(Receiver.THREADS);

    public static void encryptResponse(byte[] response, byte id) {
        executor.execute(new EncryptingWorker(response, id));
    }

}
