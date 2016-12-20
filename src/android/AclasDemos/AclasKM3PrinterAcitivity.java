package android.AclasDemos;


import aclasdriver.km3_printer;

import android.AclasDemos.AclasPrinterActivity.printerthread;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AclasKM3PrinterAcitivity extends Activity{
    
    private km3_printer mkm3_printer;
    private printerthread pThread;
    private boolean runflag = false;
    private int fd;
    private Button OpenButton;
    public static final String tag = "AclasArmPosDBG";
    final byte buf[]= {0x1b,0x61,0x01,0x1d ,0x77 ,0x02 ,0x1d ,0x48 ,0x01 ,0x1d ,0x66 
            ,0x00 ,0x1d ,0x68 , (byte) 0x80 ,0x1d ,0x6b ,0x49 ,0x14 ,0x7b ,0x42 ,0x41 ,0x42 ,0x43 ,0x44
            ,0x45 ,0x46 ,0x47 ,0x48 ,0x49 ,0x4a ,0x4b ,0x4c ,0x4d ,0x4e ,0x4f ,0x50 ,0x51 ,0x52
            ,0x0a };
    
    final byte barcodebuf[] = {0x1d,0x68,0x60,0x1d,0x48,0x02,0x1d,0x6b,0x02,0x36
    		,0x39,0x31,0x31,0x39,0x38,0x39,0x32,0x31,0x32,0x35,0x38,0x39,0x00};
    /*
    final byte testchar[] = { 0x1b, 0x21, 0x30,
    		'A', 'A','A','A','A', 'A', 'A','A','A','A', 'A', 'A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A',
            'B', 'B','B','B','B', 'B', 'B','B','B','B', 'B', 'B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B',
            'C', 'C','C','C','C', 'C', 'C', 'C','C','C','C', 'C','C','C','C', 'C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C',   
            'D', 'D','D','D','D','D', 'D','D','D','D','D', 'D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D',
            'E', 'E','E','E','E','E', 'E','E','E','E','E', 'E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E',
            'F', 'F','F','F','F','F', 'F','F','F','F','F', 'F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F',
            'a', 'a','a','a','a','a', 'a','a','a','a','a', 'a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a',
            'b', 'b','b','b','b','b', 'b','b','b','b','b', 'b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b',
            'c', 'c','c','c','c','c', 'c','c','c','c','c', 'c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c',
            'd', 'd','d','d','d','d', 'd','d','d','d','d', 'd','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d',
            'e', 'e','e','e','e','e', 'e','e','e','e','e', 'e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e',
            'f', 'f','f','f','f','f', 'f','f','f','f','f', 'f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f',
            '1', '1','1','1','1','1', '1','1','1','1','1', '1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1',
            '2', '2','2','2','2','2', '2','2','2','2','2', '2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2',
            '3', '3','3','3','3','3', '3','3','3','3','3', '3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3',
            '\n'
           };
           */
    final byte testchar[] = { 0x1b, 0x21, 0x20,
    		'A', 'A','A','A','A','A', 'A','A','A','A','A', 'A','A','A','A','A','A','A','A','A','A',
            'B', 'B','B','B','B','B', 'B','B','B','B','B', 'B','B','B','B','B','B','B','B','B','B',
            'C', 'C','C','C','C','C', 'C','C','C','C','C', 'C','C','C','C','C','C','C','C','C','C',   
            'D', 'D','D','D','D','D', 'D','D','D','D','D', 'D','D','D','D','D','D','D','D','D','D',
            'E', 'E','E','E','E','E', 'E','E','E','E','E', 'E','E','E','E','E','E','E','E','E','E',
            'F', 'F','F','F','F','F', 'F','F','F','F','F', 'F','F','F','F','F','F','F','F','F','F',
            'a', 'a','a','a','a','a', 'a','a','a','a','a', 'a','a','a','a','a','a','a','a','a','a',
            'b', 'b','b','b','b','b', 'b','b','b','b','b', 'b','b','b','b','b','b','b','b','b','b',
            'c', 'c','c','c','c','c', 'c','c','c','c','c', 'c','c','c','c','c','c','c','c','c','c',
            'd', 'd','d','d','d','d', 'd','d','d','d','d', 'd','d','d','d','d','d','d','d','d','d',
            'e', 'e','e','e','e','e', 'e','e','e','e','e', 'e','e','e','e','e','e','e','e','e','e',
            'f', 'f','f','f','f','f', 'f','f','f','f','f', 'f','f','f','f','f','f','f','f','f','f',
            '1', '1','1','1','1','1', '1','1','1','1','1', '1','1','1','1','1','1','1','1','1','1',
            //'2', '2','2','2','2','2', '2','2','2','2','2', '2','2','2','2','2','2','2','2','2','2',
            '3', '3','3','3','3','3', '3','3','3','3','3', '3','3','3','3','3','3','3','3','3','3',
            '3', '3','3','3','3','3', '3','3','3','3','3', '3','3','3','3','3','3','3','3','3','3',
            '\n'
           };
    
    final byte cutcmd[]    = {0x1d,0x56,0x00};
    final byte halfcutcmd[]    = {0x1d,0x56,0x01};
    final byte halfncutcmd[]    = {0x1d,0x56,0x42,0x20};
    final byte tmpfeed[]    = {0x1b,0x64,0x01,0xd,0xa};
    final byte feed[]    = {0xa,0xa};
    final byte statuscmd[]    = {0x10,0x04,0x01};
    final byte nopapercmd[]    = {0x1b,0x63,0x33,0x7};
    final byte density[]	= {0x12, 0x23, 0x2};
    final byte feed_cut[]	= {0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x1b,0x69};

    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.km3printer_main);
        
