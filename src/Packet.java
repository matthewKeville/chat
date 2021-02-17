import java.io.Serializable;
//the Packet class is a wrapper for objects sent over
//an ObjectStream. The packet contains an object 
//and a control code.
public class Packet implements Serializable {
    private int control;
    //private SerialObject payload;
    private Serializable payload; 

    //control code and serializable data 
    public Packet(int control,Serializable o) {
        this.control = control;
        this.payload = o;
    }

    //just a control code, no payload
    public Packet(int control) {
        this.control = control;
        this.payload = null;
    }

    public Serializable getPayload(){
        return this.payload;
    }
    
    public int getControl(){
        return this.control;
    }
    

}
