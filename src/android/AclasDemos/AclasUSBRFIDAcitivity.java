package android.AclasDemos;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;

import aclasdriver.UsbRfid;
import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class AclasUSBRFIDAcitivity extends Activity{
    
    private UsbRfid musbrfid;
    private int fd;
    private TextView terial,tread;
    private String STRING_SERIALERR,STRING_READ,STRING_SERIALOK;
    public static final String tag = "AclasArmPosDBG";
    public static boolean runflag;
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usbrfid_main);
        
        terial = (TextView) findViewById(R.id.textView_rfid);
        tread = (TextView) findViewById(R.id.textView_rfidRead);
        STRING_SERIALERR = getString(R.string.usbrfiderr);
        STRING_SERIALOK = getString(R.string.usbrfidok);
        STRING_READ = getString(R.string.usbrfidread);
        tread.setText(STRING_READ);
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        musbrfid.close();
        fd = -1;
        AclasUSBRFIDAcitivity.runflag = false;
        
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(tag, String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    } 
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        musbrfid = new UsbRfid();
        if((fd = musbrfid.open()) < 0)
        {//show err msg
            terial.setText(STRING_SERIALERR);    
            return;
        }
        else
        {
            terial.setText(STRING_SERIALOK);
        }
        AclasUSBRFIDAcitivity.runflag = true;
        RfidWriteThread tRfidWriteThread = new RfidWriteThread();
        tRfidWriteThread.start();
        
//        RfidReadThread tRfidReadThread = new RfidReadThread();
//        tRfidReadThread.start();
//        Log.d(tag, "usb rfid read start\n");
    }
    
    class RfidWriteThread extends Thread
    {
        public boolean runflag;
        public void run()
        {
            
            
            
            while(AclasUSBRFIDAcitivity.runflag == true)
            {
                byte[] wrbuf = {0x20 ,0x00 ,0x31 ,0x00 ,(byte) 0xCE ,0x03 };
                musbrfid.write(wrbuf);
                
                try {
                    sleep(1000);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                byte[] rdbuf;
                Log.d(tag, "Read The Rfid\n");
                rdbuf = musbrfid.read(100);
                if(rdbuf != null)
                {
                    StringBuilder str3 = new StringBuilder();
                    Message msg_read = gui_show.obtainMessage();
                    msg_read.arg1 = SERIALMSG.MSG_READ;
                    for(int i=0; i<rdbuf.length; i++)
                    {
                        str3.append(Integer.toHexString(rdbuf[i] & 0x000000FF));
                    }
                    msg_read.obj = new String(str3.toString());
                    gui_show.sendMessage(msg_read);
                }
            }

        }
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            super.start();
        }
    }
    
//    class RfidReadThread extends Thread
//    {
//        public boolean runflag;
//        public void run()
//        {
//            byte[] rdbuf;
//            byte[] cardno;
//            while(this.runflag)
//            {
//                if(fd < 0) 
//                {
//                    runflag = false;
//                    break;
//                }
//                Log.d(tag, "aclas usb rfid read thread\n");
//                rdbuf = musbrfid.read(100);
//                if(rdbuf != null)
//                {
//                    Message msg_read = gui_show.obtainMessage();
//                    msg_read.arg1 = SERIALMSG.MSG_READ;
//                    msg_read.obj = new String(new String(rdbuf));
//                    gui_show.sendMessage(msg_read);
//                }
//            }
//
//        }
//        
//        @Override
//        public synchronized void start() {
//            // TODO Auto-generated method stub
//            this.runflag = true;
//            super.start();
//        }
//    }
    
    private interface SERIALMSG
    {
        int MSG_READ = 1;
    }
    
    Handler gui_show = new Handler() {
        @Override
          public void handleMessage(Message msg) {
              // TODO Auto-generated method stub
              super.handleMessage(msg);
              switch(msg.arg1)
              {
                  case SERIALMSG.MSG_READ:
                      tread.setText(STRING_READ + (String)msg.obj);
                      break;
              }
          }  
   };
}