//        new View.OnKeyListener() {
//            
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // TODO Auto-generated method stub
//                
//                Log.i(tag, "zzn debug "+keyCode);
//           if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                           (keyCode == KeyEvent.KEYCODE_P)) 
//            {
//                   mkm3_printer.write(buf);
//                   return false;
//              }
//           return false;
//            }
//        };

        OpenButton = (Button) findViewById(R.id.PrinterButton);
        OpenButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stubs
                mkm3_printer.write(buf);
            	//mkm3_printer.write(barcodebuf);
                mkm3_printer.write(tmpfeed);
                mkm3_printer.write(halfcutcmd);
                
                mkm3_printer.write(testchar);
                mkm3_printer.write(tmpfeed);
                mkm3_printer.write(cutcmd);
            }
        });
        
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(tag, String.valueOf(event.getKeyCode()));
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_P)) {
                    mkm3_printer.write(buf);
                    return false;
        }
        return super.dispatchKeyEvent(event);
    } 
    

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
    	mkm3_printer.close();
    	runflag = false;
    	//pThread.destroy();
        super.onPause();
        Log.d(tag, "close the km3 printer\n");
        
    }
    
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mkm3_printer = new km3_printer();
        if((mkm3_printer.open()) < 0)
        {
           Log.d(tag, "open the km3 device error");
            return;
        }
        pThread = new printerthread();
        pThread.start();
        
       InitPrinter();
        
      
    }
    
    class printerthread extends Thread{
        
        int ret;
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while(runflag)
            {
                try {
                    sleep(1000*2);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ret = mkm3_printer.readstatus();
                if(ret == -1)
                {
                	Log.d(tag, "read status null!");
                    continue;
                }
                if (ret == 1 )
                {
                	Log.d(tag, "No paper!");
                	mkm3_printer.beep();
                }
                
            }
        }
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            runflag = true;
            super.start();
        }

		@Override
		public void destroy() {
			// TODO Auto-generated method stub
			runflag = false;
			Log.d(tag, "printerthread exit");
			super.destroy();
		}        
        
    }
    
    private void InitPrinter()
    {
    	 mkm3_printer.write(density);
         mkm3_printer.write(nopapercmd);    	
    }
}
