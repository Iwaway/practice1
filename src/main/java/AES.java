import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {
    static Cipher cipher;
    static SecretKeySpec skeySpec;

    private static void genKey()  {

        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        skeySpec = new SecretKeySpec(key, "AES");
    }

    public static byte[] encryptData(byte[] target)  {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        genKey();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(target);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptData(byte[] target) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(target);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}