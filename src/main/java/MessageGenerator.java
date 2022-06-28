import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MessageGenerator {
    private byte userAppId;
    private Key key;

    public MessageGenerator(byte userApId, Key key) {
        this.userAppId = userApId;
        this.key = key;
    }

    public byte[] getMessage(String message, int command, int userId) {
        byte[] msgTxt;
        byte[] packet = null;
        try {
            msgTxt = message.getBytes("UTF-8");
            byte[] msgArr = new byte[msgTxt.length + 8];
            addIntToByteArray(msgArr, 0, command);
            addIntToByteArray(msgArr, 4, userId);
            for (int i = 0; i < msgTxt.length; i++)
                msgArr[i + 8] = msgTxt[i];
            byte[] encrypted = encryptMessage(msgArr);
            int messageLength = encrypted.length;
            packet = new byte[22 + messageLength];
            packet[0] = 0x13;
            packet[1] = userAppId;
            long messageNumber = System.currentTimeMillis();
            System.out.println("Sending message: " + message + " " + messageNumber);
            addMessageNumberToByteArray(messageNumber, packet, 2);
            addIntToByteArray(packet, 10, messageLength);
            byte[] arr = Arrays.copyOfRange(packet, 0, 14);
            int firstCRC = CRC16.getCRC(arr);
            addIntToByteArray(packet, 14, firstCRC);
            int curOffset = 18;
            for (int i = 0; i < messageLength; i++) {
                packet[curOffset + i] = encrypted[i];
            }
            byte[] arr2 = Arrays.copyOfRange(packet, curOffset, messageLength + curOffset);
            int secondCRC = CRC16.getCRC(arr2);
            addIntToByteArray(packet, 18 + messageLength, secondCRC);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return packet;
    }

    private static void addMessageNumberToByteArray(long messageNumber, byte[] arr, int pos) {
        for (byte i = 56; i >= 0; i -= 8) {
            arr[pos] = (byte) ((messageNumber >> i) & 0xff);
            pos++;
        }
    }

    private static void addIntToByteArray(byte[] arr, int pos, int num) {
        for (byte i = 24; i >= 0; i -= 8) {
            arr[pos] = (byte) ((num >> i) & 0xff);
            pos++;
        }
    }

    private byte[] encryptMessage(byte[] arr) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(arr);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] createFromMessage(Message message) {
        byte[] msgTxt;
        byte[] packet = null;
        try {
            msgTxt = message.getMessage().getBytes("UTF-8");
            byte[] msgArr = new byte[msgTxt.length + 8];
            addIntToByteArray(msgArr, 0, message.getcType());
            addIntToByteArray(msgArr, 4, message.getbUserId());
            for (int i = 0; i < msgTxt.length; i++)
                msgArr[i + 8] = msgTxt[i];
            byte[] encrypted = encryptMessage(msgArr);
            int messageLength = encrypted.length;
            packet = new byte[22 + messageLength];
            packet[0] = 0x13;
            packet[1] = message.getClientAppId();
            long number = message.getMessageId();
            addMessageNumberToByteArray(number, packet, 2);
            addIntToByteArray(packet, 10, messageLength);
            byte[] arr = Arrays.copyOfRange(packet, 0, 14);
            int firstCRC = CRC16.getCRC(arr);
            addIntToByteArray(packet, 14, firstCRC);
            int curOffset = 18;
            for (int i = 0; i < messageLength; i++) {
                packet[curOffset + i] = encrypted[i];
            }
            byte[] arr2 = Arrays.copyOfRange(packet, curOffset, messageLength + curOffset);
            int secondCRC = CRC16.getCRC(arr2);
            addIntToByteArray(packet, 18 + messageLength, secondCRC);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return packet;
    }

    public void setClientId(byte id) {
        userAppId = id;
    }
}
