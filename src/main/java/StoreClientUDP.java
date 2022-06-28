import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

public class StoreClientUDP implements Runnable {

    private byte[] sentMessage;
    private boolean needsResending = false;
    private ClientDecryptor decryptor = null;
    private MessageGenerator messageGenerator = null;
    private Key key;
    private String host;
    private int port;
    private byte id = -1;

    public StoreClientUDP(String host, int port) {
        this.host = host;
        this.port = port;
        KeyGenerator gen = null;
        try {
            gen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        gen.init(128, new SecureRandom("Secret".getBytes()));
        key = gen.generateKey();
        decryptor = new ClientDecryptor(key);
        messageGenerator = new MessageGenerator(id, key);
        new Thread(this).start();
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            socket.connect(InetAddress.getByName(host), port);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        while (true) {
            byte[] buf;
            if (needsResending) {
                buf = sentMessage;
                needsResending = false;
            } else {
                buf = messageGenerator.getMessage("milk 20", 1, 0);
            }
            sentMessage = buf;
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.send(packet);
                System.out.println("sent message");
            } catch (IOException e) {
                e.printStackTrace();
            }

            packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException ex) {
                needsResending = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] response = packet.getData();
            if (id == -1) {
                id = response[1];
                sentMessage[1] = id;
            }
            boolean correctAnswer = true;
            for (int i = 0; i < response.length && i < sentMessage.length; i++) {
                if (response[i] != sentMessage[i]) {
                    correctAnswer = false;
                    break;
                }
            }
            System.out.println("Answer successfully received");
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 2000;
        new StoreClientUDP(host, port);
    }

}
