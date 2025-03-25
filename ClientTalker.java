import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;


public class ClientTalker
{
    Socket socket;
    BufferedReader inStream;
    DataOutputStream outStream;
    String id;


    ClientTalker(Socket socket) throws IOException
    {
        this.socket = socket;


        inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new DataOutputStream(socket.getOutputStream());

    }



    void send(String msg) throws IOException
    {
        outStream.writeBytes(msg + "\n");
        System.out.println("Client: " + msg);
    }

    String receive() throws IOException
    {
        String str;
        str = inStream.readLine();
        System.out.println("Server: " + str);
        return str;
    }



}