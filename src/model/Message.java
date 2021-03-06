package model;
import java.io.Serializable;
public class Message implements Serializable {
    private String user;
    private String content;
    private long time;
   
    public Message(String user,String content) {
        this.user = user;
        this.content = content;
        this.time = System.currentTimeMillis()/1000;
    }
    
    public String getUser(){
        return user;
    }
    
    public String getContent() {
        return content;
    }

    //time in seconds
    public String getTime() {
        return Long.toString(time);
    }
        

    public String toString() {
        return ""+user+" : "+content+" : "+Long.toString(time);
    }

}
