import java.util.ArrayList;
import java.util.UUID;
import java.io.Serializable;
import java.util.HashMap;
public class ChatRoom implements Serializable {
    private String name;
    private String id;
    private boolean whiteListActive;
    private boolean secret; //only whitelisted users can see this chat
    private ArrayList<String> whiteList; //userID only whitelisted can join chat
    private ArrayList<String> blackList; //userID banned from chat
    private ArrayList<Message> history;
    private ArrayList<String> lobby;
    private int capacity; 

    public ChatRoom(String name,int capacity) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.whiteListActive = false;
        this.secret = false;
        this.whiteList = new ArrayList<String>();
        this.blackList = new ArrayList<String>();
        this.history = new ArrayList<Message>();
        this.capacity = capacity;
        this.lobby = new ArrayList<String>();
    }

    //attempt to put user in chat room
    //respond with status
    //0 access denied : full 
    //1 access granted 
    //2 access denied : members only
    //3 access denied : you are banned
    public int join(String userId,String username) {
        //lobby is full
        if (lobby.size() == capacity) {
            return 0;
        }
        //banned
        if (blackList.contains(userId)) {
            return 3;
        }
        
        if (whiteListActive) {
            if (!(whiteList.contains(userId))) {
                return 2;
            }else {
                lobby.add(userId);
                history.add(new Message("",username+" has joined the room.")); 
                return 1;
            }
        }
        lobby.add(userId);
        history.add(new Message("",username+" has joined the room.")); 
        return 1;
    }

    //remove a user from the lobby
    public void remove(String userId,String username) {
        if (lobby.contains(userId)) {
            int ndx = lobby.indexOf(userId);
            lobby.remove(ndx);
        }
        history.add(new Message("",username+" has left the room.")); 
    }

    //how many users are in the room
    public int lobbyCount() {
        return lobby.size();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void toggleWhiteList(boolean b) {
        this.whiteListActive = b;
    }

    public void addMessage(Message m) {
        history.add(m);
    }

    public ArrayList<Message> getHistory() {
        return history;
    }

    public String getId(){
        return id;
    }

    //generate string,object map with safe room information
    public HashMap<String,Object> getHeaderMap() {
        HashMap<String,Object> header = new HashMap<String,Object>();
        header.put("name",name);
        header.put("id",id);
        header.put("capacity",capacity); 
        header.put("occupancy",lobby.size());
        return header;
    }

}
