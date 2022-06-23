import javax.crypto.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// Клас, що в багато потоків шифрує відповідь та відправляє класу,
// що відповідає за передачу інформації по мережі
public class Encriptor {
    static final byte uniqClientNum = 11;
    static final byte startPackage = 0x13;
    static long numMessage = 0;
    static Key key;
    private static Cipher cipher;

    public static byte[] encrypt(Message message) throws IOException {
        ByteArrayOutputStream msgbout = new ByteArrayOutputStream();
        byte[] msgBytes = encryptMessage(message.msg);
        writeCRCtoStream(msgbout, msgBytes);
        int msgLength = msgBytes.length + 4;

        byte[] header = new byte[14];
        header[0] = startPackage;
        header[1] = uniqClientNum;
        header[2] = (byte) (numMessage >> 56 & 0xFF);
        header[3] = (byte) (numMessage >> 48 & 0xFF);
        header[4] = (byte) (numMessage >> 40 & 0xFF);
        header[5] = (byte) (numMessage >> 32 & 0xFF);
        header[6] = (byte) (numMessage >> 24 & 0xFF);
        header[7] = (byte) (numMessage >> 16 & 0xFF);
        header[8] = (byte) (numMessage >> 8 & 0xFF);
        header[9] = (byte) (numMessage & 0xFF);
        numMessage++;

        int packageL = msgLength + 18;
        header[10] = (byte) ((packageL & 0xFF) >> 24);
        header[11] = (byte) ((packageL & 0xFF) >> 16);
        header[12] = (byte) ((packageL & 0xFF) >> 8);
        header[13] = (byte) (packageL & 0xFF);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        writeCRCtoStream(byteStream, header);
        byteStream.write(msgbout.toByteArray());
        return byteStream.toByteArray();
    }


    private static byte[] encryptMessage(byte[] message) {
        try {
            if (cipher == null) {
                cipher = Cipher.getInstance("AES");
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                SecureRandom secRandom = new SecureRandom();
                keyGen.init(secRandom);
                key = keyGen.generateKey();
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }

            byte[] encrypted = cipher.doFinal(message);
            return encrypted;
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeCRCtoStream(ByteArrayOutputStream msgbout, byte[] msgBytes) throws IOException {
        msgbout.write(msgBytes);

        int msgCRC = CRC16.countCRC16(msgBytes);
        msgbout.write((msgCRC >>> 24) & 0xFF);
        msgbout.write((msgCRC >>> 16) & 0xFF);
        msgbout.write((msgCRC >>> 8) & 0xFF);
        msgbout.write(msgCRC & 0xFF);
    }
}
