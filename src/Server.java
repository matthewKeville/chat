import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
public class Server {
    static final int PORT = 6066;
    static final int BACKUP_DELAY = 1000;
    static List<Thread> threads;    
    static ArrayList<Message> chat;
    static HashMap<String,ChatRoom> rooms;    
    static HashMap<String,User> users;

    public static void main(String[] args) {

        rooms = new HashMap<String,ChatRoom>();
        users = new HashMap<String,User>();

        //load chatrooms and users from file
        loadData();

        threads = new ArrayList<Thread>();

        //establish socket server
        ServerSocket serverSocket = null;
                try {
            serverSocket = new ServerSocket(PORT);
            //only block for this set time
            //current implementation has timeout because
            //chat updates and thread management 
            //are done in this main method
            //consider spawning a dedicated thread
            serverSocket.setSoTimeout(10000);
        } catch (IOException e) {
            System.out.println("Failed to create socket server");
            e.printStackTrace();
        }

         //start backup thread : 1 minute backup
         BackupThread bt = new BackupThread(BACKUP_DELAY); 
         bt.start();
    
        while (true) {
        
            try {
                //create a new ServerThread on this socket (associated with client)
                ServerThread st = new ServerThread(serverSocket.accept());
                st.start();
                threads.add(st);

            } catch (IOException e) {
                System.out.println("Error establishing connection with client :" + e);
            }

            System.out.println("No new connections, checking for dead connections");
            Iterator itty = threads.iterator(); 
            while (itty.hasNext()) {
                ServerThread s = (ServerThread) itty.next();
                if (!(s.isAlive())) {
                    System.out.println("Removing Dead thread");
                    itty.remove();
                } 
                   
            }
             //print out current chat
            /*
            synchronized(chat){
                itty = chat.iterator();
                System.out.println("Current chat");
                while (itty.hasNext() ) {
                    System.out.println( ((Message) itty.next()).toString());
                }
            }
            */
                            
        }
    }

    //Return the chatRoom header map for each chatRoom listed in the rooms map
    public static ArrayList<HashMap<String,Object>> getChatRoomHeaders() {
        ArrayList<HashMap<String,Object>> headerList = new ArrayList<HashMap<String,Object>>();
        for (String k : rooms.keySet() ) {
            headerList.add(rooms.get(k).getHeaderMap());
        }
        return headerList;
    }

    //load user and chat maps from file if it exists
    public static void loadData() {
            //do files exist?
            File userTemp = new File("users.dat");
            File chatTemp = new File("rooms.dat");

            //no data has been saved , create empty maps and exit
            if ( !userTemp.exists()  || !chatTemp.exists() ) {
                users = new HashMap<String,User>();
                rooms = new HashMap<String,ChatRoom>();
                System.out.println("No data found");
                System.out.println("Creating development chatroom");
                ChatRoom devChat = new ChatRoom("dev",5);
                rooms.put(devChat.getId(),devChat);
                return;

            //data found, load it into the maps
            } else {
                    try {
                        //write rooms
                        FileInputStream fis = new FileInputStream(new File("rooms.dat"));
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        try {
                            rooms = (HashMap<String,ChatRoom>) ois.readObject(); 
                        } catch (ClassNotFoundException cnf) {
                            System.out.println("Error Reading Rooms from file");
                            System.out.println(cnf.getMessage());
                        }

                        //System.out.println("Rooms is null? " + (rooms == null) );
                        ois.close();
                        fis.close(); 
                    
                        //write users
                        fis = new FileInputStream(new File("users.dat"));
                        ois = new ObjectInputStream(fis);
                        try {
                            users = (HashMap<String,User>) ois.readObject();
                        } catch (ClassNotFoundException cnf) {
                            System.out.println("Error Reading Users from file");
                            System.out.println(cnf.getMessage());
                        }
                        ois.close();
                        fis.close(); 
                    
                    } catch (IOException exc) {     
                        System.out.println("Error loading stored data");
                    } 
                }
            }

}
