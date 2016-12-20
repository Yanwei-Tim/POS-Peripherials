package android.AclasDemos;

import aclasdriver.aclasHdqApi;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class AclasHdqActivity extends Activity {
    
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // TODO Auto-generated method stub
//        menu.add(0, 1, 1, R.string.exit);
//        menu.add(0, 2, 2, R.string.about);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // TODO Auto-generated method stub
//        if(item.getItemId() == 1)
//        {
//            Log.d(TAG, "exit the app");
//            finish();
//        }
//        else if(item.getItemId() ==  2)
//        {
//            Log.d(TAG, "about nothing");
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /** Called when the activity is first created. */
    private static final String TAG = "AclasArmPosDBG";
    private TextView tHdqView ;
    private String cardNo;
    private aclasHdqApi  aclasHdqApi;
    private MyThread threadHdqRead;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hdq_main);
        aclasHdqApi = new aclasHdqApi();
        tHdqView = (TextView) this.findViewById(R.id.hdqview);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "Hdq -------> onResume");
        threadHdqRead = new MyThread();
        if(aclasHdqApi.open() == 0)
        {
            threadHdqRead.start();
        }
        else
        {
            Log.d(TAG,"Can't open the HDQ device");
        }
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        threadHdqRead.stopflag = false;
        aclasHdqApi.close();
        Log.d(TAG, "Hdq -------> onPause");
        super.onPause();
    }
    
    
    class MyThread extends Thread
    {
        public boolean stopflag;
        public  String bytesToHexString(byte[] src){   
            StringBuilder stringBuilder = new StringBuilder("");   
            if (src == null || src.length <= 0) {   
                return null;   
            }   
            for (int i = 0; i < src.length; i++) {   
                int v = src[i] & 0xFF;   
                String hv = Integer.toHexString(v);   
                if (hv.length() < 2) {   
                    stringBuilder.append(0);   
                }   
                stringBuilder.append(hv);   
            }   
            return stringBuilder.toString();   
        }   
        
       public boolean ArrayCmp(byte[] src,byte[] des)
       {
           for (int i = 0; i < src.length; i++) {
               if(src[i] != des[i])
                   return false;
               
               System.out.print(src[i]);
               
           }
           return true;
           
           
       }
       
       public boolean ArrayZero(byte[] src)
       {
           for (int i = 0; i < src.length; i++) {
               if(src[i] != 0x00 )
                   return false;
           }
           return true;
           
           
       }
        
        
        public void run()
        {

//            Message msg = Readhandler.obtainMessage();
            byte[] Data = new byte [8];
            byte[] tmpData;
            
            while(stopflag)
            {
                Message msg = Readhandler.obtainMessage();
                 tmpData = aclasHdqApi.read();
                 
                 if(ArrayZero(tmpData))
                 {
                     Log.d(TAG, "no data");
                     msg.arg1 = 1;
                 }
                 else
                 {
                     msg.arg1 = 0;
                 }
                 if(ArrayCmp(tmpData,Data))
                 {
                     Log.d(TAG, "the same  data continue");
                     try {
                         sleep(500); //200ms
                     } catch (InterruptedException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     continue;
                 }
                 else
                 {
                     System.arraycopy(tmpData, 0, Data, 0, 8);
                     cardNo = bytesToHexString(Data);

                 }
                 Readhandler.sendMessage(msg);
                
                Log.d(TAG, "Hdq -------> Thread");
                try {
                    sleep(500); //200ms
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                if(!this.stopflag) break;
            }
        }
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            this.stopflag = true;
            super.start();
        }
    } 
    
    //handler 
    Handler Readhandler =new Handler(){
        
        @Override
        public void handleMessage (Message msg) {
            // TODO Auto-generated method stub  
            Log.d(TAG, "Show The Message to the textView");

            aclasHdqApi.beep();
            if(msg.arg1 == 1)
            {
                tHdqView.setText("DISCONNECT");
                msg.arg1 = 0;
            }
            else 
            {
                tHdqView.setText("CONNECT:"+cardNo);
            }
            
        }
    };
}