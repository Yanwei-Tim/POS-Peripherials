package aclasdriver;

public class rfid {
    public native String ReadCardNo();
    public native int open();
    public native int close();
    public native int beep();
    public native boolean WriteCardBlock(int Blockaddress, byte[] wrbuf);
    public native byte[] ReadCardBlock(int Blockaddress);
    public native boolean AuthKey(int Blockaddress, byte[] key);
    public native boolean SetCardPsw(int Blockaddress, byte[] key);

    static {
        System.loadLibrary("AclasArmPos");
    }
}
