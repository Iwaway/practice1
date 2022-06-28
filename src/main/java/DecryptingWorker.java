import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DecryptingWorker implements Runnable {

    private byte[] message;
    private Key key;

    public DecryptingWorker(byte[] message, Key key) {
        this.message = message;
        this.key = key;
    }

    public boolean isNotCorrupted(byte[] message) {
        byte[] arr1 = Arrays.copyOfRange(message, 0, 14);
        int calCRC1 = CRC16.getCRC(arr1);
        int recCRC1 = fromByteArray(message, 14);
        if (calCRC1 != recCRC1)
            return false;
        int msgLength = fromByteArray(message, 10);
        byte[] arr2 = Arrays.copyOfRange(message, 18, 18 + msgLength);
        int calCRC2 = CRC16.getCRC(arr2);
        int recCRC2 = fromByteArray(message, 18 + msgLength);
        return calCRC2 == recCRC2;
    }

    private int fromByteArray(byte[] bytes, int offset) {
        return bytes[offset] << 24 | (bytes[offset + 1] & 0xFF) << 16 | (bytes[offset + 2] & 0xFF) << 8
                | (bytes[offset + 3] & 0xFF);
    }

    private long longFromByteArray(byte[] bytes, int offset) {
        long res = ((bytes[offset] & 0xFFL) << 56) | ((bytes[offset + 1] & 0xFFL) << 48)
                | ((bytes[offset + 2] & 0xFFL) << 40) | ((bytes[offset + 3] & 0xFFL) << 32)
                | ((bytes[offset + 4] & 0xFFL) << 24) | ((bytes[offset + 5] & 0xFFL) << 16)
                | ((bytes[offset + 6] & 0xFFL) << 8) | ((bytes[offset + 7] & 0xFFL) << 0);
        return res;
    }

    @Override
    public void run() {
        /*
         * if (!isNotCorrupted(message)) throw new
         * IllegalArgumentException("Data was corrupted");
         */
        int msgLength = fromByteArray(message, 10);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] toDecrypt = Arrays.copyOfRange(message, 18, 18 + msgLength);
        byte[] decrypted = null;
        try {
            decrypted = cipher.doFinal(toDecrypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        byte clientAppId = message[1];
        long messageNumber = longFromByteArray(message, 2);
        int cType = fromByteArray(decrypted, 0);
        int bUserId = fromByteArray(decrypted, 4);
        byte[] decryptedMsg = Arrays.copyOfRange(decrypted, 8, decrypted.length);
        String s = new String(decryptedMsg);
        Message m = new Message(clientAppId, messageNumber, cType, bUserId, s);
        Processor.processMessage(m);
    }

}
