import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;

public class Client3 {


    private Socket socket;

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private OutputStream outNet= null;
    private InputStream input=null;

    private OutputStreamWriter  streamWriter = null;
    private BufferedWriter bt= null;

    private File file;
    private FileInputStream fis;
    private BufferedInputStream bis;

    private long fileLength;

    private InputStreamReader inReader;
    private BufferedReader br;

    private String secretKey;
    private String physicalKey;

    private String meetingKey;

    private int fileSize;
    private int messageSize;

    private byte[] decrypt_file;

    private PrintStream stream;

    private String user_number;
    private RSA RSA;

    public HashMap<String, PublicKey> pubKey;
    private PrivateKey privateKey;

    private int error;
    private String result;

    public Client3() throws IOException {
        socket = new Socket("localhost", 6789);
        RSA = new RSA();
    }

    public void clientFile3() throws Exception{

        user_number = "3";
        error = 0;

        //creating a table for keys in client1 with hashmap, key is server's username, value is server's publicKey
        pubKey = new HashMap<>();
        pubKey.put("server", RSA.getPubKey("server"));
        privateKey = RSA.getPrivateKey("client"+user_number);

        //send client username to server
        send_username();

        //receive first meeting key from server and decrypt it with client's private key
        receiveMeetingKey();
        socket.close();
        System.out.printf("session key: %s\n",meetingKey);


        //receive first file from server and decrypt it with meeting key
        socket = new Socket("localhost", 6789);
        fileSize = 0;
        receiveFileLength();
        socket.close();
        socket = new Socket("localhost", 6789);
        receiveMessageSize();
        receiveFile("./files/receivedFile1.txt");
        socket.close();

        //checking error in first file authentication
        if(error == 1)
        {
            socket = new Socket("localhost", 6789);
            result = "error in authentication, please send file again";
            send_result(result);
            socket.close();
            error = 0;
            socket = new Socket("localhost", 6789);
            fileSize = 0;
            receiveFileLength();
            socket.close();
            socket = new Socket("localhost", 6789);
            receiveMessageSize();
            receiveFile("./files/receivedFile1.txt");
            socket.close();
        }
        if(error ==0){
            socket = new Socket("localhost", 6789);
            result = "file authenticated successfully";
            send_result(result);
            socket.close();
        }
        System.out.printf("result of first file authentication: %s\n",result);
        System.out.println();

        //receive updated meeting key and decrypt it with client's private key
        socket = new Socket("localhost", 6789);
        receiveMeetingKey();
        socket.close();
        System.out.printf("updated session key: %s\n",meetingKey);

        //receive second file from server and decrypt it with meeting key
        socket = new Socket("localhost", 6789);
        fileSize = 0;
        receiveFileLength();
        socket.close();
        socket = new Socket("localhost", 6789);
        receiveMessageSize();
        receiveFile("./files/receivedFile2.jpg");
        socket.close();

        //checking error in second file authentication
        if(error == 1)
        {
            socket = new Socket("localhost", 6789);
            result = "error in authentication, please send file again";
            send_result(result);
            socket.close();
            error = 0;
            socket = new Socket("localhost", 6789);
            fileSize = 0;
            receiveFileLength();
            socket.close();
            socket = new Socket("localhost", 6789);
            receiveMessageSize();
            receiveFile("./files/receivedFile1.txt");
            socket.close();
        }
        if(error ==0){
            socket = new Socket("localhost", 6789);
            result = "file authenticated successfully";
            send_result(result);
            socket.close();
        }

        System.out.printf("result of second file authentication: %s\n",result);

    }

    private void send_username()throws Exception{
        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        String line = user_number;
        String sendMessage = line + "\n";
        bt.write(sendMessage);
        bt.flush();
    }
    private void send_result(String result)throws Exception{
        outNet = socket.getOutputStream();
        streamWriter = new OutputStreamWriter(outNet);
        bt = new BufferedWriter(streamWriter);

        String line = result;
        String sendMessage = line + "\n";
        bt.write(sendMessage);
        bt.flush();
    }

    private void receiveMeetingKey()throws Exception{
        PrivateKey h = privateKey;
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);

        String dataArray = br.readLine();
        meetingKey = RSA.decrypt(dataArray,h);

    }

    private void receiveFileLength() throws IOException {
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        fileSize = br.read();
    }

    private void receiveMessageSize() throws IOException {
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        messageSize = br.read();
    }

    private void receiveFile(String filePath)throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        File file = new File(filePath);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] message = new byte[messageSize];
        byte[] b = new byte[fileSize];
        byte[] digest = new byte[messageSize - fileSize+1];

        int i = 0;
        for(i = 0; i < messageSize; i++) {
            try {

                message[i] = dis.readByte();
            }
            catch (EOFException e)
            {
                break;
            }
        }

        System.out.printf("message size: %d\n",messageSize);
        System.out.printf("file size: %d\n",fileSize);
        System.out.printf("digest size: %d\n",digest.length-1);

        AES aes = new AES();
        decrypt_file = aes.decrypt(meetingKey ,message);

        b = Arrays.copyOfRange(decrypt_file,0,fileSize);
        digest = Arrays.copyOfRange(decrypt_file,fileSize,messageSize);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest_new = md.digest(b);

        if(new String(digest,"UTF-8").equals(new String(digest_new,"UTF-8"))) {
            fos.write(b, 0 , b.length);
            System.out.println("file received successfully");
        }
        else {
            error = 1;
        }
    }

    public static void main(String[] args) throws Exception{
        Client3 clientFile = new Client3();
        clientFile.clientFile3();
    }

}
