import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class StoreClientTCP implements Runnable {

    private ClientDecryptor decryptor = null;
    private MessageGenerator messageCreator = null;
    private Key key;
    private byte id = -1;
    private String host;
    private int port;
    private boolean keySent = false;
    private boolean serverConnected = false;

    public StoreClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            Socket s = new Socket(host, port);
            System.out.println("Connecting to server");
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            serverConnected = true;

            while (true) {
                if (!keySent) {
                    System.out.println("Sending key to server");
                    byte buffer[] = new byte[95];
                    int read = in.read(buffer);
                    id = buffer[94];
                    byte[] pubKey = Arrays.copyOfRange(buffer, 0, 94);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubKey));
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    KeyGenerator gen = KeyGenerator.getInstance("AES");
                    gen.init(128, new SecureRandom());
                    key = gen.generateKey();
                    byte[] keyBytes = key.getEncoded();
                    byte[] encryptedKeyBytes = cipher.doFinal(keyBytes);
                    int len = encryptedKeyBytes.length + 1;
                    byte[] res = new byte[len];
                    for (int i = 0; i < encryptedKeyBytes.length; i++) {
                        res[i] = encryptedKeyBytes[i];
                    }
                    res[len - 1] = id;
                    out.write(res);
                    keySent = true;
                    System.out.println("Key sent");
                    messageCreator = new MessageGenerator(id, key);
                    decryptor = new ClientDecryptor(key);
                } else {
                    byte[] message = messageCreator.getMessage("vfd 200", 1, 0);
                    try {
                        out.write(message);
                    } catch (SocketException se) {
                        serverConnected = false;
                        for (int i = 0; i < 10; i++) {
                            tryToConnectToServer(s);
                            if (serverConnected)
                                break;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        in = s.getInputStream();
                        out = s.getOutputStream();
                        out.write(message);
                    }
                    byte[] response = new byte[100];
                    int read = 0;
                    try {
                        read = in.read(response);
                    } catch (SocketException se) {
                        serverConnected = false;
                        for (int i = 0; i < 10; i++) {
                            tryToConnectToServer(s);
                            if (serverConnected)
                                break;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        in = s.getInputStream();
                        out = s.getOutputStream();
                        in.read(response);
                    }

                    while (read == -1) {
                        System.out.println("Connection error. Resetting socket.");
                        s = new Socket(host, port);
                        in = s.getInputStream();
                        out = s.getOutputStream();
                        read = in.read(response);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    byte[] toDecrypt = Arrays.copyOf(response, read);
                    decryptor.decrypt(toDecrypt);
                    System.out.println("");

                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    }

    private void tryToConnectToServer(Socket s) {
        try {
            s.close();
            s = new Socket(host, port);
        } catch (ConnectException ce) {
            System.out.println("Trying to connect to the server");
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (s.isConnected())
            serverConnected = true;

    }

    static public void main(String args[]) throws Exception {

        String host = "localhost";
        int port = 1999;
        /*
         * for (int i = 0; i < 50; i++) { new StoreClientTCP(host, port); }
         */
        new StoreClientTCP(host, port);
    }

}
