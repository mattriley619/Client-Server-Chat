import java.io.*;
import java.net.*;




public class ServerTalker
{
    BufferedReader inStream;
    DataOutputStream outStream;




    ServerTalker(Socket normalSocket) throws IOException
    {
        inStream = new BufferedReader(new InputStreamReader(normalSocket.getInputStream()));
        outStream = new DataOutputStream(normalSocket.getOutputStream());
    }

    void send(String msg) throws IOException
    {
        outStream.writeBytes(msg + "\n");
        System.out.println("Server: " + msg);
    }

    String receive() throws IOException
    {
        String str;
        str = inStream.readLine();
        System.out.println("Client: " + str);
        return str;
    }






}