import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Random;

public class Main {


    public static void main(String[] args) throws GeneralSecurityException, UnsupportedEncodingException {

        String secretKey_physical = "thisiskeyforphysicalkey";

        String originalString = "thisisphysicalkey";
        PhysicalKey c = new PhysicalKey();

//        c.createPhysicalKey(secretKey_physical,originalString,"1");
//        c.createPhysicalKey(secretKey_physical,originalString,"2");
//        c.createPhysicalKey(secretKey_physical,originalString,"3");
//        c.createPhysicalKey(secretKey_physical,originalString,"4");
//        c.createPhysicalKey(secretKey_physical,originalString,"5");

        c.createPhysicalKey(secretKey_physical,generateRandom(),"1");
        c.createPhysicalKey(secretKey_physical,generateRandom(),"2");
        c.createPhysicalKey(secretKey_physical,generateRandom(),"3");


    }

    private static String generateRandom(){
        String originalString;
        char[] physical_chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(17);
        Random random = new Random();
        for (int i = 0; i < 17; i++) {
            char c = physical_chars[random.nextInt(physical_chars.length)];
            sb.append(c);
        }
        originalString = sb.toString();
        return originalString;
    }


}
