import java.io.IOException;
import java.net.Socket;

public class ConnectionToClient implements Runnable
{
    Socket normalSocket;
    ServerTalker talker;
    Thread thread;
    UserTable userList;
    Boolean success = false;
    String str;
    String[] strings;
    String activeUsername;
    String receiver;
    String sender;



    ConnectionToClient(Socket normalSocket, UserTable userList)
    {
        this.normalSocket = normalSocket;
        this.userList = userList;

        try
        {
            talker = new ServerTalker(normalSocket);
        }
        catch(IOException ioe)
        {
            System.out.println("IO Exception in ConnectionToClient()");
        }



        thread = new Thread(this);
        thread.start();

    }







    public void run()
    {


        try
        {
            str = talker.receive();
            while(!str.startsWith("TERMINATE"))
            {

                //System.out.println("Message in run() top");


                if (str.startsWith("REGISTER"))
                {
                    register();
                }
                else if (str.startsWith("LOGIN"))
                {
                    login();
                }
                else if(str.startsWith("FRIEND"))
                {
                    friendRequest();
                }
                else if (str.equals("YES"))
                {
                    User receiverUser;
                    User senderUser;

                    receiverUser = (User) userList.get(receiver);
                    receiverUser.listOfFriends.addElement(sender);
                    userList.replace(receiver, receiverUser);

                    senderUser = (User) userList.get(sender);
                    senderUser.listOfFriends.addElement(receiver);
                    userList.replace(sender, senderUser);

                    userList.store();
                    System.out.println("End of YES condition");
                }
                else if(str.equals("GIVE_FRIENDS"))
                {
                    String friends = "";
                    User tempUser = userList.getUser(activeUsername);
                    for(int i = 0; i < tempUser.listOfFriends.size(); i++)
                    {
                        friends = friends.concat(tempUser.listOfFriends.elementAt(i) + " ");
                    }
                    talker.send(friends);
                }
                else if(str.startsWith("MSG"))
                {
                    strings = str.split(" ", 4);
                    sendMessage(strings);
                }
                else if(str.startsWith("FILE"))
                {
                    User tempUser;
                    String tempName;
                    String fileName;
                    String fileSize;
                    strings = str.split(" ");

                    tempName = strings[1];
                    tempUser = userList.getUser(tempName);
                    fileName = strings[2];
                    fileSize =(strings[3]);

                    tempUser.ctc.talker.send("FILE " + activeUsername + " " + fileName + " " + fileSize);
                }
                else if (str.startsWith("DENIED"))
                {
                    User tempUser;
                    String tempName;
                    strings = str.split(" ");

                    tempName = strings[1];
                    tempUser = userList.getUser(tempName);

                    tempUser.ctc.talker.send("DENIED");

                }
                else if (str.startsWith("ACCEPT"))
                {
                    String tempName;
                    String portString;
                    String fileName;
                    User tempUser;
                    String ip;
                    strings = str.split(" ");

                    tempName = strings[1];
                    portString = strings[2];
                    fileName = strings[3];
                    ip = normalSocket.getInetAddress().toString();

                    tempUser = userList.getUser(tempName);
                    tempUser.ctc.talker.send("IP " + activeUsername + " " + portString + " " + ip + " " + fileName);

                }

                str = talker.receive();

               // System.out.println("Message in run() right below receive");
            }
            User tempUser = userList.getUser(activeUsername);
            tempUser.ctc = null;
            userList.replace(tempUser.username, tempUser);
            System.out.println("User disconnected.");
            thread.interrupt();
        }
        catch(IOException ioe)
        {
            User tempUser = userList.getUser(activeUsername);
            tempUser.ctc = null;
            userList.replace(tempUser.username, tempUser);
            System.out.println("IO Exception in run() - (Most likely a user disconnected.)");
            thread.interrupt();

        }



    }

    public void register()
    {
        try
        {
            String tempName;
            String tempPassword;

            strings = str.split(" "); // username & password
            tempName = strings[1];
            tempPassword = strings[2];

            if(userList.unusedUsername(tempName))
            {
                if(!tempName.contains(" "))
                {
                    talker.send("OK");

                    if(!tempPassword.contains(" "))
                    {
                        talker.send("OK");
                        userList.addUser(new User(tempName, tempPassword, this));
                        activeUsername = tempName;
                        success = true;
                    }
                    else
                    {
                        talker.send("SPACE");
                    }
                }
                else
                {
                    talker.send("SPACE");
                }
            }
            else
            {
                talker.send("USED");
            }
        }
        catch(IOException ioe)
        {
            System.out.println("IO Exception in register()");
        }


    }

    public void login()
    {

        try
        {
            String tempName;
            String tempPass;
            User tempUser;

            strings = str.split(" ");

            tempName = strings[1];
            tempPass = strings[2];

            if(userList.containsKey(tempName))
            {
                if (userList.isOnline(tempName))
                {
                    talker.send("ONLINE");
                }
                else if (userList.credentialsMatch(tempName, tempPass))
                {
                    talker.send("OK");
                    //tempUser = new User(tempName,tempPass,this);
                    tempUser = userList.getUser(tempName);
                    tempUser.ctc = this;
                    userList.replace(tempName, tempUser);

                    activeUsername = tempName;

                }
                else
                {
                    talker.send("WRONG");
                }
            }
            else
                talker.send("WRONG");
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in CTC - login()");
        }



    }

    public void friendRequest()
    {
        User tempUser;


        strings = str.split(" ");
        receiver = strings[1];
        sender = strings[2];

        try
        {
            if(userList.containsKey(receiver))
            {

                if(userList.isOnline(receiver))
                {
                    tempUser = userList.getUser(receiver);

                    tempUser.ctc.receiver = receiver;
                    tempUser.ctc.sender = sender;
                    tempUser.ctc.talker.send("INCOMING_FRIEND " + receiver + " " + sender);

                    //str = tempUser.ctc.talker.receive();

                }
                else
                {
                    System.out.println(receiver + " is not online.");
                }



            }
            else
            {
                talker.send("NO_USER " + sender);
            }


        }
        catch(IOException ioe)
        {
            System.out.println("IOException in friendRequest()");
        }

    }

    public void sendMessage(String[] strings)
    {
        String sender = strings[1];
        String recipient = strings[2];
        String msg = strings[3];
        User tempUser = userList.getUser(recipient);

        try
        {
            tempUser.ctc.talker.send("MSG " + sender + " " + msg);
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in sendMessage()");
        }

    }


}