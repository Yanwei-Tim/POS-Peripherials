package aclasdriver;

public class UsbRfid {
    public native int open();
    public native int close();
    public native int write(byte[] wrbuf);
    public native byte[] read(int needreadlen);
    static {
        System.loadLibrary("AclasArmPos");
    }
}
