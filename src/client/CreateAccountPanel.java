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
public class CreateAccountPanel extends JPanel {
    private JTextField newUsernameField,newPasswordField,verifyPasswordField;
    private JLabel usernameLabel, passwordLabel,verifyPasswordLabel;
    private JButton registerAccountButton;

    private Font font1;
    private CardLayout cardLayout;
    private JPanel master, usernamePanel,passwordPanel,verifyPasswordPanel;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;

    public CreateAccountPanel(CardLayout cardLayout,JPanel master,ObjectOutputStream objectOut,ObjectInputStream objectIn) {

        super();

        this.objectOut = objectOut;
        this.objectIn = objectIn;

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBackground(Color.BLACK);

        this.cardLayout = cardLayout;
        this.master = master;
        Font font1 = new Font("SansSerif",Font.BOLD,10); 

        newUsernameField = new JTextField(12);
        newUsernameField.setFont(font1);
        newUsernameField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        newUsernameField.setForeground(Color.GREEN);
        newUsernameField.setBackground(Color.BLACK);

        newPasswordField = new JTextField(12);
        newPasswordField.setFont(font1);
        newPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        newPasswordField.setForeground(Color.GREEN);
        newPasswordField.setBackground(Color.BLACK);

        verifyPasswordField = new JTextField(12);
        verifyPasswordField.setFont(font1);
        verifyPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        verifyPasswordField.setForeground(Color.GREEN);
        verifyPasswordField.setBackground(Color.BLACK);

        usernameLabel = new JLabel("Username: ");
        usernameLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        usernameLabel.setForeground(Color.GREEN);
        usernameLabel.setBackground(Color.BLACK);

        passwordLabel = new JLabel("Password: ");
        passwordLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        passwordLabel.setForeground(Color.GREEN);
        passwordLabel.setBackground(Color.BLACK);

        verifyPasswordLabel = new JLabel("Verify Password: ");
        verifyPasswordLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        verifyPasswordLabel.setForeground(Color.GREEN);
        verifyPasswordLabel.setBackground(Color.BLACK);

        usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel,BoxLayout.X_AXIS));
        usernamePanel.setBackground(Color.BLACK);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(Box.createHorizontalStrut(20));
        usernamePanel.add(newUsernameField);

        passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel,BoxLayout.X_AXIS));
        passwordPanel.setBackground(Color.BLACK);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createHorizontalStrut(20));
        passwordPanel.add(newPasswordField);

        verifyPasswordPanel = new JPanel();
        verifyPasswordPanel.setLayout(new BoxLayout(verifyPasswordPanel,BoxLayout.X_AXIS));
        verifyPasswordPanel.setBackground(Color.BLACK);
        verifyPasswordPanel.add(verifyPasswordLabel);
        verifyPasswordPanel.add(Box.createHorizontalStrut(20));
        verifyPasswordPanel.add(verifyPasswordField);

        registerAccountButton = new JButton("register");
        registerAccountButton.setActionCommand("register");
        registerAccountButton.setBackground(Color.BLACK);
        registerAccountButton.setForeground(Color.YELLOW);
        registerAccountButton.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        registerAccountButton.addActionListener(new ButtonClickListener());

        this.add(usernamePanel);
        this.add(passwordPanel);
        this.add(verifyPasswordPanel);
        this.add(registerAccountButton);

     }

    //event handler class for button
    //should break this up for the various panes (screens)
    private  class ButtonClickListener implements ActionListener{
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();  
         if( command.equals( "register" ))  {
            //ensure password fields have parity
            if (newPasswordField.getText().equals(verifyPasswordField.getText())) {
                    //attempt to create account
                    try {
                            objectOut.writeInt(3);
                            objectOut.writeObject(newUsernameField.getText());
                            objectOut.writeInt(newPasswordField.getText().hashCode());
                            objectOut.flush();
                            
                            int response = objectIn.readInt();
                            if (response == 52) {
                                //if account creation is successful then switch 
                                // to login pannel
                                System.out.println("Account creation successful");
                                newUsernameField.setText("");
                                newPasswordField.setText("");
                                verifyPasswordField.setText("");
                                //change state
                                GuiClient.currentState = State.LOGIN;
                                cardLayout.show(master,"loginPanel");
                            } else if (response == 53) {
                                System.out.println("username already taken!");
                            }
                    } catch (IOException exc) {
                        System.out.println("Error communicating with server");
                    }

            } else {
                //display account creation error pop ...
                System.out.println("Invalid Password");
            }   
         }
      }     
    }
}
