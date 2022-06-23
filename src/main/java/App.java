// Клас, що в багато потоків приймає перетворене повідомлення та формує відповідь.
// Поки достатньо відповіді Ок.
public class App {
    public static void main(String[] args){
        Receiver rec = new Receiver();
        for (int i = 0; i < 5; i++) {
            try {
                rec.receiveMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Decriptor dec1 = new Decriptor();
        Decriptor dec2 = new Decriptor();
        dec1.start();
        dec2.start();

        Processor p1 = new Processor();
        Processor p2 = new Processor();
        p1.start();
        p2.start();

        Sender s1 = new Sender();
        Sender s2 = new Sender();
        s1.start();
        s2.start();
    }
}
