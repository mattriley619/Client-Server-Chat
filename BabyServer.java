import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BabyServer implements Runnable
{

    ServerSocket serverSocket;
    Socket normalSocket;
    DataInputStream inStream;
    FileOutputStream outStream;
    byte[] buffer;
    int numBytesRead;
    int port;
    String fileName;
    Thread thread;


    BabyServer(int port, String fileName)
    {
        this.port = port;
        this.fileName = fileName;

        thread = new Thread(this);
        thread.start();
    }

    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("BabyServer started. Waiting for client connection...");

            outStream = new FileOutputStream(fileName);

            normalSocket = serverSocket.accept();
            System.out.println("Client connected.");

            inStream = new DataInputStream(normalSocket.getInputStream());
            buffer = new byte[64];

            numBytesRead = inStream.read(buffer);
            outStream.write(buffer, 0 ,numBytesRead);

            while(numBytesRead > 0)
            {
                numBytesRead = inStream.read(buffer);
                outStream.write(buffer, 0 ,numBytesRead);
            }

            inStream.close();
            outStream.close();
            serverSocket.close();
            normalSocket.close();

            thread.interrupt();
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in babyServer()");
            thread.interrupt();
        }

    }


}
