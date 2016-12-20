package android.AclasDemos;

import aclasdriver.aclasMagcardApi;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;



public class AclasMagcardActivity extends Activity {

//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//    	menu.add(0, 1, 1, R.string.exit);
//    	menu.add(0, 2, 2, R.string.about);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		if(item.getItemId() == 1)
//		{
//    		Log.d(TAG, "exit the app");
//    		MgcardAPI.close();
//    		finish();
//			
//		}
//		else if(item.getItemId() ==  2)
//		{
//			Log.d(TAG, "about nothing");
//			
//		}
//		return super.onOptionsItemSelected(item);
//	}

	/** Called when the activity is first created. */
    private static final String TAG = "AclasArmPosDBG";
	private TextView		MgText = null;
	private aclasMagcardApi MgcardAPI = null;
	private Button bMagcardE = null;
	private MyThread ThreadMagcardRead;
	
	private String[]  MagcardNo = new String [3];
	private String MAG_OPEN_ERR_STRING;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magcard_main);
        MgText = (TextView) this.findViewById(R.id.MagcardText);
        bMagcardE = (Button) this.findViewById(R.id.Btexit); 
        bMagcardE.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AclasMagcardActivity.this.finish();
            }
        });
        MgcardAPI = new aclasMagcardApi();
        
        MAG_OPEN_ERR_STRING = getString(R.string.macerr);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        ThreadMagcardRead = new MyThread();
        if(MgcardAPI.open() == 0)
        {
            Log.d(TAG,"Aclas debug"); 
            ThreadMagcardRead.start();
        }
        else
        {
            MgText.setText(MAG_OPEN_ERR_STRING);   
        }
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "Magcard ------> OnPause");
        ThreadMagcardRead.runflag = false;
        MgcardAPI.close();
        Log.d(TAG, "Magcard ------> OnPause LEAVE");
        super.onPause();
    }
    
    class MyThread extends Thread
    {
        public boolean runflag;
    	public void run()
    	{
    		int i;
    		 
    		while(runflag)
    		{
//    		    Message msg = new Message();    
//                msg = Readhandler.obtainMessage();
    		    Message msg = Readhandler.obtainMessage();
    		    if(!this.runflag) break;
    		    Log.d(TAG, "Magcard ------> Thread");
    		    
            	if( (i = MgcardAPI.read(MagcardNo)) > 0)
            	{
            		Log.d(TAG, "Send The Message to the ReadHandler");
            		Readhandler.sendMessage(msg);
            	}
            	else
            	{
            		Log.d(TAG, "Read error =========");
            		System.out.println(i);
            		Log.d(TAG, "Read error =========");
            	}    			
    		}

    	}
    	
    	@Override
    	public synchronized void start() {
    	    // TODO Auto-generated method stub
    	    this.runflag = true;
    	    super.start();
    	}
    } 
    
    //handler 
    Handler Readhandler =new Handler(){
    	
    	@Override
    	public void handleMessage (Message msg) {
    		// TODO Auto-generated method stub	
    		Log.d(TAG, "Show The Message to the textView");
    		MgcardAPI.beep();
    		MgText.setText("track1:"+MagcardNo[0]+"\n"+"track2:"+MagcardNo[1]+"\n"+"track3:"+MagcardNo[2]);
    		
    	}
    };
}
