import java.security.PrivateKey;
import java.security.PublicKey;

public class Main {


    public static void main(String[] args) throws Exception {
        RSA RSA;
        String[] names;
        names = new String[]{"server", "client1", "client2", "client3"};
        RSA = new RSA();
        for (int i = 0; i <names.length ; i++) {
            RSA.generateKeyPair(names[i]);
        }
    }
}
