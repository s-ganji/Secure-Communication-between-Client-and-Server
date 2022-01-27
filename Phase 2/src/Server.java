import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class Server {
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

    private long fileSize;
    private long  messageSize;

    private Timer timer;

    private String user_number;

    private RSA RSA;

    public HashMap<String, PublicKey> pubKey;
    private PrivateKey privateKey;

    private String result;

    public Server() throws IOException {
        ssock = new ServerSocket(6789);
        socket = ssock.accept();
        RSA = new RSA();
    }

    public void serverFile() throws Exception {

        //creating a table for keys in server with hashmap,key is client's username, value is client's publicKey
        pubKey = new HashMap<>();
        for (int i = 1; i <=3 ; i++) {
            pubKey.put("client"+i, RSA.getPubKey("client"+i));
        }
        privateKey = RSA.getPrivateKey("server");

        //creating a random session key
        char[] meeting_chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            char c = meeting_chars[random.nextInt(meeting_chars.length)];
            sb.append(c);
        }
        meetingKey = sb.toString();

        // receive client usename from client
        set_username();

        //encrypt random session key with client's publicKey and send it to client
        sendMeetingKey(meetingKey);
        System.out.printf("session key: %s\n",meetingKey);

        //set timer for updating the meeting key every 1500 seconds
        timer = new Timer();
        timer.schedule(new updateMeetingKey(),10,1500000);
        socket.close();

        //send first file to client and encrypt it with session key
        socket = ssock.accept();
        sendFileSize("./files/sendFile.txt");
        socket.close();
        socket = ssock.accept();
        sendMessageSize("./files/sendFile.txt");
        sendFile("./files/sendFile.txt");
        socket.close();

        //Check that the first file was authenticated correctly or not, if not server should send file again
        socket = ssock.accept();
        get_result();
        socket.close();
        if(!result.equals("file authenticated successfully"))
        {
            socket = ssock.accept();
            sendFileSize("./files/sendFile.txt");
            socket.close();
            socket = ssock.accept();
            sendMessageSize("./files/sendFile.txt");
            sendFile("./files/sendFile.txt");
            socket.close();
        }

        System.out.printf("result from client for first file authentication: %s\n",result);
        System.out.println();

        //send updated meeting key and encrypt it with client's public key
        socket = ssock.accept();
        sendMeetingKey(meetingKey_updated);
        meetingKey = meetingKey_updated;
        socket.close();
        System.out.printf("updated session key: %s\n",meetingKey);

        //send second file and encrypt it with updated meeting key
        socket = ssock.accept();
        sendFileSize("./files/download.jpg");
        socket.close();
        socket = ssock.accept();
        sendMessageSize("./files/download.jpg");
        sendFile("./files/download.jpg");
        socket.close();

        //Check that the second file was authenticated correctly or not, if not server should send file again
        socket = ssock.accept();
        get_result();
        socket.close();
        if(!result.equals("file authenticated successfully"))
        {
            socket = ssock.accept();
            sendFileSize("./files/sendFile.txt");
            socket.close();
            socket = ssock.accept();
            sendMessageSize("./files/sendFile.txt");
            sendFile("./files/sendFile.txt");
            socket.close();
        }
        System.out.printf("result from client for second file authentication: %s\n",result);

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

                PublicKey f = pubKey.get("client"+user_number);
                String line = RSA.encrypt(meetingKey_updated,f);

                timer = new Timer();
                timer.schedule(new updateMeetingKey(),10,1500000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
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

    public void get_result() throws Exception {
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        result = br.readLine();
    }

    private void sendMeetingKey(String plainText) throws Exception {
        PublicKey f = pubKey.get("client"+user_number);
        String line = RSA.encrypt(plainText,f);

        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        bt.write(line);
        bt.flush();
        outNet.flush();
    }

    private void sendFileSize(String filePath) throws IOException {
        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        file = new File(filePath);
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);

        fileSize= file.length();

        bt.write((int) fileSize);
        bt.flush();
    }

    private void sendMessageSize(String filePath) throws IOException, NoSuchAlgorithmException {
        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        file = new File(filePath);
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);

        byte[] b = new byte[(int) file.length()];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(b);

        messageSize = b.length+digest.length;

        bt.write((int) messageSize);
        bt.flush();
    }

    private void sendFile(String filePath) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        File file = new File(filePath);

        byte[] b = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);

        fis.read(b);
        fis.close();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(b);

        System.out.printf("message size: %d\n",messageSize);
        System.out.printf("file size: %d\n",fileSize);
        System.out.printf("digest size: %d\n",digest.length);

        byte[] message = new byte[b.length+digest.length];
        System.arraycopy(b,0,message,0,b.length);
        System.arraycopy(digest,0,message,b.length,digest.length);


        AES aes = new AES();
        byte[] output = aes.encrypt(meetingKey, message);


        dos.write(output, 0, output.length);
        dos.flush();

        System.out.println("file sent successfully");
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.serverFile();
    }
}
