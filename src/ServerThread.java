import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerThread extends Thread {

   private Socket socket;   
   private String socketAddress;
   //client data streams
   private DataInputStream dataIn;
   private DataOutputStream dataOut;
   //client object streams
   private ObjectInputStream objectIn;
   private ObjectOutputStream objectOut;

   public ServerThread(Socket client) {
        this.socket = client;
        try {
            objectIn = new ObjectInputStream(socket.getInputStream());
            objectOut = new ObjectOutputStream(socket.getOutputStream()); 
            socketAddress = ""+socket.getRemoteSocketAddress();
            System.out.println("Successfully Connected :" + socketAddress);

        } catch(IOException e) {
            return;
        }
   }
   

    public void run() {
        
        while(true) {
            try {
                Packet userPack = (Packet) objectIn.readObject();
                int control = userPack.getControl();
                System.out.println("control code recieved " + control);
                switch(control) {
                    case 1://message from client
                        System.out.println("message recived from client");
                        Message m  = (Message) userPack.getPayload();
                        System.out.println(m.toString());
                        synchronized(Server.chat) {
                            Server.chat.add(m);
                        }
                        break;
                    case 2://update chat request
                        System.out.println("update request recieved");
                        synchronized(Server.chat) {
                            System.out.println("sthread chat size " + Server.chat.size());
                            Packet pack = new Packet(3,Server.chat);
                            System.out.println("Packaged just now size : " +((Vector<Message>) pack.getPayload()).size());
                            objectOut.writeObject(pack);
                            objectOut.flush();
                        }
                        break;
                    case -1: //stream is dead
                        return;
                    default:
                        System.out.println("Client sent invalid control code : " + control);
                }
            } catch (Exception e) {
                System.out.println("something is fucked up");
                System.out.println(e.getMessage());
                //System.out.println(socketAddress + " has disconnected");
                //kill thread
                return;
            }
        }
    }
    
}
