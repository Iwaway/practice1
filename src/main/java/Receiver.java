import java.util.concurrent.ArrayBlockingQueue;

// Інтерфейс що приймає повідомлення по мережі
public class Receiver {

    public static ArrayBlockingQueue<byte[]> queue;
    public static PackageGenerator generator;

    public Receiver(){
        if(queue==null)
            queue = new ArrayBlockingQueue<>(10);
        if(generator==null)
            generator= new PackageGenerator();
    }
    public static void receiveMessage() throws InterruptedException {
        byte [] packg = generator.generatePackage();
        queue.add(packg);
    }
}
