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
public class BrowserPanel extends JPanel {
    private JTextField usernameField,passwordField;
    private JButton connectButton,exitButton;
    private JPanel browserButtonPanel;

    private JList<String> serverList;
    private JScrollPane serverBrowser;

    private Timer browserUpdateTimer;

    private Font font1;
    private CardLayout cardLayout;
    private JPanel master; //need reference to masters's card layout

    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;

    private List<HashMap<String,Object>> chatRoomHeaderList = new ArrayList<HashMap<String,Object>>();

    public BrowserPanel(CardLayout cardLayout,JPanel master,ObjectOutputStream out,ObjectInputStream in) {
        super();
        this.master = master;
        this.cardLayout = cardLayout;

        this.objectOut = out;
        this.objectIn = in;

        Font font1 = new Font("SansSerif",Font.BOLD,10); 

        //create a this
        //BoxLayout is just column or row layout
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBackground(Color.BLACK);

        //Add a JList for the server browswer
        String[] emptyChatHeaderStrings = new String[10];
        serverList = new JList<String>(emptyChatHeaderStrings); 
        serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        serverList.setLayoutOrientation(JList.VERTICAL); 
        serverList.setSelectionBackground(Color.BLACK); //back selected
        serverList.setSelectionForeground(Color.RED); //text selected
        serverList.setForeground(Color.GREEN); //text
        serverList.setBackground(Color.BLACK); //back
        serverList.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        serverBrowser = new JScrollPane(serverList); 
        serverBrowser.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        browserUpdateTimer = new Timer(1000*5,new browserUpdateListener());
        browserUpdateTimer.start();

        //connect button
        connectButton = new JButton("Connect");
        connectButton.setActionCommand("connect");
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

        browserButtonPanel = new JPanel();
        browserButtonPanel.setLayout(new BoxLayout(browserButtonPanel,BoxLayout.X_AXIS));
        browserButtonPanel.setBackground(Color.BLACK);
        browserButtonPanel.add(connectButton);
        browserButtonPanel.add(Box.createHorizontalStrut(20)); //add spacing 
        browserButtonPanel.add(exitButton);
        browserButtonPanel.add(Box.createHorizontalStrut(20)); //add spacing 
 
        this.add(serverBrowser);
        this.add(browserButtonPanel);
 
    }   
 
    //event handler class for button
    //should break this up for the various panes (screens)
    private  class ButtonClickListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         if( command.equals( "connect" ))  {
            //begin chat room enter sequence
            try {
             synchronized(objectIn) {
              synchronized(objectOut) {
               //retrieve selected room id
               int selectedRoomHeaderIndex = serverList.getSelectedIndex();  
               System.out.println(chatRoomHeaderList.size());
               System.out.println(chatRoomHeaderList.toString());
               String selectedRoomId = (String) chatRoomHeaderList.get(selectedRoomHeaderIndex).get("id"); 
               objectOut.writeInt(6);
               objectOut.writeObject(selectedRoomId);
               objectOut.flush();
               
               int control = objectIn.readInt(); 
               if (control == 60) {
                System.out.println("Room is full");
               
               } else if (control == 61) {
                System.out.println("success");
                //change state
                GuiClient.currentState = State.CHAT;
                cardLayout.show(master,"chatPanel");
               } else if (control == 62) {
                System.out.println("Not a member");
               } else if (control == 63) {
                System.out.println("You are banned from joining this room");
               }
              }
             }
            } catch (IOException exc) {
                System.out.println("Error attempting to join room");
                System.out.println(exc.getMessage());
            }             


            
         } else if( command.equals("exit"))  {
            //switch card to createAccountPanel
         } 
      }     
    }

    //update server browser list
    //event handler for chatUpdateTimer
    //this might  trigger to often for it to complete
    private  class browserUpdateListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
        //only update if in browser panel
        if (GuiClient.currentState == State.BROWSER) { 
                String command = e.getActionCommand();  
                System.out.println("Updating Browser");
                try {
                    synchronized(objectOut) {
                     synchronized(objectIn)  {
                            objectOut.writeInt(5); //request browser update
                            objectOut.flush(); 
                            int control = objectIn.readInt(); 
                     
                    if (control == 56) {//56 -> browserUpdate success

                        //unpack chatRoomHeader list
                        //ArrayList<HashMap<String,Object>>  chatRoomHeaderList; 
                        chatRoomHeaderList = (ArrayList<HashMap<String,Object>>) objectIn.readObject();
                        System.out.println(chatRoomHeaderList.toString());
                        //convert list of room heaers to array of strings
                        String[] headerStrings = new String[chatRoomHeaderList.size()];
                        Iterator headerIterator = chatRoomHeaderList.iterator();
                        int i = 0;
                        while (headerIterator.hasNext()) {
                            HashMap<String,Object> header = (HashMap<String,Object>) headerIterator.next();
                            String h = "" + header.get("name") + " : " + header.get("occupancy") + " / " + header.get("capacity");
                            headerStrings[i] = h;
                            i++;
                        }
                        serverList.setListData(headerStrings);    
                        System.out.println("update succesfull");


                    } else if (control == 57) { //browser update fail
                        System.out.println("browser update failed");
                        System.exit(1);
                    } else {
                        System.out.println("recieved invalid response on browser update requeste");
                    }
                    }
                   }


                    
                } catch (Exception exc) {
                    System.out.println(exc.getMessage());
                    System.out.println("Browser update failed");
                }
        }
        //do nothing 
       }
    }   
    


}
