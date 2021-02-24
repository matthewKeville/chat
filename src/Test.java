import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.*;
public class Test {
    static String ip = "localhost";
    static int port = 6066;

 
    public static Socket createConnection() {
        //establish network socket
        Socket client = null;
        try {
         System.out.println("Connecting to " + ip + " on port " + port);
         //attempt connection
         client = new Socket(ip, port);
         //create object outputstream
                 } catch (IOException e)  {
            System.out.println("failed to connect to server");
        }
        return client;
    }


    //send the clients pulse check
    public static void pulse(Socket client) {
         try {
         //open output stream
         ObjectOutputStream objectOut = new ObjectOutputStream(client.getOutputStream());
    
         //write pulse signal
         objectOut.writeInt(0);
         objectOut.flush();  
   
         //close
         objectOut.close();     
        } catch (IOException e) {
            System.out.println("Failed to signal pulse");
        }
    }

    public static void main(String[] args){
        //establish 10 network connections
        List<Socket> connections = new ArrayList<Socket>();
        for (int i=0; i<10; i++){
           connections.add(createConnection()); 
        }
        
        //keep sockets alive
        while (true) {
                Iterator connectionIterator = connections.iterator();
                while (connectionIterator.hasNext()){
                    pulse(((Socket) connectionIterator.next()));
                }
        }
    }



}
