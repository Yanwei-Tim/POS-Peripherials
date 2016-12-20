package aclasdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import android.AclasDemos.R;
import android.util.Log;

/**************************************************************
SOH STX STA SIGN WEIGHT_ASCII WEIGHT_UNIT  BCC ETX EOT
SOH         :    开始传送符,一个字节,固定为01H.
STX         :    数据项开始符,一个字节,固定为02H.
STA         :    重量状态字, 一个字节,只可能为53H或55H46H; 为53H表示重量稳定,为55H表示重量不稳定,为46表示重量异常.
SIGN        :    重量符号字, 一个字节,只可能为2DH或20H; 为2DH表示重量为负数,为20H表示重量为正数.
WEIGHT_ASCII:    重量字符串, 5~6个字节,只可能为数字30H~39H,小数点(2EH),空格(20H).
WEIGHT_UNIT :    重量单位字符串, 1~2个字节,只可能为:'TJ':台斤,'TL':台两, 'SJ':市斤, 'LB':磅, 'KG':公斤, 'G':克
BCC         :    数据校验字,一个字节. 设STA为D1,SIGN为D2,BCC前的那个字节为DN,则BCC=D1^D2^..^DN.'^'是逻辑异或运算符.
ETX         :    数据项结束符,一个字节,固定为03H
EOT         :    传送结束符  ,一个字节,固定为04
**************************************************************/


public class AclasScale extends SerialPort {
    public static final String tag = "AclasArmPosDBG";
    private final byte[] TareBuf = { (byte) 0xfe };  
    private final byte[] ZeroBuf = { (byte) 0xfd };
    protected OutputStream scale_OutputStream;
    private InputStream scale_InputStream;
    private File scale_file;
    private int scale_baudrate;
    private ACLAS_SCALE_PROTOCOL_TYPE scale_type = ACLAS_SCALE_PROTOCOL_TYPE.scale_protocol_autoupload;
    public enum ACLAS_SCALE_PROTOCOL_TYPE
    {
        scale_protocol_angel, scale_protocol_ftp, scale_protocol_autoupload
    }

    public AclasScale(File device, int baudrate, int flags, ACLAS_SCALE_PROTOCOL_TYPE type)
            throws SecurityException, IOException {
        // TODO Auto-generated constructor stub
        super(device, baudrate, flags);
        scale_file = device;
        scale_baudrate = baudrate;
        scale_InputStream = super.getInputStream();
        scale_OutputStream = super.getOutputStream();
    }

    public void close()
    {
        super.close();
    }

    public byte[] get_wei()
    {
        byte[] weistr = new byte[100];
        
        switch(scale_type)
        {
            case scale_protocol_angel:
                break;
                
            case scale_protocol_ftp:
                break;
                
            default:    
            case scale_protocol_autoupload:
                try {
                    int size = scale_InputStream.read(weistr);
                    if(size > 0)
                    {
//                        //debug
//                        {
//                            StringBuilder dbgstr = new StringBuilder();
//                            dbgstr.append("str = ");
//                            for(int i=0; i<size; i++)
//                            {   
//                                dbgstr.append(Integer.toHexString(weistr[i]) + " ");
//                            }
//                            Log.d(tag, dbgstr.toString());
//                            Log.d(tag, "size = " + size);
//                        }

                        {//decode scale weight string
                            if(size > 12)
                            {//SOH STX STA SIGN WEIGHT_ASCII WEIGHT_UNIT  BCC ETX EOT
                                int soh_pos = 0, etx_pos = 0, foundF = 0;;
                                for(int i=0; i<size; i++)
                                {                                    
                                    if((weistr[i] == 0x01) && (weistr[i + 1] == 0x02) 
                                    && (weistr[i + 2] == 0x53) || (weistr[i + 2] == 0x55) || (weistr[i + 2] == 0x46))
                                    {//find SOH
                                        soh_pos = i;
                                        foundF = 1;
                                    }
                                    
                                    if((weistr[i] == 0x03) && (weistr[i + 1] == 0x04))
                                    {//found ETX
                                        if(foundF == 1)
                                        {
                                            etx_pos = i;
                                            foundF = 2;
                                            break;
                                        }
                                        
                                    }
                                }

                                if(foundF == 2)
                                {                                    
                                    byte bcc = weistr[2];
                                    
                                    int weilen = etx_pos - soh_pos - 3;
                                    byte[] uploadwei = new byte[weilen];
                                    for(int i=0; i<weilen; i++)
                                    {
                                        uploadwei[i] = weistr[i + 2];
                                        if(i > 0) { bcc ^= weistr[i + 2]; }                                           
                                    }
                                    
                                    if(bcc != weistr[etx_pos - 1])
                                    {
                                        Log.e(tag, "BCC error, it's " + Integer.toHexString(weistr[etx_pos - 1])   + "should be " + Integer.toHexString(bcc) );
                                        return null;
                                    }
                                    
                                    if(uploadwei[1] == 0x2D)
                                    {//change sign
                                        uploadwei[1] = '-';
                                    }
                                    else
                                    {
                                        uploadwei[1] = '+';
                                    }
                                    
                                    switch(uploadwei[0])
                                    {
                                        case 0x53:
                                            uploadwei[0] = 'S';   //stable weight
                                            break;
                                            
                                        case 0x55:
                                            uploadwei[0] = 'U';   //ustable weight
                                            break;
                                            
                                        case 0x46:
                                            byte [] err = { 'W', 'e', 'i', 'g', 'h', 't', ' ', 'E', 'r', 'r', 'o', 'r' }; //weight error
                                            uploadwei = err;
                                            break;
                                    }
                                    return uploadwei;
                                }
                            }
                        }
                    }
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            
        }
        return null;
    }
    
    public void SetTare()
    {
        try {
            scale_OutputStream.write(TareBuf);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void SetZero()
    {
        try {
            scale_OutputStream.write(ZeroBuf);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }    
}

