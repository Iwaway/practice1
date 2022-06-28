import org.junit.Test;
import java.security.NoSuchAlgorithmException;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */

public class AppTest {
    @Test
        public void testConnectionUPD(){
        String host = "localhost";
        int port = 2000;

        new StoreServerUDP();
        new StoreClientUDP(host, port);
    }

    @Test
    public void testConnectionTCP(){
        String host = "localhost";
        int port = 1999;

        new StoreServerTCP();
        new StoreClientTCP(host, port);
    }
}

