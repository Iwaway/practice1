import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;

public class StoreServerUDP implements Runnable {

    private Decriptor decryptor = new Decriptor();
    private static Key key;
    private static int port = 2000;
    private static Selector selector;
    private static byte userAppId = 0;

    public StoreServerUDP() {
        KeyGenerator gen = null;
        try {
            gen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        gen.init(128, new SecureRandom("Secret".getBytes()));
        key = gen.generateKey();
        Database.clientKeys = new ConcurrentHashMap<>();
        Database.processedMessages = new ConcurrentHashMap<>();
        new Thread(this).start();
    }

    @Override
    public void run() {
        DatagramChannel channel = null;
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(port));
            channel.configureBlocking(false);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            System.out.println("Listening on port " + port);
            while (true) {
                int num = selector.select();
                if (num == 0)
                    continue;
                System.out.println("Incoming message");
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                        System.out.println("receiving message");
                        DatagramChannel dc = (DatagramChannel) key.channel();
                        ByteBuffer buf = ByteBuffer.allocate(100);
                        buf.clear();
                        dc.receive(buf);
                        buf.flip();
                        byte[] received = new byte[buf.remaining()];
                        buf.get(received);
                        processMessage(received, dc);
                    }
                }
                keys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processMessage(byte[] received, DatagramChannel dc) {
        byte[] messageNumber = Arrays.copyOfRange(received, 2, 10);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(messageNumber);
        buf.flip();
        long mn = buf.getLong();
        if (!Database.processedMessages.containsKey(mn)) {
            if (received[1] == -1) {
                Database.clientKeys.put(userAppId, key);
                received[1] = userAppId++;
            }
            decryptor.decryptMessage(received, key);
            while (!Database.processedMessages.containsKey(mn))
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        byte[] answer = Database.processedMessages.get(mn);
        ByteBuffer sender = ByteBuffer.allocate(100);
        sender.put(answer);
        sender.flip();
        SocketAddress add = null;
        try {
            add = dc.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

            dc.send(sender, add);
        } catch (NullPointerException ex) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StoreServerUDP();
    }
}
