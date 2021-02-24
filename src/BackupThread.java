import java.io.*;
//save the Servers data maps at regular intervals
//this depends on maps in Server class being static
public class BackupThread extends Thread {
   float delay = 10000;//default delay 10 minutes

   public BackupThread(float delay) {
    this.delay = delay;
   }

   public void run() {
    float lastUpdate = System.currentTimeMillis();
    while(true) {
       if (System.currentTimeMillis() > lastUpdate + delay) {
        System.out.println("Backing up data start");
        float backupStart = System.nanoTime();
        saveData();
        float elapsed = System.nanoTime() - backupStart;
        System.out.println("Backup finished in : " + elapsed);
        lastUpdate = System.currentTimeMillis();
       } 
    }
   }

   //save the current chatRooms and Users to file
   public static void saveData() {
        try {
            //write rooms
            FileOutputStream fos = new FileOutputStream(new File("res/rooms.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Server.rooms); 
            oos.close();
            fos.close(); 
    
            //write users
            fos = new FileOutputStream(new File("res/users.dat"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(Server.users);
            oos.close();
            fos.close(); 
    
        } catch (IOException exc) {    
            System.out.println("Error Writing Data to File");
        }
    }   
    
      
    
}
