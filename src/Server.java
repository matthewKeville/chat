import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
public class Server {
    static final int PORT = 6066;
    static List<Thread> threads;
    
    //static ArrayList<Message> chat;
    static Vector<Message> chat;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        threads = new ArrayList<Thread>();
        //chat = new ArrayList<Message>();
        chat = new Vector<Message>();
        //establish socket server
        try {
            serverSocket = new ServerSocket(PORT);
            //only block for this set time
            serverSocket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            //program should hang at accept until a
            //connection is attempted again
            try {
                //create a new ServerThread on this socket (associated with client)
                ServerThread st = new ServerThread(serverSocket.accept());
                st.start();
                threads.add(st);

            } catch (IOException e) {
                //System.out.println("I/O Error: " + e);
            }

            System.out.println("No new connections, checking for dead connections");
            Iterator itty = threads.iterator(); 
            while (itty.hasNext()) {
                ServerThread s = (ServerThread) itty.next();
                //threads seems to properly pass chat to Server class,
                //but don't take updates from other threads additions
                //s.updateChat(chat);
                if (!(s.isAlive())) {
                    System.out.println("Removing Dead thread");
                    itty.remove();
                //thread is alive , add its messages to the chat
                } 
                   
            }
            //print out current chat
            synchronized(chat){
                itty = chat.iterator();
                System.out.println("Current chat");
                while (itty.hasNext() ) {
                    System.out.println( ((Message) itty.next()).toString());
                }
            }
                            
        }
    }

}
