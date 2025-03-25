import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ConnectionToServer implements Runnable
{
    ClientTalker talker;
    String protocol;
    Thread thread;
    String username;
    String password;
    String[] strings;
    String status;
    String[] friends;

    ConnectionToServer(Socket socket)
    {
        try
        {
            talker = new ClientTalker(socket);

        } catch (IOException ioe)
        {
            System.out.println("IO Exception in ConnectionToClient()");
        }

        thread = new Thread(this);
        thread.start();
    }


    public void run()
    {
        String str;

        if (protocol.equals("REGISTER"))
        {
            register();


        }
        else if (protocol.equals("LOGIN"))
        {
            login();

        }

        try
        {
            str = talker.receive();
            while (!str.startsWith("TERMINATE"))
            {

                if (str.startsWith("INCOMING_FRIEND"))
                {
                    String receiver;
                    String sender;

                    strings = str.split(" ");
                    receiver = strings[1];
                    sender = strings[2];


                    if (username.equals(receiver))
                    {
                       // System.out.println("Right under username.equals(receiver");

                        int option = JOptionPane.showConfirmDialog(null,
                                sender + " would like to be your friend. Do you accept?", "Friend Request", JOptionPane.YES_NO_OPTION);

                        try
                        {
                            if (option == JOptionPane.YES_OPTION)
                            {
                                talker.send("YES");
                            }
                            else if (option == JOptionPane.NO_OPTION)
                            {
                                talker.send("NO");
                            }
                        }
                        catch (IOException ioe)
                        {
                            System.out.println("IOException in run() in friend section");
                        }
                    }
                    else
                    {
                        System.out.println("This is not the friend request recipient.");
                    }


                }
                else if (str.startsWith("NO_USER"))
                {
                    strings = str.split(" ");
                    String sender = strings[1];

                    if (username.equals(sender))
                        JOptionPane.showMessageDialog(null, "Username does not exist.");
                }
                else if(str.startsWith("MSG"))
                {
                    String msg;
                    String sender;
                    strings = str.split(" ", 3);

                    sender = strings[1];
                    msg = strings[2];

                    FrameClass.message(msg,sender);
                }
                else if(str.startsWith("FILE"))
                {
                    incomingFileRequest(str);
                }
                else if(str.equals("DENIED"))
                {
                    JOptionPane.showMessageDialog(null, "Your file was denied.");
                }
                else if (str.startsWith("IP"))
                {
                    String tempName;
                    String portString;
                    String ipString;
                    String fileName;
                    byte[] buffer;
                    int numBytesRead;

                    Socket babySocket;
                    FileInputStream inStream;
                    DataOutputStream outStream;

                    strings = str.split(" ");
                    tempName = strings[1];
                    portString = strings[2];
                    ipString = strings[3];
                    fileName = strings[4];

                    babySocket = new Socket(ipString, Integer.parseInt(portString));
                    inStream = new FileInputStream(fileName);
                    outStream = new DataOutputStream(babySocket.getOutputStream());

                     buffer = new byte[64];
                     numBytesRead = inStream.read(buffer);
                     outStream.write(buffer, 0, numBytesRead);

                     while(numBytesRead > 0)
                     {
                         numBytesRead = inStream.read(buffer);
                         outStream.write(buffer, 0, numBytesRead);
                     }

                     inStream.close();
                     outStream.close();
                     babySocket.close();
                }



                str = talker.receive();
            }
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in run()");
        }

    }


    public void register()
    {
        try
        {
            String str;

            talker.send("REGISTER " + username + " " + password);
            //talker.send(username);

            str = talker.receive(); // OK if good, USED if taken, SPACE if spaces

            if(str.equals("OK"))
            {
                //talker.send(password);
                str = talker.receive(); // OK if no space, SPACE if space

                if(str.equals("OK"))
                {
                    JOptionPane.showMessageDialog(null, "Successful registry.");
                }
                else if (str.equals("SPACE"))
                {
                    username = null;
                    JOptionPane.showMessageDialog(null, "Password can not contain spaces. Try again.");
                }
            }
            else if(str.equals("USED"))
            {
                username = null;
                JOptionPane.showMessageDialog(null,"Username is already taken. Try again.");
            }
            else if(str.equals("SPACE"))
            {
                username = null;
                JOptionPane.showMessageDialog(null, "Username can not contain spaces. Try again.");
            }

            status = str;


        }
        catch (IOException ioe)
        {
            System.out.println("IOException in CTS - register()");
        }


    }

    public void login()
    {
        try
        {
            String str;

            talker.send("LOGIN " + username + " " + password);

            str = talker.receive(); // OK , WRONG or ONLINE

            if(str.equals("OK"))
            {
                JOptionPane.showMessageDialog(null, "Login successful.");
            }
            else if(str.equals("WRONG"))
            {
                username = null;
                JOptionPane.showMessageDialog(null, "Credentials do not match. Try again.");
            }
            else if(str.equals("ONLINE"))
            {
                username = null;
                JOptionPane.showMessageDialog(null,"User is already online.");
            }

            status = str;
        }
        catch (IOException ioe)
        {
            System.out.println("IOException in CTS - login()");
        }
    }

    public void sendFriendRequest(String username)
    {
        try
        {

            talker.send("FRIEND " + username + " " + this.username);



        }
        catch(IOException ioe)
        {
            System.out.println("IOException in sendFriendRequest()");
        }


    }

    public void sendInfo(String username, String password, String protocol)
    {
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        talker.id = username;
    }

    public String[] getFriends()
    {
        try
        {
            String str;
            talker.send("GIVE_FRIENDS");
            str = talker.receive(); //string with friends
            friends = str.split(" ");
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in getFriends()");
        }

        return friends;
    }

    public void sendMessage(String msg, String recipient)
    {
        try
        {
            talker.send("MSG " + username + " " + recipient + " " + msg);
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in sendMessage()");
        }

    }
    public void requestFileSend(String recipient, String fileName, String fileSize)
    {
        try
        {
            talker.send("FILE "  + recipient + " " + fileName + " " + fileSize);
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in requestFileSend()");
        }
    }
    public void incomingFileRequest(String str)
    {
        String tempName;
        String fileName;
        String fileSize;
        int babyPort;
        Random r;

        strings = str.split(" ");

        tempName = strings[1];
        fileName = strings[2];
        fileSize = strings[3];

        int option = JOptionPane.showConfirmDialog(null, tempName +
                " would like to send you a file.\n File name: " + fileName + " \n File size: " + fileSize + "\n Do you accept?",
                "File Request", JOptionPane.YES_NO_OPTION);

        try
        {
            if (option == JOptionPane.YES_OPTION)
            {
                BabyServer babyServer;

                r = new Random();

                babyPort = r.nextInt(4000) + 4000;
                babyServer = new BabyServer(babyPort, fileName);

                talker.send("ACCEPT " + tempName + " " + babyPort + " " + fileName);

            }
            else
            {
                talker.send("DENIED " + username);
            }
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in incomingFileRequest()");
        }

    }

}