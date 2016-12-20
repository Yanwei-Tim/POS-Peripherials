package aclasdriver;

public class printer {
    public native int Open();
    public native void Close();
    public native int Write(byte[] wrbuf);
    public native byte[] Read(int getlen);
    public native int SetContrast(int contrast);
    public native int Feed(int setp);
    public native int Stop();
    public native int Conitnue();
    public native int GetDotWidth(); 
    public native int SetPrintMode(int Mode); //
    public native boolean IsPaperExist();    
    static {
        System.loadLibrary("AclasArmPos");
    }
}
