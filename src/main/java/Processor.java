import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Processor {

    private static ExecutorService executor = Executors.newFixedThreadPool(Receiver.THREADS);
    private static final Storage storage = new Storage();

    public static void processMessage(Message message) {
        executor.execute(new ProcessingWorker(message));
    }
}
