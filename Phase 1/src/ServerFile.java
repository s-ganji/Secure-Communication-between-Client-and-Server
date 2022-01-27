import sun.security.krb5.internal.crypto.Aes128;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ServerFile {

    private ServerSocket ssock;
    private Socket socket;

    private File file;
    private FileInputStream fis;
    private BufferedInputStream bis;

    private OutputStream outNet = null;
    private InputStream input;

    private InputStreamReader inReader;
    private BufferedReader br;


    private OutputStreamWriter streamWriter = null;
    private BufferedWriter bt = null;

    private String secretKey;
    private String physicalKey;

    private String meetingKey;
    private String meetingKey_updated;

    private long fileLength;

    private Timer timer;

    private String user_number;


    public ServerFile() throws IOException {
        ssock = new ServerSocket(6789);
        socket = ssock.accept();
    }

    public void serverFile() throws Exception {

        //create random 16 byte meeting key
        char[] meeting_chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            char c = meeting_chars[random.nextInt(meeting_chars.length)];
            sb.append(c);
        }
        meetingKey = sb.toString();

        //secretkey for encrypt/decrypt physical key
        secretKey = "thisiskeyforphysicalkey";

        //send client usename from client
        set_username();
        //get physical key from physical_key(username).txt file and decrypt it with secretKey
        setPhysicalKey();
        System.out.println("physical key:");
        System.out.println(physicalKey);
        //send first meeting key to client and encrypt it with physicalKey
        sendMeetingKey(physicalKey, meetingKey);
        System.out.println("meeting key:");
        System.out.println(meetingKey);

        //set timer for updating the meeting key every 1500 seconds
        timer = new Timer();
        timer.schedule(new updateMeetingKey(),10,1500000);
        socket.close();

        //send first file to client and encrypt it with meeting key
        socket = ssock.accept();
        sendFileSize("./files/sendFile.txt");
        sendFile("./files/sendFile.txt");
        socket.close();

        //send updated meeting key and encrypt it with meeting key
        socket = ssock.accept();
        sendMeetingKey(meetingKey,meetingKey_updated);
        meetingKey = meetingKey_updated;
        socket.close();

        //send second file and encrypt it with updated meeting key
        socket = ssock.accept();
        System.out.println("updated meeting key:");
        System.out.println(meetingKey);
        sendFileSize("./files/download.jpg");
        sendFile("./files/download.jpg");
        socket.close();

        //update physical key file with another physical key and encrypt it with meeting key
        updatePhysicalKey();

        //set physical key with updated physical key
        setPhysicalKey();
        System.out.println("updated physical key:");
        System.out.println(physicalKey);

        ssock.close();
    }


    private class updateMeetingKey extends TimerTask {
        public void run() {
            try {
                char[] meeting_updated_chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                StringBuilder sb = new StringBuilder(16);
                Random random = new Random();
                for (int i = 0; i < 16; i++) {
                    char c = meeting_updated_chars[random.nextInt(meeting_updated_chars.length)];
                    sb.append(c);
                }
                meetingKey_updated = sb.toString();

                AES aes = new AES();
                byte[] line = aes.encrypt(meetingKey, meetingKey_updated.getBytes("UTF-8"));

                timer = new Timer();
                timer.schedule(new updateMeetingKey(),10,1500000);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void set_username() throws Exception {
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        user_number = br.readLine();
    }

    private void setPhysicalKey() throws UnsupportedEncodingException {
        PhysicalKey phyKey = new PhysicalKey();
        physicalKey = phyKey.getPhysicalKey(secretKey, user_number);
    }

    private void sendMeetingKey(String key, String plainText) throws Exception {
        AES aes = new AES();
        byte[] line = aes.encrypt(key, plainText.getBytes("UTF-8"));

        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        outNet.write(meetingKey.length());
        outNet.write(line);
        outNet.flush();
    }

    private void sendFile(String filePath) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        File file = new File(filePath);

        byte[] b = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);

        fis.read(b);
        fis.close();

        AES aes = new AES();
        byte[] output = aes.encrypt(meetingKey, b);

        dos.write(output, 0, output.length);
        dos.flush();

        System.out.println("file sent successfully");
    }

    private void sendFileSize(String filePath) throws IOException {
        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        file = new File(filePath);
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);

        fileLength = file.length();

        bt.write((int) fileLength);
        bt.flush();
    }

    private void updatePhysicalKey() throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String update_physical;
        char[] physical_chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(17);
        Random random = new Random();
        for (int i = 0; i < 17; i++) {
            char ph = physical_chars[random.nextInt(physical_chars.length)];
            sb.append(ph);
        }
        update_physical = sb.toString();
        PhysicalKey c = new PhysicalKey();
        c.createPhysicalKey(secretKey, update_physical, user_number);
    }

    public static void main(String[] args) throws Exception {
        ServerFile server = new ServerFile();
        server.serverFile();
    }
}
