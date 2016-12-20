package aclasdriver;

public class km3_printer {
    public native int open();
    public native int close();
    public native int write(byte[] wrbuf);
    public native byte[] read(int needreadlen);
    public native int readstatus();
    public native int beep();
    static {
        System.loadLibrary("AclasArmPos");
    }
}
