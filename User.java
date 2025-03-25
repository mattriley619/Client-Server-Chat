import java.io.DataOutputStream;
import java.util.Vector;
import java.io.DataInputStream;

public class User
{
    String username;
    String password;
    Vector<String> listOfFriends;
    ConnectionToClient ctc;

    User(String username, String password, ConnectionToClient ctc)
    {
        this.username = username;
        this.password = password;
        this.ctc = ctc;

        listOfFriends = new Vector<>();
    }

    User(DataInputStream dis) throws java.io.IOException
    {
        username = dis.readUTF();
        password = dis.readUTF();

        int num = dis.readInt();

        listOfFriends = new Vector<>();

        for(int i = 0; i < num; i++)
        {
            listOfFriends.addElement(dis.readUTF());
        }
    }

    void store(DataOutputStream dos) throws java.io.IOException
    {
        dos.writeUTF(username);
        dos.writeUTF(password);

        dos.writeInt(listOfFriends.size());

        for(int i = 0; i < listOfFriends.size(); i++)
        {
            dos.writeUTF(listOfFriends.elementAt(i));
        }
    }

    @Override
    public String toString()
    {
        return (username + " " + password + " " + listOfFriends.toString());
    }





}
