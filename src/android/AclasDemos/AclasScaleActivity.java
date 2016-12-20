package android.AclasDemos;

import java.io.File;
import java.io.IOException;

import aclasdriver.AclasScale;
import aclasdriver.AclasScale.ACLAS_SCALE_PROTOCOL_TYPE;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AclasScaleActivity extends Activity {
    private static final String tag = "AclasArmPosDBG";
    private AclasScale scale;
    private static boolean runflag;
    private Button bTare, bZero;
    private TextView tWei;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.d(tag, "scale oncreat");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scale_main);
        bTare = (Button) findViewById(R.id.button_tare);
        bTare.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                scale.SetTare();
            }
        });
        
        bZero = (Button) findViewById(R.id.button_zero);
        bZero.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                scale.SetZero();
            }
        });
        
        tWei = (TextView) findViewById(R.id.textView_wei);
        tWei.setText("------");
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(tag, "scale onresume");
        try {
            Log.d(tag, "scale new aclas scale");
            scale = new AclasScale(new File("/dev/ttyO1"), 9600, 0, ACLAS_SCALE_PROTOCOL_TYPE.scale_protocol_autoupload);
        }
        catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        
        Log.d(tag, "scale start run thread");
        AclasScaleActivity.runflag = true;
        scale_thread Scalethread = new scale_thread();
        Scalethread.start();        
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        AclasScaleActivity.runflag = false;
        scale.close();
    }
    
    class scale_thread extends Thread
    {
        public void run()
        {
            AclasScaleActivity.runflag = true;
            while(AclasScaleActivity.runflag)
            {                
                byte[] wei = scale.get_wei();
                   
                if(wei != null)
                {
                    Message msg_read = gui_show.obtainMessage();
                    msg_read.arg1 = 1;
                    msg_read.obj = new String(new String(wei));
                    gui_show.sendMessage(msg_read);
                }
             
//                try {
//                    sleep(1000);
//                }
//                catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
                
        }
        
        Handler gui_show = new Handler() {
            @Override
              public void handleMessage(Message msg) {
                  // TODO Auto-generated method stub
                  super.handleMessage(msg);
                  switch(msg.arg1)
                  {
                      case 1:
                          tWei.setText((String)msg.obj);
                          break;
                  }
              }  
        };        
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            super.start();
        }
    }    
}
