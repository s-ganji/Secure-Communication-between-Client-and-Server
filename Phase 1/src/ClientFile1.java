import java.io.*;
import java.net.Socket;



public class ClientFile1 {
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

    private int length;

    private byte[] decrypt_file;

    private AES aes;
    private PrintStream stream;

    private String user_number;

    public ClientFile1() throws IOException {
        socket = new Socket("localhost", 6789);
    }

    public void clientFile() throws Exception{

        aes = new AES();

        user_number = "1";
        //secretkey for encrypt/decrypt physical key
        secretKey = "thisiskeyforphysicalkey";

        //send client usename to server
        send_username();
        //get physical key from physical_key(username).txt file and decrypt it with secretKey
        setPhysicalKey();
        System.out.println("physical key:");
        System.out.println(physicalKey);
        //receive first meeting key from server and decrypt it with physicalKey
        receiveMeetingKey(physicalKey);
        System.out.println("meeting key:");
        System.out.println(meetingKey);
        socket.close();

        //receive first file from server and decrypt it with meeting key
        socket = new Socket("localhost", 6789);
        length = 0;
        receiveFileLength();
        receiveFile("./files/receivedFile1.txt");
        socket.close();

        //receive updated meeting key and decrypt it with meeting key
        socket = new Socket("localhost", 6789);
        receiveMeetingKey(meetingKey);
        socket.close();

        //receive second file and decrypt it with updated meeting key
        socket = new Socket("localhost", 6789);
        System.out.println("updated meeting key:");
        System.out.println(meetingKey);
        receiveFileLength();
        receiveFile("./files/receivedFile2.jpg");
        socket.close();

        //set physical key with updated physical key
        setPhysicalKey();
        System.out.println("updated physical key:");
        System.out.println(physicalKey);

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

    private void setPhysicalKey() throws UnsupportedEncodingException {
        PhysicalKey phyKey = new PhysicalKey();
        physicalKey = phyKey.getPhysicalKey(secretKey,user_number);

    }

    private void receiveMeetingKey(String key)throws Exception{
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        AES aes = new AES();
        int length = input.read();
        byte[] dataArray = new byte[length];
        int count = input.read(dataArray,0,length);
        byte[] in = aes.decrypt(key,dataArray);
        meetingKey = new String(in,"UTF-8");

    }

    private void receiveFile(String filePath)throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        File file = new File(filePath);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] b = new byte[length];

        int i = 0;
        for(i = 0; i < length; i++) {
            try {

            b[i] = dis.readByte();
            }
            catch (EOFException e)
            {
                break;
            }
        }

        AES aes = new AES();
        decrypt_file = aes.decrypt(meetingKey ,b);
        fos.write(decrypt_file, 0 , decrypt_file.length);
        System.out.println("file received successfully");
        }


    private void receiveFileLength() throws IOException {
        input = socket.getInputStream();
        inReader = new InputStreamReader(input);
        br = new BufferedReader(inReader);
        length = br.read();
    }

    public static void main(String[] args) throws Exception{
           ClientFile1 clientFile = new ClientFile1();
           clientFile.clientFile();
    }

}
