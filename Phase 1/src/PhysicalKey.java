import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class PhysicalKey {

    //create physical key and encrypt it with secretKey and put it in physical_key(username).txt file
    public void createPhysicalKey(String secretKey, String plaintext,String user_number) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        AES aes = new AES();
        String plain = plaintext+user_number;
        byte[] cipherText = aes.encrypt(secretKey, plain.getBytes("UTF-8"));
        writeToFile(cipherText,user_number);
    }

    private static void writeToFile(byte[] cipherText , String user_number) {
        String pathName = "physical_key"+user_number+".txt";
        File file = new File(pathName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(cipherText);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while writing file " + ioe);
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }

        }

    }

    //get encrypt physical key from physical_key(username).txt file and decrypt it with secretKey
    public String getPhysicalKey(String secretKey, String user_number) throws UnsupportedEncodingException {
        byte[] cipherText =readFile(user_number);
        AES aes = new AES();
        byte[] plainText = aes.decrypt(secretKey,cipherText);
        return new String(plainText,"UTF-8");
    }

    private byte[] readFile(String user_number) {
        String path = "physical_key"+user_number+".txt";
        File file = new File(path);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte fileContent[] = new byte[(int) file.length()];
            fin.read(fileContent);
            String s = new String(fileContent);
            return fileContent;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        return null;
    }

    }



