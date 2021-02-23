import java.net.*;
import java.io.*;
import java.util.*;
// byte codes
// 1 message sent from client
// 2 client request for chat update
// 3 chat sent from client


public class Client {

   public static void main(String [] args) {
      String serverName = args[0];
      int port = Integer.parseInt(args[1]);
      String userName = args[2];
      ArrayList<Message> localChat = new ArrayList<Message>();
      //Vector<Message> localChat = new Vector<Message>();
      try {

         System.out.println("Connecting to " + serverName + " on port " + port);
         //attempt connection
         Socket client = new Socket(serverName, port);

         //create streams
         OutputStream outToServer = client.getOutputStream();
         InputStream inFromServer = client.getInputStream();

         //create object outputstream
         //ObjectOutputStream objectOut = new ObjectOutputStream(outToServer);
         ObjectOutputStream objectOut = new ObjectOutputStream(outToServer);
         ObjectInputStream objectIn = new ObjectInputStream(inFromServer);

         
         boolean running = true;
         boolean update = false;
         String line = "";
         String chatString = "";
         Scanner scan = new Scanner(System.in);
         while (running) {

            String menu = "[1] Enter a message\n[2] Refresh chat";
            chatString = ""; 
            for (Message m : localChat) {
               chatString+=m.toString()+"\n";
            }
            //clear screen
            System.out.print("\033[H\033[2J");   
            System.out.println(menu);
            System.out.println(chatString); 
            line = scan.nextLine();
            switch(line) {
                case "1":
                    System.out.println("Enter your message");
                    String messageContent = scan.nextLine();
                    //write packet to server
                    Message message = new Message(userName,messageContent);
                    localChat.add(message);
                    Packet mpack = new Packet(1,message);
                    objectOut.writeObject(mpack);
                    objectOut.flush();
                    break;
                case "2":
                    try {
                    //signal to server request for new chat log
                    Packet upack = new Packet(2);
                    objectOut.writeObject(upack);
                    objectOut.flush();
                    //read in chat object
                    Packet serverPack = (Packet) objectIn.readObject();
                    //objectOut.reset();
                    //objectIn.drain();
                    if (serverPack.getControl() != 3) {
                        System.out.println("this is fucked");
                        System.exit(1);
                    }
                    //Vector newChat = (Vector<Message>) serverPack.getPayload();
                    //localChat = (Vector<Message>) serverPack.getPayload();

                    ArrayList<Message> newChat = (ArrayList<Message>) serverPack.getPayload();
                    localChat = (ArrayList<Message>) serverPack.getPayload();


                    update = true;
                    } catch (Exception e) {
                        System.out.println("Yo is update getting fucked up?");
                    }
                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }              
         }

         //exit
         client.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }



}
