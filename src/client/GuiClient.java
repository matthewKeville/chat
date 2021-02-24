package client;
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

    private final int PULSE_DELAY = 1000; //5 seconds

    private JFrame frame;
    private JPanel master; //ties together different pages
    private JPanel loginPanel, browserPanel, chatPanel,createAccountPanel;
    private CardLayout cardLayout; //for master panel

    //net stuff
    private Socket client = null;
    private OutputStream outToServer = null;
    private InputStream inFromServer = null; 

    private ObjectOutputStream objectOut = null;
    private ObjectInputStream objectIn = null; 

    //static net stuff
    private static String serverName = "localhost";
    private static int port = 6066;
    
    //static state stuff
    //replace with enum ..
    static State currentState = State.LOGIN;    

    private Timer pulseUpdateTimer;
    
    //what info does the client need?
    //how do we prevent the client from posing as other users?
    //should content


    private void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("Graphical Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font1 = new Font("SansSerif",Font.BOLD,10); 

        //masterPanel is card layout
        master = new JPanel();
        cardLayout = new CardLayout();
        master.setLayout(cardLayout);

        //loginPanel = makeLoginPanel();
        loginPanel = new LoginPanel(cardLayout,master,objectOut,objectIn);
        browserPanel = new BrowserPanel(cardLayout,master,objectOut,objectIn);
        chatPanel = new ChatPanel(cardLayout,master,objectOut,objectIn);
        createAccountPanel = new CreateAccountPanel(cardLayout,master,objectOut,objectIn);

        //update timer
        pulseUpdateTimer = new Timer(PULSE_DELAY,new pulseUpdateListener());
        pulseUpdateTimer.start();

        master.add(loginPanel,"loginPanel");
        master.add(createAccountPanel,"createAccountPanel");
        master.add(browserPanel,"browserPanel"); 
        master.add(chatPanel,"chatPanel");

        frame.add(master);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

    }
            
    public GuiClient() {
        connectToServer();
        createAndShowGUI();
    }

    public void connectToServer(){
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
    }

    public static void main(String[] args) {
        //process command line args
        serverName = args[0];
        port = Integer.parseInt(args[1]);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GuiClient gc = new GuiClient();
            }
        });

    }


         
    //event handler for chatUpdateTimer
    private  class pulseUpdateListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         System.out.println("sending connection pulse"); 
        try {
            //signal to server connection is active
            synchronized (objectOut ) {
             objectOut.writeInt(0);
             objectOut.flush();
            }
        } catch (Exception exc) {
            System.out.println("Error testing connection");
            System.out.println(exc.getMessage());
        }
      }		
    }


}

