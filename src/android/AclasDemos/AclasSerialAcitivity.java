package android.AclasDemos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import aclasdriver.SerialPort;
import android.AclasDemos.AclasPrinterActivity.DotPrintPicture_OnItemSelectedListener;
import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class AclasSerialAcitivity extends Activity{
//    private enum SelectCom{
//        COM1,COM2,COM3;
//    }
//    private enum SelectComBoudRate{
//        R9600,R19200,R38400,R57600,R115200;
//    }
//    
    private SerialPort mserial = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private int fd;
    private TextView terial,tread;
    private String STRING_SERIALERR,STRING_READ;
    public static final String tag = "AclasArmPosDBG";
    public static boolean runflag;
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_main);
        
        terial = (TextView) findViewById(R.id.textView_serial);
        tread = (TextView) findViewById(R.id.textView_read);
        STRING_SERIALERR = getString(R.string.serialerr);
        STRING_READ = getString(R.string.serialread);
        tread.setText(STRING_READ);
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mserial != null)
            mserial.close();
        AclasSerialAcitivity.runflag =false;
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        try {
            mserial = new SerialPort(new File("/dev/ttyS3"), 9600, 0);
            mOutputStream = mserial.getOutputStream();
            mInputStream = mserial.getInputStream();
        }
        catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        AclasSerialAcitivity.runflag = true;
        SerialWriteThread tSerialWriteThread = new SerialWriteThread();
        tSerialWriteThread.start();
        
//        SerialReadThread tSerialReadThread = new SerialReadThread();
//        tSerialReadThread.start();
//        Log.d(tag, "serial read start\n");
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(tag, String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    } 
    
    class SerialWriteThread extends Thread
    {

        public void run()
        {
            byte[] rdbuf = new byte[200];
            while( AclasSerialAcitivity.runflag == true)
            {
               
                byte[] wrbuf = { 'S', 'e', 'r', 'i', 'a', 'l', ' ', 'T', 'e', 's', 't', '\n' };
                Log.d(tag, "serial output" + mOutputStream);
                if(mOutputStream != null)
                {
                    try {
                        mOutputStream.write(wrbuf);
                    }
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
                try {
                    sleep(1000);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                    
                Log.d(tag, "serial read thread\n");
                if(mInputStream != null)
                {
                    try {
                        int size = mInputStream.read(rdbuf);
                        Log.d(tag, "read" + size + "bytes");
                        if(size > 0)
                        {
                            byte[] sendbuf = new byte[size];
                            for(int i=0; i<size; i++) { sendbuf[i] = rdbuf[i]; }
                            Message msg_read = gui_show.obtainMessage();
                            msg_read.arg1 = SERIALMSG.MSG_READ;
                            msg_read.obj = new String(new String(sendbuf));
                            gui_show.sendMessage(msg_read);
                        }
                    }
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }

        }
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            super.start();
        }
    }
    
    
//    private Spinner sSerialProSet;
//    private void Init_sSerialProSet() {
//        sSerialProSet = (Spinner) findViewById(R.id.serial_com);
//        ArrayAdapter<CharSequence> adapter_DotPrintPicture = ArrayAdapter.createFromResource(
//                this, R.array.DotModePic, android.R.layout.simple_spinner_item);
//        adapter_DotPrintPicture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sSerialProSet.setAdapter(adapter_DotPrintPicture);
//        sSerialProSet.setOnItemSelectedListener(new DotPrintPicture_OnItemSelectedListener());
//        
//    }
//    
//    public class DotPrintPicture_OnItemSelectedListener implements OnItemSelectedListener {
//
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int pos,
//                long id) {
//            // TODO Auto-generated method stub
//            for(DOT_MODE_SHARP i: DOT_MODE_SHARP.values())
//            {
//                switch(pos)
//                {
//                    case 1:
//                        DotModeSharp = DOT_MODE_SHARP.horizontal;
//                        showview.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
//                        break;
//                    
//                    case 2:
//                        DotModeSharp = DOT_MODE_SHARP.triangle;
//                        showview.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
//                        break;
//                        
//                    case 3:
//                        DotModeSharp = DOT_MODE_SHARP.picture;
//                        showview.setImageBitmap(PrnBitMap);
//                        break;
//                        
//                    case 4:
//                        DotModeSharp = DOT_MODE_SHARP.picture_text;
//                        showview.setImageBitmap(PrnTextBitMap);
//                        break;
//                        
//                    case 0:
//                        DotModeSharp = DOT_MODE_SHARP.vertical;
//                        break;
//                 
//                    default: break;    
//                }
//            }
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> arg0) {
//            // TODO Auto-generated method stub
//            Log.d(tag, "nothing select");
//        }
//     }
//    class SerialReadThread extends Thread
//    {
//        public boolean runflag;
//        public void run()
//        {
//            byte[] rdbuf;
//            while(this.runflag)
//            {
//                if(fd < 0) 
//                {
//                    runflag = false;
//                    break;
//                }
//                
//                Log.d(tag, "serial read thread\n");
//                rdbuf = mserial.read(100);
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
                      tread.setText(STRING_READ + ": " + (String)msg.obj);
                      break;
              }
          }  
   };
}
