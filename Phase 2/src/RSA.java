import javax.crypto.Cipher;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {

    public  void generateKeyPair(String name) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        try (FileOutputStream out = new FileOutputStream(name+"_private" + ".key")) {
            out.write(kp.getPrivate().getEncoded());
        }

        try (FileOutputStream out = new FileOutputStream(name+"_public" + ".pub")) {
            out.write(kp.getPublic().getEncoded());
        }
    }

//    public Array getKeyTable(String name)throws Exception{
//
//    }

    public PublicKey getPubKey(String name)throws Exception{
        byte[] bytes = Files.readAllBytes(Paths.get(name+"_public.pub"));
        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(ks);

        return pub;
    }


    public  String encrypt(String plainText,PublicKey pub) throws Exception {
//        byte[] bytes = Files.readAllBytes(Paths.get("fpublic.pub"));
//        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        PublicKey pub = kf.generatePublic(ks);

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pub);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public PrivateKey getPrivateKey(String name)throws Exception{
        byte[] bytes = Files.readAllBytes(Paths.get(name+"_private.key"));
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pvt = kf.generatePrivate(ks);
        return pvt;
    }

    public  String decrypt(String cipherText,PrivateKey pvt) throws Exception {
//        byte[] bytes = Files.readAllBytes(Paths.get("fprivate.key"));
//        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        PrivateKey pvt = kf.generatePrivate(ks);

        byte[] bytes2 = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, pvt);

        return new String(decriptCipher.doFinal(bytes2), "UTF-8");
    }

//    public static void main(String[] args) throws Exception {
//        generateKeyPair();
//        //Our secret message
//        String message = "the answer to life the universe and everything";
//
//        //Encrypt the message
//        String cipherText = encrypt(message);
//
////Now decrypt it
//        String decipheredMessage = decrypt(cipherText);
//
//        System.out.println(decipheredMessage);
//    }



}
