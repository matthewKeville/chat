import java.awt.event.*;
import javax.swing.*;   
import java.util.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.CardLayout;
import javax.swing.Timer;
import java.io.*;
import java.util.*;
import java.net.*;
public class GuiClient {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private JList<String> serverList;
    private JList<String> chatList;
    private DefaultListModel<String> chatListModel;
    private JFrame frame;
    private JPanel master; //ties together different pages
    private JPanel loginPanel; //master pannel for login page
    private JPanel chatPanel; //master pannel for chat page
    private JPanel loginButtonPanel;

    private JScrollPane serverBrowser;
    private JScrollPane chatBrowser;
    private JLabel label;
    private JTextField usernameField;
    private JTextField messageField;
    private JButton connectButton,exitButton,switchButton;
    private CardLayout cardLayout; //for master panel

    private JButton sendMessageButton;
    
    private Font font1;
    private Timer chatUpdateTimer;

    //net stuff
    private static String serverName;
    private static int port;
    private static String userName = "";
    private static Vector<Message> localChat = new Vector<Message>();

    static Socket client = null;
    //create streams
    static OutputStream outToServer = null;
    static InputStream inFromServer = null; 
    static ObjectOutputStream objectOut = null;
    static ObjectInputStream objectIn = null; 



    //construct the login panel
    private void makeLoginPanel(){
       //create a panel
        loginPanel = new JPanel();
        //set panel layout 
        //BoxLayout is just column or row layout
        loginPanel.setLayout(new BoxLayout(loginPanel,BoxLayout.Y_AXIS));
        loginPanel.setBackground(Color.BLACK);
        //Border blackLineBorder = BorderFactory.createLineBorder(Color.black);

         //Add a JList for the server browswer
        //default list model is immutable , to add content dynamically we must
        //change model -> setModel() DefaultListModel is mutable
        String[] fakeServers = new String[]{"tokyo","sand","rayaki"};
        serverList = new JList<String>(fakeServers); 
        serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        serverList.setLayoutOrientation(JList.VERTICAL); 
        serverList.setSelectionBackground(Color.BLACK); //back selected
        serverList.setSelectionForeground(Color.RED); //text selected
        serverList.setForeground(Color.GREEN); //text
        serverList.setBackground(Color.BLACK); //back
        serverList.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        serverBrowser = new JScrollPane(serverList); 
        serverBrowser.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        //embed the list in a scroll pane
        usernameField = new JTextField(12);
        usernameField.setFont(font1);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        usernameField.setForeground(Color.GREEN);
        usernameField.setBackground(Color.BLACK);
        //usernameField.setPreferredSize(new Dimension(40,20));
        //connect button
        connectButton = new JButton("Connect");
        connectButton.setActionCommand("enter");
        connectButton.setBackground(Color.BLACK); 
        connectButton.setForeground(Color.YELLOW);      
        connectButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));    
        connectButton.addActionListener(new ButtonClickListener()); 
        connectButton.setPreferredSize(new Dimension(120,60));
 
        exitButton = new JButton("Exit");
        exitButton.setActionCommand("exit");
        exitButton.setBackground(Color.BLACK); 
        exitButton.setForeground(Color.YELLOW);      
        exitButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));     
        exitButton.addActionListener(new ButtonClickListener());
        exitButton.setPreferredSize(new Dimension(120,60));

        switchButton = new JButton("Switch");
        switchButton.setActionCommand("switch");
        switchButton.setBackground(Color.BLACK); 
        switchButton.setForeground(Color.YELLOW);      
        switchButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));   
        switchButton.addActionListener(new ButtonClickListener());       
        switchButton.setPreferredSize(new Dimension(120,60));
    
        loginButtonPanel = new JPanel();
        loginButtonPanel.setLayout(new BoxLayout(loginButtonPanel,BoxLayout.X_AXIS));
        loginButtonPanel.setBackground(Color.BLACK);

        loginButtonPanel.add(connectButton);
        loginButtonPanel.add(Box.createHorizontalStrut(20)); //add spacing 
        loginButtonPanel.add(exitButton);
        loginButtonPanel.add(Box.createHorizontalStrut(20)); //add spacing 
        loginButtonPanel.add(switchButton);

 
        loginPanel.add(serverBrowser);
        loginPanel.add(usernameField);
        loginPanel.add(loginButtonPanel);
 
    }   

    private void makeChatPanel() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.BLACK);

        String[] fakeChat = new String[]{"Dad: hello","Sandy: what it do","Tom : rayaki"};
        chatList = new JList<String>(fakeChat); 
        chatList.setModel(new DefaultListModel<String>());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        chatList.setLayoutOrientation(JList.VERTICAL); 
        chatList.setSelectionBackground(Color.BLACK); //back selected
        chatList.setSelectionForeground(Color.RED); //text selected
        chatList.setForeground(Color.GREEN); //text
        chatList.setBackground(Color.BLACK); //back
        chatList.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        chatUpdateTimer = new Timer(1000*1,new chatUpdateListener());
        chatUpdateTimer.start();

        //chatList.setActionCommand("chatUpdate");
        //chatList.addActionListener(new chatUpdateListener());

        chatBrowser = new JScrollPane(chatList); 
        chatBrowser.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        messageField = new JTextField(12);
        messageField.setFont(font1);
        messageField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        messageField.setForeground(Color.GREEN);
        messageField.setBackground(Color.BLACK);

        //consider changing this to on Enter 
        sendMessageButton = new JButton("send");
        sendMessageButton.setActionCommand("send");
        sendMessageButton.setBackground(Color.BLACK); 
        sendMessageButton.setForeground(Color.YELLOW);      
        sendMessageButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));    
        sendMessageButton.addActionListener(new ButtonClickListener()); 
        sendMessageButton.setPreferredSize(new Dimension(120,60));
 


        //JLabel testerino = new JLabel("this is a test");
        //chatPanel.add(testerino);
        chatPanel.add(chatBrowser);
        chatPanel.add(messageField);
        chatPanel.add(sendMessageButton);
    }
 
    private void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("Graphical Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font1 = new Font("SansSerif",Font.BOLD,10); 

        makeLoginPanel();
        makeChatPanel();
        
        //masterPanel is card layout
        master = new JPanel();
        cardLayout = new CardLayout();
        master.setLayout(cardLayout);

        master.add(loginPanel,"1"); 
        master.add(chatPanel,"2");

        frame.add(master);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public GuiClient() {
        createAndShowGUI();
    }

    public static void main(String[] args) {

        //process command line args
        serverName = args[0];
        port = Integer.parseInt(args[1]);
        //establish network socket
        try {
         System.out.println("Connecting to " + serverName + " on port " + port);
         //attempt connection
         client = new Socket(serverName, port);
         //create streams
         outToServer = client.getOutputStream();
         inFromServer = client.getInputStream();
         //create object outputstream
         //ObjectOutputStream objectOut = new ObjectOutputStream(outToServer);
         objectOut = new ObjectOutputStream(outToServer);
         objectIn = new ObjectInputStream(inFromServer);
        } catch (IOException e)  {
            System.out.println("failed to connect to server");
        } 

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GuiClient gc = new GuiClient();
            }
        });

    }


    //event handler class for button
    private  class ButtonClickListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         if( command.equals( "enter" ))  {
            //get value of selected server from JList
            String selected = (String) serverList.getSelectedValue();
            //set username
            userName = usernameField.getText();
            System.out.println("connecting " + userName + "@" + selected + " ... "); 
         } else if( command.equals( "exit" ) )  {
            System.exit(0);
         } else if ( command.equals("switch")){
            cardLayout.show(master,"2"); 
         } else if ( command.equals("send")) {
            //write packet to server
            Message message = new Message(userName,messageField.getText());
            localChat.add(message);
            Packet mpack = new Packet(1,message);
            try {
                objectOut.writeObject(mpack);
                objectOut.flush();
            } catch (IOException exc) {
                System.out.println("Error sending message");
            }
            messageField.setText("");
            //for QOS my local List of Messages should be updated
            //as soon as my message is sent
         } 
           else {
         } 	
      }		
    }

    //event handler for chatUpdateTimer
    private  class chatUpdateListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         //if the chat update takes to long consider scheduling 
         //the action in a swingWorker background thread
         //get value of selected server from JList
        System.out.println("Updating chat"); 
        //String[] newChat = new String[]{"new","messages","are","cool"};
        //chatList.setListData(newChat);


        try {
            //signal to server request for new chat log
            Packet upack = new Packet(2);
            objectOut.writeObject(upack);
            objectOut.flush();
            //read in chat object
            Packet serverPack = (Packet) objectIn.readObject();
            if (serverPack.getControl() != 3) {
                System.out.println("this is fucked");
                System.exit(1);
            }
            Vector<Message> messageVector = (Vector<Message>) serverPack.getPayload();
            //convert vector of messages to vector of strings
            String[] unpackedMessages = new String[messageVector.size()];
            for (int i = 0; i < messageVector.size(); i++ ) {
                unpackedMessages[i] = messageVector.get(i).toString();
            }
            chatList.setListData(unpackedMessages);               
 
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            System.out.println("Yo is update getting fucked up?");
        }












      }		
    }


}

