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
public class ChatPanel extends JPanel {
 
    private JList chatList; 
    private JScrollPane chatBrowser;
    private JTextField messageField;
    private JButton sendMessageButton;
    //net stuff
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private CardLayout cardLayout;
    private JPanel master;

    private Timer chatUpdateTimer;
    private int UPDATE_DELAY = 100; // 1/10 seconds

    private Font font1;

    public ChatPanel(CardLayout cardLayout,JPanel master,ObjectOutputStream objectOut,ObjectInputStream objectIn) {
        super();
        this.master = master;
        this.cardLayout = cardLayout;
        this.objectOut = objectOut;
        this.objectIn = objectIn;


        Font font1 = new Font("SansSerif",Font.BOLD,10); 
        //set this layout 
        //BoxLayout is just column or row layout
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBackground(Color.BLACK);
        
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


        chatBrowser = new JScrollPane(chatList);
        chatBrowser.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        messageField = new JTextField(12);
        messageField.setFont(font1);
        messageField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        messageField.setForeground(Color.GREEN);
        messageField.setBackground(Color.BLACK);

        //need to find a way to trigger "send" on Enter !
        sendMessageButton = new JButton("send");
        sendMessageButton.setActionCommand("send");
        sendMessageButton.setBackground(Color.BLACK);
        sendMessageButton.setForeground(Color.YELLOW);
        sendMessageButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        sendMessageButton.addActionListener(new ButtonClickListener());
        sendMessageButton.setPreferredSize(new Dimension(120,60));

        chatUpdateTimer = new Timer(UPDATE_DELAY,new chatUpdateListener());
        chatUpdateTimer.start();

        this.add(chatBrowser);
        this.add(messageField);
        this.add(sendMessageButton);

    }

    //event handler class for button
    //should break this up for the various panes (screens)
    private  class ButtonClickListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         if( command.equals( "send" ))  {
           try {
            synchronized(objectOut) {
             synchronized(objectIn) {
                   objectOut.writeInt(1);
                   //write message content
                   objectOut.writeObject(messageField.getText());
                   //reset textField
                   messageField.setText(""); 
                   objectOut.flush();
                   
              }
             }
            } catch (IOException exc) {
                        System.out.println("error sending your message");
            }
        }

      }     
    }


    //event handler for chatUpdateTimer
    //not a bad solution but consider instead a client thread dedicated
    //to handling incoming signals, it would have to synchronize on OOS
    //as to not disrupt the serverThreads logic
    private  class chatUpdateListener implements ActionListener{
      //Event dispatch thread spawns new thread for action handler ...
      //need to synchronize on shared resources -> OOS and OIS
      public void actionPerformed(ActionEvent e) {
        if (GuiClient.currentState == State.CHAT) {
                String command = e.getActionCommand();  
                System.out.println("Updating chat");
                try { synchronized(objectOut) {
                       synchronized(objectIn) {
                
                            objectOut.writeInt(2); //request chat update
                            objectOut.flush(); 
                            
                            int control = objectIn.readInt(); 
                            if (control == 55 ) {
                                    //read and unpack chat          
                                    System.out.println("Chat recieved");
                                    ArrayList<Message>  messageVector = (ArrayList<Message>) objectIn.readObject();
                                    //convert vector of messages to vector of strings
                                    String[] unpackedMessages = new String[messageVector.size()];
                                    for (int i = 0; i < messageVector.size(); i++ ) {
                                        unpackedMessages[i] = messageVector.get(i).toString();
                                    }
                                    chatList.setListData(unpackedMessages);    
                            }           
                     }
                    }

                } catch (Exception exc) {
                    System.out.println(exc.getMessage());
                    System.out.println("Yo is update getting fucked up?");
                }
        }
       }
    }


}
