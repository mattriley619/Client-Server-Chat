import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class FrameClass extends JFrame
                        implements ActionListener, ListSelectionListener, DropTargetListener
{
    JButton loginButton;
    JButton registerButton;
    JButton cancelButton;
    JButton setupButton;
    JButton addFriendButton;
    JButton friendSubmitButton;
    JTextField portField;
    JTextField ipField;
    JTextField userField;
    JTextField passField;
    JTextField addFriendField;
    String port;
    String ip;
    String username;
    String password;
    Properties properties;

    FileOutputStream outputStream;
    FileInputStream inputStream;
    File file;
    JDialog dialog;
    JDialog addFriendDialog;
    String protocol; // = REGISTER or LOGIN
    ConnectionToServer cts;
    JButton chatButton;
    JDialog chatDialog;
    JEditorPane editorPane;
    JScrollPane chatScrollPane;
    JTextArea textArea;
    JButton sendButton;
    JList jList;
    String[] friends;
    static JButton testButton;
    static String msg;
    static String sender;

    DropTarget dropTarget;






    FrameClass()
    {
        properties = new Properties();

        try
        {
            file = new File("cookies.txt");
            outputStream = new FileOutputStream(file.getName(), true);
            inputStream = new FileInputStream(file.getName());
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println("FileNotFoundException in FrameClass()");
        }

        openDialog();

        setupButton = new JButton("Login/Register");
        setupButton.addActionListener(this);
        this.add(setupButton,BorderLayout.SOUTH);
        setupButton.setVisible(true);

        addFriendButton = new JButton("Add Friend");
        addFriendButton.addActionListener(this);
        this.add(addFriendButton, BorderLayout.EAST);
        addFriendButton.setVisible(true);

        chatButton = new JButton("Chat");
        chatButton.addActionListener(this);
        this.add(chatButton, BorderLayout.NORTH);
        chatButton.setEnabled(false);
        chatButton.setVisible(true);

        FrameClass.testButton = new JButton();
        FrameClass.testButton.addActionListener(this);
        FrameClass.testButton.setVisible(false);

        setupMainFrame();
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == loginButton)
        {
            protocol = "LOGIN";
            handleSetup(protocol);
            setTitle(username);
        }
        else if(e.getSource() == registerButton)
        {
            protocol = "REGISTER";
            handleSetup(protocol);
        }
        else if(e.getSource() == cancelButton)
        {
            cts.username = null;
            dialog.dispose();
        }
        else if(e.getSource() == setupButton)
        {
            if(cts.username == null)
                 openDialog();
            else
            {
                int option = JOptionPane.showConfirmDialog(null,
                        "You are already logged in. Would you like to log out?", "Logout?", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION)
                {
                    cts.username = null;
                    try
                    {
                        cts.talker.send("TERMINATE");
                    }
                    catch(IOException ioe)
                    {
                        System.out.println("IOException in actionPerformed() sending TERMINATE");
                    }
                }

            }
            setTitle(username);
        }
        else if(e.getSource() == addFriendButton)
        {
            if(cts.username != null)
            {
                addFriend();
            }
            else
                JOptionPane.showMessageDialog(null,"You must be logged in for this.");
        }
        else if (e.getSource() == friendSubmitButton)
        {
            if(username.equals(addFriendField.getText().trim()))
            {
                JOptionPane.showMessageDialog(null, "You cannot add yourself as a friend.");
            }
            else if(addFriendField.getText().trim().equals(""))
            {
                JOptionPane.showMessageDialog(null, "Username cannot be empty.");
            }
            else
            {
                cts.sendFriendRequest(addFriendField.getText().trim());
                JOptionPane.showMessageDialog(null, "Friend request sent.");
                jList.setListData(friends);
            }
        }
        else if (e.getSource() == chatButton)
        {
            openChatDialog(jList.getSelectedValue().toString());
           // jList.setListData(friends);
        }
        else if (e.getSource() == sendButton)
        {
            addText("<div align = \"right\">"            +
                        "<font color = \"gray\">"       +
                        textArea.getText()              +
                        "</font></div>");
            cts.sendMessage(textArea.getText(), jList.getSelectedValue().toString());
        }
        else if (e.getSource() == FrameClass.testButton)
        {
            jList.setSelectedValue(FrameClass.sender, true);

            if(chatDialog == null || !chatDialog.isVisible())
                chatButton.doClick();

            chatDialog.requestFocus();
            addText("<div align = \"left\">"        +
                    "<font color = \"purple\">"     +
                    FrameClass.msg                  +
                    "</font></div>");
        }



    }

    public void valueChanged(ListSelectionEvent e)
    {
        chatButton.setEnabled(jList.getSelectedIndices().length == 1);
    }

    void setupMainFrame()
    {
        Toolkit tk;
        Dimension d;

        if(username != null)
            setTitle(username);
        else
            setTitle("Chat");

        tk = Toolkit.getDefaultToolkit();
        d = tk.getScreenSize();
        setSize(d.width / 2, d.height / 2);
        setLocation(d.width / 4, d.height / 4);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }


    void openDialog()
    {
        GroupLayout layout;

        Toolkit tk;
        Dimension d;

        JLabel portLabel;
        JLabel ipLabel;
        JLabel userLabel;
        JLabel passLabel;

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        dialog = new JDialog(this, "Setup", true);
        layout = new GroupLayout(dialog.getContentPane());

        //dialog.getContentPane().add(submitButton, BorderLayout.SOUTH);

        portField = new JTextField();
        ipField = new JTextField();
        userField = new JTextField();
        passField = new JTextField();

        portLabel = new JLabel("Port Number");
        ipLabel = new JLabel("IP Address");
        userLabel = new JLabel("Username (optional)");
        passLabel = new JLabel("Password (optional)");

        dialog.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(portLabel).addComponent(ipLabel).addComponent(userLabel).addComponent(passLabel).addComponent(loginButton));
        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(portField).addComponent(ipField).addComponent(userField).addComponent(passField).addComponent(registerButton));
        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(cancelButton));
        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(portLabel).addComponent(portField));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(ipLabel).addComponent(ipField));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(userLabel).addComponent(userField));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(passLabel).addComponent(passField));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(loginButton).addComponent(registerButton).addComponent(cancelButton));
        layout.setVerticalGroup(vGroup);





        tk = Toolkit.getDefaultToolkit();
        d = tk.getScreenSize();
        dialog.setSize(d.width / 2, d.height / 3);
        dialog.setLocation(d.width / 4, d.height / 4);

        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        try
        {
            properties.load(inputStream);
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in FrameClass() - openDialog()");
        }


        if(properties.get("port") != null)
        {
            portField.setText(properties.get("port").toString());
            ipField.setText(properties.get("ip").toString());
            if(properties.get("user") != null)
            {
                userField.setText(properties.get("user").toString());
                passField.setText(properties.get("pass").toString());
            }
        }

        loginButton.setVisible(true);
        registerButton.setVisible(true);
        cancelButton.setVisible(true);
        dialog.setVisible(true);
    }

    void handleSetup(String protocol)
    {
        port = portField.getText();
        ip = ipField.getText();
        username = userField.getText();
        password = passField.getText();

        properties.put("port", port);
        properties.put("ip", ip);
        properties.put("user", username);
        properties.put("pass", password);

        try
        {
            properties.store(outputStream, "Port, IP");
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in handleSetup() while storing properties");
        }


        try
        {
            cts = new ConnectionToServer(new Socket(ip, Integer.parseInt(port)));
            cts.sendInfo(username, password, protocol);
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in handleSetup() during cts construction");
        }

        if(cts.username != null)
        {
            dialog.dispose();
            friends = cts.getFriends();
            if(friends != null)
            {
                jList = new JList(friends);
                jList.addListSelectionListener(this);
                this.add(jList);
                jList.setVisible(true);
            }

        }
        setTitle(username);


    }

    void addFriend()
    {
        Toolkit tk;
        Dimension d;

        addFriendDialog = new JDialog(this, "Add Friend", true);

        addFriendField = new JTextField("Enter username of friend to add");

        friendSubmitButton = new JButton("Submit");
        friendSubmitButton.addActionListener(this);

        addFriendDialog.add(addFriendField, BorderLayout.NORTH);
        addFriendDialog.add(friendSubmitButton, BorderLayout.SOUTH);

        tk = Toolkit.getDefaultToolkit();
        d = tk.getScreenSize();
        addFriendDialog.setSize(d.width / 4, d.height / 4);
        addFriendDialog.setLocation(d.width / 4, d.height / 4);
        addFriendDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addFriendField.setVisible(true);
        friendSubmitButton.setVisible(true);
        addFriendDialog.setVisible(true);



    }

    void openChatDialog(String friend)
    {
        Toolkit tk;
        Dimension d;

        chatDialog = new JDialog(this, username, false);

        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        chatScrollPane = new JScrollPane(editorPane);
       // chatDialog.add(editorPane);
        chatDialog.add(chatScrollPane);

        textArea = new JTextArea();
        textArea.setText("The area is here");
        textArea.setCaretColor(Color.GRAY);
        chatDialog.add(textArea, BorderLayout.SOUTH);

        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        chatDialog.add(sendButton, BorderLayout.EAST);

        editorPane.setText(
                "<div align = \"center\">"              +
                        "<font color = \"green\">"      +
                        friend                           +
                        "</font>"                       +
                        "</div>");



        tk = Toolkit.getDefaultToolkit();
        d = tk.getScreenSize();
        chatDialog.setSize(d.width / 2, d.height / 3);
        chatDialog.setLocation(d.width / 4, d.height / 4);

        chatDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dropTarget = new DropTarget(chatDialog, this);

        editorPane.setVisible(true);
        textArea.setVisible(true);
        sendButton.setVisible(true);
        chatDialog.setVisible(true);
    }

    void addText(String text)
    {
        HTMLDocument doc;
        Element html;
        Element body;

        doc = (HTMLDocument) editorPane.getDocument();
        html = doc.getRootElements()[0];
        body = html.getElement(1);

        try
        {
            doc.insertBeforeEnd(body,text);
            editorPane.setCaretPosition(editorPane.getDocument().getLength());
        }
        catch(Exception e)
        {
            System.out.println("Error inserting text");
        }


    }

    static void message(String msg, String sender)
    {
        FrameClass.msg = msg;
        FrameClass.sender = sender;
        FrameClass.testButton.doClick();
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {

    }
    public void dragExit(DropTargetEvent dte)
    {

    }
    public void dragOver(DropTargetDragEvent dtde)
    {

    }
    public void drop(DropTargetDropEvent dtde)
    {
        java.util.List<File> fileList;
        Transferable transferableData;


        transferableData = dtde.getTransferable();

        try
        {
            if(transferableData.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                fileList = (java.util.List<File>) (transferableData.getTransferData(DataFlavor.javaFileListFlavor));

                if(fileList.size() == 1)
                {
                    cts.requestFileSend(jList.getSelectedValue().toString(), fileList.get(0).getName(), Long.toString((fileList.get(0).getTotalSpace())));
                }
                else
                    JOptionPane.showMessageDialog(null, "One file at a time.");
            }
        }
        catch(UnsupportedFlavorException ufe)
        {
            System.out.println("Unsupported flavor.");
        }
        catch(IOException ioe)
        {
            System.out.println("IOException in drop()");
        }


    }
    public void dropActionChanged(DropTargetDragEvent dtde)
    {

    }



}
