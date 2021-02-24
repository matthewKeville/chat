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
public class LoginPanel extends JPanel {
    private JTextField usernameField,passwordField;
    private JButton loginButton,createAccountButton;
    private JPanel loginButtonPanel;
    private Font font1;
    private CardLayout cardLayout; //need reference to masters's card layout
    private JPanel master;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;


    public LoginPanel(CardLayout cardLayout,JPanel master,ObjectOutputStream out,ObjectInputStream in) {
        super();

        this.master = master;
        this.cardLayout = cardLayout;

        this.objectOut = out;
        this.objectIn = in;
 
        Font font1 = new Font("SansSerif",Font.BOLD,10); 
        //set this layout 
        //BoxLayout is just column or row layout
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBackground(Color.BLACK);

        usernameField = new JTextField(12);
        usernameField.setFont(font1);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        usernameField.setForeground(Color.GREEN);
        usernameField.setBackground(Color.BLACK);

        passwordField = new JTextField(12);
        passwordField.setFont(font1);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        passwordField.setForeground(Color.GREEN);
        passwordField.setBackground(Color.BLACK);

        loginButton = new JButton("Login");
        loginButton.setActionCommand("login");
        loginButton.setBackground(Color.BLACK); 
        loginButton.setForeground(Color.YELLOW);      
        loginButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));    
        loginButton.addActionListener(new ButtonClickListener()); 
        loginButton.setPreferredSize(new Dimension(120,60));
 
        createAccountButton = new JButton("Create New Account");
        createAccountButton.setActionCommand("create");
        createAccountButton.setBackground(Color.BLACK); 
        createAccountButton.setForeground(Color.YELLOW);      
        createAccountButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));   
        createAccountButton.addActionListener(new ButtonClickListener());       
        createAccountButton.setPreferredSize(new Dimension(120,60));
    
        loginButtonPanel = new JPanel();
        loginButtonPanel.setLayout(new BoxLayout(loginButtonPanel,BoxLayout.X_AXIS));
        loginButtonPanel.setBackground(Color.BLACK);
        loginButtonPanel.add(loginButton);
        loginButtonPanel.add(Box.createHorizontalStrut(20)); //add spacing 
        loginButtonPanel.add(createAccountButton);

        this.add(usernameField);
        this.add(passwordField);
        this.add(loginButtonPanel);
    }

//event handler class for button
    //should break this up for the various panes (screens)
    private  class ButtonClickListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         if( command.equals( "login" ))  {
            //begin login sequence
            try {
                    synchronized(objectOut) {
                     synchronized(objectIn) {
                            objectOut.writeInt(4);
                            objectOut.writeObject(usernameField.getText());
                            objectOut.writeInt(passwordField.getText().hashCode()); 
                            objectOut.flush();                    
                            int response = objectIn.readInt();
                            if (response == 50) {
                                GuiClient.currentState = State.BROWSER;
                                cardLayout.show(master,"browserPanel");
                            } else if (response == 51) {
                                System.out.println("Bad Login Attempt");
                            }
                    }
                   }
            } catch (IOException exc) {
                System.out.println("login failed");
            }
            
         } else if( command.equals( "create" ))  {
            //switch card to createAccountPanel
            cardLayout.show(master,"createAccountPanel");
         } 
      }     
    }
}
