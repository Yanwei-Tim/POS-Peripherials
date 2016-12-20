package aclasdriver;

public class SmartCard {
    public native int Open(int dev);
    public native void Close(int dev);
    public native int SetCardType(int dev,int type);
    public native int Pread(int dev,byte[] buf,int cnt,int pos);
    public native int Pwrite(int dev,byte[] buf,int cnt,int pos);
    public native int TestCardReady(int dev);
    public native byte[] GetATR(int dev);
    public native int GetState(int dev);
    public native int IsCardRemoved(int dev);
    public native int SendCommand(int dev, byte[] send, byte[] recv);
    public native byte[] ApduCommand(int dev, int cla, int ins, int p1, int p2, byte[] data, int le);

    public byte[] GetChallenge8(int dev)
    {
//        return this.ApduCommand(dev, 0, 0x84, 0, 0, null, 8);
        return this.ApduCommand(dev, 0, 0x84, 0, 0, new byte[0], 8);
    }

    /* Standard Command */
    public byte[] SelectFile(int dev, byte[] file_id)
    {
        return ApduCommand(dev, 0, 0xa4, 0, 0, file_id, -1);
    }

    public byte[] GetResponse(int dev, byte le)
    {
        return this.ApduCommand(dev, 0, 0xc0, 0, 0, new byte[0], le);
    }

    public byte[] VerifyPIN(int dev, byte PIN_id, byte[] PIN)
    {
        return this.ApduCommand(dev, 0, 0x20, 0, PIN_id, PIN, -1);
    }

    public byte[] ReadBinary_CUR(int dev, byte pos, byte len)
    {
        return this.ApduCommand(dev, 0, 0xb0, (pos>>8) & 0x7f, pos & 0xff, new byte[0], len);
    }

    static {
        System.loadLibrary("AclasArmPos");
    }
}
