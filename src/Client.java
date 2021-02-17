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
      //ArrayList<Message> localChat = new ArrayList<Message>();
      Vector<Message> localChat = new Vector<Message>();
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
            System.out.println("messages " + localChat.size());
            line = scan.nextLine();
            switch(line) {
                case "1":
                    System.out.println("Enter your message");
                    String message = scan.nextLine();
                    //write packet to server
                    Packet mpack = new Packet(1,new Message(userName,message));
                    objectOut.writeObject(mpack);
                    objectOut.flush();
                    break;
                case "2":
                    try {
                    System.out.println("Reading new chat log");
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
                    Vector newChat = (Vector<Message>) serverPack.getPayload();
                    localChat = (Vector<Message>) serverPack.getPayload();
                    System.out.println(newChat.equals(localChat));
                    update = true;
                    } catch (Exception e) {
                        System.out.println("Yo is update getting fucked up?");
                    }
                    System.out.println("Size of recieved chat " + localChat.size());
                    System.out.println("debug");
                    scan.nextLine();
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
