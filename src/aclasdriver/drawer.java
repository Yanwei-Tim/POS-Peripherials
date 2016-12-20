package aclasdriver;

public class drawer {
    public native int open();
    static {
        System.loadLibrary("AclasArmPos");
    }
}
