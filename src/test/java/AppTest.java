import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AppTest {

    @Test
    public void test(){
        AppTest t = new AppTest();
        assertDoesNotThrow(() ->t.testMyNetwork());
    }

    @Test
    public  void testMyNetwork() throws InterruptedException {
        Receiver rec = new Receiver();
        for (int i = 0; i < 5; i++) {
            rec.receiveMessage();
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
