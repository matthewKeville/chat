package model;
import java.util.UUID;
import java.io.Serializable;
public class User implements Serializable {
    private String username;
    private int passwordHash;
    private String userId; //this must be unique
    private boolean online;   
 
    public User(String username, int passwordHash){
       this.username = username;
       this.passwordHash = passwordHash;
       this.userId = UUID.randomUUID().toString(); 
       this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setPasswordHash(int newPass) {
        passwordHash = newPass;
    }

    public void setOnline(boolean b) {
        online = b;
    }

    public int getPasswordHash() {
        return passwordHash; 
    }

}
