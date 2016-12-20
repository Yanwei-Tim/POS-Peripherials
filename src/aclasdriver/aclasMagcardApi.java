package aclasdriver;



public class aclasMagcardApi {
    // JNI
    public native int open();//ok :0, fail -1; 
    public native int close();
    public native int read(String[] msg);
    public native int beep();
    
    static {
        System.loadLibrary("AclasArmPos");
    }
}