import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Decriptor {

    private static ExecutorService executor;

    public Decriptor() {
        executor = Executors.newFixedThreadPool(Receiver.THREADS);
    }

    public void decryptMessage(byte[] message, Key key) {
        executor.execute(new DecryptingWorker(message, key));
    }

}
