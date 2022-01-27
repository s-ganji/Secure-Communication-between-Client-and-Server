import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

public class AES {

    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static int key_length;

    public static void setKey(String secret) throws UnsupportedEncodingException {
        key_length =16;
        key = fixSecret(secret, key_length);
        secretKey = new SecretKeySpec(key ,"AES");
    }
    private static byte[] fixSecret(String s, int length) throws UnsupportedEncodingException {
        if (s.length() < length) {
            int missingLength = length - s.length();
            for (int i = 0; i < missingLength; i++) {
                s += " ";
            }
        }
        return s.substring(0, length).getBytes("UTF-8");
    }

    public static byte[] encrypt(String secret, byte[] plainText){

        try{
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,ivspec);
            return cipher.doFinal(plainText);
        }
        catch (Exception e){
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }


    public static byte[] decrypt(String secret,byte[] cipherText){
        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,ivspec);
            return cipher.doFinal(cipherText);
        }

        catch (Exception e){
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

}
