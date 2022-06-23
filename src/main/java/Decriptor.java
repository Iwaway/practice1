import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

// Клас, що в багато потоків розбирає,
// дешифрує та перетворює повідомлення в об'єкт домену
// Після чого передає на обробник повідомлення
public class Decriptor extends Thread {

    private static Key key;
    private static Cipher cipher;
    public static ArrayBlockingQueue<Message> msgQueue;

    public Decriptor() {
        if (msgQueue == null)
            msgQueue = new ArrayBlockingQueue<>(10);
        key = Encriptor.key;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    public static void decrypt(byte[] message) throws IOException {
        byte[] header = new byte[14];
        System.arraycopy(message, 0, header, 0, 14);
        int hCRC = ((message[14] & 0xFF) << 24) | ((message[15] & 0xFF) << 16) |
                ((message[16] & 0xFF) << 8) | (message[17] & 0xFF);
        checkCRC(hCRC, header);

        int packageL = message.length;
        int msgLength = packageL - 22;
        byte[] msgPart = new byte[msgLength];
        System.arraycopy(message, 18, msgPart, 0, msgLength);

        int msgCRC = ((message[packageL - 4] & 0xFF) << 24) | ((message[packageL - 3] & 0xFF) << 16) |
                ((message[packageL - 2] & 0xFF) << 8) | (message[packageL - 1] & 0xFF);
        checkCRC(msgCRC, msgPart);
        byte[] decryptedMsg = decryptMsgPart(msgPart);
        int command = ((decryptedMsg[0] & 0xFF) << 24) | ((decryptedMsg[1] & 0xFF) << 16) |
                ((decryptedMsg[2] & 0xFF) << 8) | (decryptedMsg[3] & 0xFF);
        int user = ((decryptedMsg[4] & 0xFF) << 24) | ((decryptedMsg[5] & 0xFF) << 16) |
                ((decryptedMsg[6] & 0xFF) << 8) | (decryptedMsg[7] & 0xFF);
        byte[] m = new byte[decryptedMsg.length - 8];
        System.arraycopy(decryptedMsg, 8, m, 0, decryptedMsg.length - 8);
        Message message1 = new Message(m.toString(), command, user);
        msgQueue.add(message1);
    }

    public static byte[] decryptMsgPart(byte[] toDecrypt) {
        try {
            if (cipher == null) {
                cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            return cipher.doFinal(toDecrypt);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    private static void checkCRC(int msgCRC, byte[] msgPart) throws IOException {
        int cm = CRC16.countCRC16(msgPart);
        if (cm != msgCRC)
            throw new IOException("The package was damaged");
    }

    @Override
    public void run() {
        boolean flag = false;
        while (true) {
            if (Receiver.queue.isEmpty())
                flag = true;
            if (flag) {
                break;
            }
            try {
                byte[] encoded = Receiver.queue.poll(1000, TimeUnit.MILLISECONDS);
                decrypt(encoded);


            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
