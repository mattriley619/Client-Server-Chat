import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer
{
    public static void main(String[] args)
    {
        ServerSocket serverSocket;
        Socket normalSocket;
        ConnectionToClient ctc;
        UserTable userList;
        FileInputStream inStream;
        FileOutputStream outStream;
        File file;


        try
        {
            serverSocket = new ServerSocket(4567);
            System.out.println("Server started. Waiting for client connection...");

            file = new File("userFile.txt");
            outStream = new FileOutputStream(file.getName(), true);

            if(file.length() > 0)
            {
                inStream = new FileInputStream(file.getName());
                userList = new UserTable(new DataInputStream(inStream), new DataOutputStream(outStream));
            }
            else
            {
                userList = new UserTable(new DataOutputStream(outStream));
            }

            while(true)
            {

                normalSocket = serverSocket.accept();
                System.out.println("Client connected.");

                ctc = new ConnectionToClient(normalSocket, userList);
            }
        }
        catch(IOException ioe)
        {
            System.out.println("IO Exception in main()");
        }








    }







}
