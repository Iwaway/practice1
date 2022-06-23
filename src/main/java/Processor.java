import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Processor extends Thread{
    public static ArrayBlockingQueue<byte[]> answQueue;


    public Processor() {
        if (answQueue == null)
            answQueue = new ArrayBlockingQueue<>(10);
    }

    public static void process(Message message) {
        //form answer
        Message answMsg = new Message("OK",0,0);
        byte [] answer;
        try {
            answer = Encriptor.encrypt(answMsg);
            answQueue.add(answer);
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        boolean flag = false;
        while (true) {
            if (Decriptor.msgQueue.isEmpty())
                flag = true;
            if (flag) {
                break;
            }
            try {
                Message msg = Decriptor.msgQueue.poll(1000, TimeUnit.MILLISECONDS);
                process(msg);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
