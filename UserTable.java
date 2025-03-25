import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.DataInputStream;


public class UserTable extends Hashtable
{
    DataOutputStream dos;

    UserTable(DataOutputStream dos)
    {
        this.dos = dos;


    }
    UserTable(DataInputStream dis, DataOutputStream dos) throws java.io.IOException
    {
        this.dos = dos;

        int size = dis.readInt();
        for (int i = 0; i < size; i++)
        {
            User user = new User(dis);
            this.put(user.username, user);

        }


    }

    void store() throws java.io.IOException
    {
        Enumeration<User> users;
        users = this.elements();

        dos = new DataOutputStream(new FileOutputStream("userFile.txt"));

        dos.writeInt(this.size());

        while(users.hasMoreElements())
        {
            users.nextElement().store(dos);
        }
    }


    boolean credentialsMatch(String username, String password)
    {
        User tempUser;

        if(this.containsKey(username))
        {
            tempUser = (User) this.get(username);

            if(tempUser.password.equals(password))
            {
                return true;
            }
        }



        return false;
    }

    boolean unusedUsername(String username)
    {
        return !this.containsKey(username); // returns true is username is not taken, and false if it is taken
    }

    void addUser(User user)
    {
        this.put(user.username, user);
        try
        {
            this.store();
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in UserTable - addUser()");
        }
    }

    boolean isOnline(String username)
    {
        User tempUser;

        tempUser = (User) this.get(username);

        if(tempUser.ctc == null)
            return false;



        return true;
    }

    User getUser(String username)
    {
        return (User) this.get(username);
    }








}
