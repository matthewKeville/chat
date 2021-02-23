import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
// control codes
// 1 message from client
// 2 chat update request from client
// 3 request chat room list
public class ServerThread extends Thread {
  
   //networking vars 
   private Socket socket;   
   private String socketAddress;
   private ObjectInputStream objectIn; 
   private ObjectOutputStream objectOut;
   //connected client state info
   //private String clientUserId;
   private User clientUser;
   private String currentRoomId;
   private final long PULSE_DELAY = 3000; //3 seconds
   private long lastPulse;

   public ServerThread(Socket client) {
        this.socket = client;
        try {
            objectIn = new ObjectInputStream(socket.getInputStream());
            objectOut = new ObjectOutputStream(socket.getOutputStream()); 
            socketAddress = ""+socket.getRemoteSocketAddress();
            System.out.println("Successfully Connected :" + socketAddress);
            lastPulse = System.currentTimeMillis();

        } catch(IOException e) {
            System.out.println("Error establishing ObjectStreams with client");
            return;
        }
   }
   

    public void run() {
        while(true) {

            //only execute below if the connection is active
            if ( System.currentTimeMillis() > lastPulse + PULSE_DELAY) {
                System.out.println("Stopped recieving communicatioin from client");
                System.out.println("cleaning  up, and closing thread"); 
                synchronized(Server.rooms) {
                 Server.rooms.get(currentRoomId).remove(clientUser.getUserId(),clientUser.getUsername());
                } 
                return; 
            }            


            try {
                //each request from the client begins with
                //an integer control code
                int control = objectIn.readInt();
                System.out.println("control code recieved " + control);
                objectOut.reset();
                switch(control) {

                    //pulse recieved
                    case 0:
                        lastPulse = System.currentTimeMillis();
                        System.out.println("Pulse recieved");
                        break;
                        

                    //Message Data Recieved from Client
                    //this case is inconsistent with all other requests
                    //as it does not have a response code sent to the client
                    //consider refactoring this event
                    case 1:
                        System.out.println("message data recived from client");
                        String content = (String) objectIn.readObject();
                        //consider refactoring message to include both username and userid
                        Message incoming = new Message(clientUser.getUsername(),content);
                        System.out.println(incoming.toString());
                        synchronized(Server.rooms) {
                            //add message to the chat room the client is connected to
                            Server.rooms.get(currentRoomId).addMessage(incoming);
                        }
                        break;
                    //Chat Update Request Recived from Client
                    case 2:
                        System.out.println("update request recieved");
                        synchronized(Server.rooms) {
                            objectOut.writeInt(55); 
                            objectOut.writeObject(Server.rooms.get(currentRoomId).getHistory()); 
                            objectOut.flush();
                        }
                        break;

                    //Account Creation attempt recieved from client
                    case 3:
                        System.out.println("create account attempt recieved");
                        String userName = (String) objectIn.readObject();
                        int passwordHash = objectIn.readInt();
                        System.out.println(userName);
                        System.out.println(passwordHash);
                        boolean taken = false;
                        //does the username exist?
                        synchronized(Server.users) {
                           for (String key : Server.users.keySet()){
                                if (Server.users.get(key).getUsername().equals(userName)) {
                                    System.out.println("Error , Username already taken");
                                    taken = true;
                                }
                            } 
                        }

                        if (!taken) { 
                                synchronized(Server.users) {
                                    //create account
                                    User newUser = new User(userName,passwordHash);
                                    Server.users.put(newUser.getUserId(),newUser);
                                }
            
                                objectOut.writeInt(52);
                                objectOut.flush();
                        } else {
                            //inform client the account is already taken
                            objectOut.writeInt(53);
                            objectOut.flush();
                        }
                        break;

                    //Login Attempt recieved from client
                    case 4:
                        System.out.println("Login attempt recieved");
                        String usernameIn = (String) objectIn.readObject();
                        int passwordHashIn =  objectIn.readInt();
                        boolean valid = false;
                        String userIdIn = null;

                        System.out.println(usernameIn);
                        System.out.println(passwordHashIn);

                        //get userId from username
                        for (String key : Server.users.keySet()) {
                            if (Server.users.get(key).getUsername().equals(usernameIn)){
                                userIdIn = Server.users.get(key).getUserId();     
                                System.out.println("match : " + Server.users.get(key).getPasswordHash());
                            }
                        }
                        synchronized (Server.users) {
                            User temp = Server.users.get(userIdIn); 
                            System.out.println(temp == null);
                            if (userIdIn != null && temp != null && temp.getPasswordHash()==passwordHashIn) {
                                //clientUserId = userIdIn; 
                                clientUser = temp;
                                System.out.println("Login Successful"); 
                                objectOut.writeInt(50); 
                                objectOut.flush();
                            } else {
                                System.out.println("Login Failed"); 
                                objectOut.writeInt(51); 
                                objectOut.flush();
                            }
                        }  
                        break;

                    //Chat Room Headers requested from client
                    case 5: 
                        System.out.println("chat room list request received");
                        ArrayList<HashMap<String,Object>> roomHeaders = null;
                        synchronized(Server.rooms) {
                            roomHeaders = Server.getChatRoomHeaders();
                        }
                        if (roomHeaders == null) {
                            objectOut.writeInt(57);
                            objectOut.flush();
                        } else {
                            System.out.println("chat room update granted");
                            objectOut.writeInt(56);
                            objectOut.writeObject(roomHeaders);
                            objectOut.flush();
                        }
                        break;

                    //Enter Room request from client
                    case 6:
                        System.out.println("Chat Room enter request recieved");
                        synchronized(Server.rooms) {
                         String requestedRoomId = (String) objectIn.readObject();
                         int result = ((ChatRoom) Server.rooms.get(requestedRoomId)).join(clientUser.getUserId(),clientUser.getUsername());
                         switch (result) {
                            //at capacity
                            case 0:
                                objectOut.writeInt(60);
                                System.out.println("At capacity");
                                break;
                            //success
                            case 1:
                                currentRoomId = requestedRoomId;
                                objectOut.writeInt(61);
                                System.out.println("acccess granted");
                                break;
                            //not whitelisted
                            case 2:
                                objectOut.writeInt(62);
                                break; 
                            //banned
                            case 3:
                                objectOut.writeInt(63);
                                break;
                            default:
                                //shouldn't happen
                                break;
                        }
                        objectOut.flush();
                       } 
                       break;


                    //Client disconnected from socket
                    case -1: 
                        System.out.println("Client Disconnected");
                        return;
                    default:
                        System.out.println("Client sent invalid control code : " + control);
                }
            } catch (Exception e) {
                System.out.println("Error Handling Client Request");
                System.out.println(e.getMessage());
                //return;
            }
        }
    }
    
}
