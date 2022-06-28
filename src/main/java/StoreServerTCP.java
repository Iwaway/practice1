import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class StoreServerTCP implements Runnable {

    private Decriptor decryptor = new Decriptor();
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static int port = 1999;
    private static Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static byte userAppId = 0;
    private static long messageId = 0;

    public StoreServerTCP() {
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Key generator exception");
        }
        gen.initialize(512, new SecureRandom());
        KeyPair pair = gen.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
        new Thread(this).start();
    }

    @Override
    public void run() {
        ServerSocketChannel ssc;
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ServerSocket ss = ssc.socket();
            InetSocketAddress isa = new InetSocketAddress(port);
            ss.bind(isa);
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Listening on port " + port);
            while (true) {
                int num = selector.select();
                if (num == 0)
                    continue;
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                        System.out.println("accepting client connection");
                        Socket s = ss.accept();
                        System.out.println("Got connection from " + s);
                        SocketChannel sc = s.getChannel();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                        sendUserPublicKey(sc);
                    }
                    if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                        System.out.println("receiving message");
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buf = ByteBuffer.allocate(100);
                        buf.clear();
                        try {
                            sc.read(buf);
                        } catch (IOException ex) {
                            key.cancel();
                            Socket s = null;
                            try {
                                s = sc.socket();
                                s.close();
                            } catch (IOException ie) {
                                System.err.println("Error closing socket " + s + ": " + ie);
                            }
                        }
                        buf.flip();
                        byte[] received = new byte[buf.remaining()];
                        buf.get(received);
                        if (received[0] == 0x13) {
                            System.out.println("Package processing");
                            processPackage(received, sc);
                        } else {
                            receiveUserKey(received);
                        }

                    }
                }
                keys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processPackage(byte[] received, SocketChannel channel) throws IOException {
        byte bUserId = received[1];
        byte[] messageNumber = Arrays.copyOfRange(received, 2, 10);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(messageNumber);
        buf.flip();
        long mn = buf.getLong();
        Key key = Database.clientKeys.get(bUserId);
        decryptor.decryptMessage(received, key);
        while (!Database.processedMessages.containsKey(mn)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] answer = Database.processedMessages.get(mn);
        Database.processedMessages.remove(mn, answer);
        ByteBuffer sender = ByteBuffer.allocate(100);
        sender.put(answer);
        sender.flip();
        while (sender.hasRemaining()) {
            channel.write(sender);
        }

    }

    private void sendUserPublicKey(SocketChannel sc) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(95);
        buf.put(publicKey.getEncoded());
        buf.put(userAppId);
        buf.flip();
        while (buf.hasRemaining()) {
            sc.write(buf);
        }
        userAppId++;
    }

    private void receiveUserKey(byte[] received) {
        byte id = received[received.length - 1];
        byte[] encrypted = new byte[64];
        for (int i = 0; i < 64; i++) {
            encrypted[i] = received[i];
        }
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(encrypted);
            Key sk = new SecretKeySpec(decrypted, "AES");
            System.out.println("Received key from client");
            Database.clientKeys.put(id, sk);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StoreServerTCP();
    }

}
