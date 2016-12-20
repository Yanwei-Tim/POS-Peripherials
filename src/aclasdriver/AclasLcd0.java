package aclasdriver;

import android.graphics.Bitmap;
import android.util.Log;

public class AclasLcd0 {
    private static final String TAG = "AclasArmPosDBG";
    // JNI
    public native int open();//ok :1, fail -1; 
    public native void close();
    public native void writeMsg(byte[] msg);
    public native void SetContrast(int contrast);
    public native void SetBacklight(int on);
    public native int WriteDotMatrix(byte[] dot);
    public native int GetWidth(); 
    public native int GetHeight(); 
    public int WriteBitMap(Bitmap map) {
        int height = GetHeight();  
        int width =  GetWidth();
        int byteofline = (width+7)/8;
        byte[] BitMapBuf = new byte[height*byteofline];
        int[] tmpbuf = new int[width];
        int mapwidth = (map.getWidth() > width) ? width : map.getWidth();
        int mapheight = (map.getHeight() > height) ? height : map.getHeight();
        
        for(int i=0; i<BitMapBuf.length; i++)
        {
            BitMapBuf[i] = 0;
        }
       
        Log.d(TAG, "0 " + "a=" + map.getPixel(0, 0) + " b=" + map.getPixel(0, 1) 
                + " height = " + height + " width = " + width 
                + " mapwidth = " + mapwidth + " mapheight = " + mapheight);
        
        for(int i=0; i<mapheight; i++)
        {
            map.getPixels(tmpbuf, 0, mapwidth, 0, i, mapwidth, 1);
           
            for(int j=0; j<mapwidth; j+=8)
            {
//                if((height == 32) && (width == 144))
//                {
//                    for(int k=0; k<8; k++)
//                    {
//                        if((j+k) >= mapwidth)
//                            break;
////                        Log.d(TAG,"32x144 lcd");
//                        if(tmpbuf[j+k] != 0xFFFFFFFF)
//                        {
//                            BitMapBuf[i*byteofline + j/8] |= (0x01 << k);
//                        }else
//                        {
//                            BitMapBuf[i*byteofline + j/8] &= (~(0x01 << k));
//                        }
//                    }
//                } 
//                else
                {
                    for(int k=0; k < 8; k++)
                    {
                        if((j+k) >= mapwidth)
                            break;
//                        Log.d(TAG,"32x144 lcd");
                        if(tmpbuf[j+k] != 0xFFFFFFFF)
                        {
                            BitMapBuf[i*byteofline + j/8] |= (0x01 <<(7-k));
                        }else
                        {
                            BitMapBuf[i*byteofline + j/8] &= (~(0x01 << (7-k)));
                        }
                    }
                }
         
            }
        }
        
//        BitMapBuf[0]=(byte)0xf0;
//        return 0;
        return WriteDotMatrix(BitMapBuf);
    }
    
    static {
        System.loadLibrary("AclasArmPos");
    }
}