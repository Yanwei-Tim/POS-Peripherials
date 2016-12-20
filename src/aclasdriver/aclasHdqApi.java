package aclasdriver;

public class aclasHdqApi {
    // JNI
    public native int     open();//ok :0, fail -1; 
    public native int     close();
    public native byte[]  read();
    public native int     beep();
    
    static {
        System.loadLibrary("AclasArmPos");
    }
}