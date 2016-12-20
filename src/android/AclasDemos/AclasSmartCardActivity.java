package android.AclasDemos;

import java.util.Timer;
import java.util.TimerTask;

import aclasdriver.SmartCard;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AclasSmartCardActivity extends Activity {
    private interface USED_CARD{
        int CARD_1 = 0, CARD_2 = 1;
    };
    
    private interface SMATR_CARD_TYPE{
        int PSAM = 0; int SLE4442 = 1;
    };
    

    private interface SLE4442_OP{
        int MAIN = 0x00000000;
        int PROTECT = 0x01000000;
        int PSC = 0x02000000;
        int COMP_PSC = 0x03000000;
    };
    
    private int OPEN_CARD_NO = USED_CARD.CARD_2;    
    public int THREAD_OPENED_CARD_NO = -1;
    private final String TAG = "AclasArmPosDBG";
    private String ATR_STRING;
    private TextView tTextView_ATR,ttextView_info;
    private Button bExit;
    private SmartcardThread smartcardtd = null;
    private SmartCard smartdrv = new SmartCard();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartcard_main);

        ATR_STRING = getString(R.string.ATR);

        InitWidget();
        smartcardtd = null; 
        
        RadioGroup but = (RadioGroup) findViewById(R.id.radioGroup_cardselect);
        but.check(R.id.radio_card2);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        smartcardtd = new SmartcardThread();
        smartcardtd.start();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "smart onPause");
        if(smartcardtd != null)
        {
            smartcardtd.runflag = false;
            try {
                smartcardtd.join();
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            smartcardtd = null;
        }

        super.onPause();
    }
    
    private void SendInfo(int what, Object msg) {
        Message smsg = new Message();
        smsg = ShowInfoHandler.obtainMessage(what, msg);
        ShowInfoHandler.sendMessage(smsg);
    }

    private void disp_resp(byte[] data)
    {
        int i;

        StringBuilder tmpstr = new StringBuilder();
        tmpstr.append("Response : [ ");
        if(data != null)
        {
            for(i=0;i<data.length;i++)
            {
                tmpstr.append(Integer.toHexString( (data[i] & 0x000000FF)) + "-");
            }
        }else
        {
            tmpstr.append("null ");
        }
        tmpstr.append("]");
        
        Log.d(TAG, tmpstr.toString());
        SendInfo(INFO_MSG.ADD_INFO, tmpstr.toString());
    }

    private void ShowLog(String str)
    {
        Log.d(TAG, str);
        SendInfo(INFO_MSG.ADD_INFO, str);
    }

    class SmartcardThread extends Thread{
        public boolean runflag = true;       
        public boolean card_exist = true;
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            int openf = smartdrv.Open(OPEN_CARD_NO);
            if(openf < 0)
            {
                ShowLog("open fail!, openf = " + openf);
                return;
            }else
            {
                ShowLog("open success");
            }
            
            
            THREAD_OPENED_CARD_NO = OPEN_CARD_NO;
            Log.d(TAG, "card no = " + THREAD_OPENED_CARD_NO);

            
            int cardtype = SMATR_CARD_TYPE.SLE4442;
            smartdrv.SetCardType(THREAD_OPENED_CARD_NO, cardtype);
            
            this.card_exist = false;
            SendInfo(INFO_MSG.NEW_INFO, "Please insert Sim Card"); 
            this.runflag = true;
            
            while(this.runflag)
            {
                int rc;
                
                if((smartdrv.IsCardRemoved(THREAD_OPENED_CARD_NO) != 0))
                {                   
                    if(this.card_exist)
                    {
                        SendInfo(INFO_MSG.NEW_INFO, "Please insert Sim Card");                
                        this.card_exist = false;
                    }
                }
                else if(!this.card_exist)
                {   
                    SendInfo(INFO_MSG.NEW_INFO, "Card Inserted");
                    card_exist = true;
                    
                    rc = smartdrv.TestCardReady(THREAD_OPENED_CARD_NO);
                    if(rc != 0)
                    {                    
                        ShowLog("Init Card Failed, rc = " + rc);    
                        try {
                            sleep(100*10);
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Log.d(TAG, "test car err");
                        continue;
                    }
                    
                    switch (cardtype)
                    {
	                    case SMATR_CARD_TYPE.SLE4442:
	                    	byte[]buf=new byte[0x100];
	                    	int pos;
	                    	
	                    	//read main ram from addr 0~255
	                    	pos = 0;
	                    	smartdrv.Pread(THREAD_OPENED_CARD_NO,buf,0x100,SLE4442_OP.MAIN | pos);
	                    	//check password (0xffffff)
	                    	//after check password we can write
	                    	buf[0]=(byte)0xff;
	                    	buf[1]=(byte)0xff;
	                    	buf[2]=(byte)0xff;
	                    	smartdrv.Pwrite(THREAD_OPENED_CARD_NO,buf,0x03,SLE4442_OP.COMP_PSC);

	                    	//change password
	                    	pos = 1;
	                    	buf[0]=(byte)0xff;
	                    	buf[1]=(byte)0xff;
	                    	buf[2]=(byte)0xff;
	                    	smartdrv.Pwrite(THREAD_OPENED_CARD_NO,buf,0x3,SLE4442_OP.PSC | pos);
	                    	
	                    	//read protect ram   (4byte must read together)
	                    	smartdrv.Pread(THREAD_OPENED_CARD_NO,buf,4,SLE4442_OP.PROTECT);
	                    	
	                    	//read error cnt password   (4byte must read together)
	                    	smartdrv.Pread(THREAD_OPENED_CARD_NO,buf,4,SLE4442_OP.PSC);
	                    	
	                    	//write to main ram addr 0x30~0x50 (0x20bytes)
	                    	pos = 0x30;
	                    	smartdrv.Pwrite(THREAD_OPENED_CARD_NO,buf,0x20,SLE4442_OP.MAIN | pos);
	                    	
	                    	//read from main ram addr 0x30~0x50 (0x20bytes)
	                    	smartdrv.Pread(THREAD_OPENED_CARD_NO,buf,0x20,SLE4442_OP.MAIN | pos);

	                    	byte[]dispbuf=new byte[0x10];
	                    	smartdrv.Pread(THREAD_OPENED_CARD_NO,dispbuf,0x10,SLE4442_OP.MAIN | 0);
	                    	disp_resp(dispbuf);
	                    	break;
	                    default:
		                    byte[] Atr = smartdrv.GetATR(THREAD_OPENED_CARD_NO);
		                    
		                    StringBuilder atr_str = new StringBuilder("[ "); 
		                    for(int i=0; i<Atr.length; i++)
		                    {
		                        atr_str.append(Integer.toHexString(Atr[i] & 0x000000FF) + "-");
		                    }
		                    atr_str.append("]");
		                    Message smsg = new Message();                
		                    smsg = ShowInfoHandler.obtainMessage(INFO_MSG.SHOW_ATR, atr_str.toString());
		                    ShowInfoHandler.sendMessage(smsg);
		                    ShowLog("Atr = " + atr_str.toString());
		                    
		                    ShowLog("Command  : sc_GetChallenge8");
		                    byte[]resp = smartdrv.GetChallenge8(THREAD_OPENED_CARD_NO);
		                    disp_resp(resp);
		                    
		                    ShowLog("Command sc_SelectFile [F0]");
		                    byte[]filename = { 0x3F, 0x00 };
		                    resp = smartdrv.SelectFile(THREAD_OPENED_CARD_NO, filename);
		                    disp_resp(resp);
		                    if((resp != null) && (resp[0] == 0x61))
		                    {
		                        resp = smartdrv.GetResponse(THREAD_OPENED_CARD_NO, resp[1]);
		                        disp_resp(resp);
		                    }
		                    break;
                    }
                }

                if(!this.runflag) 
                {
                    
                    break;
                }
                try {
                    sleep(100*10);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                
            }    
            
            smartdrv.Close(THREAD_OPENED_CARD_NO);

            Log.d(TAG, "thread close smart card");
        }
                
//
//                ShowLog("Command  : sc_SelectFile [4200]");
//                byte[]filename1 = { 0x42, 0x00 };
//                resp = smartdrv.SelectFile(THREAD_OPENED_CARD_NO,  filename1);
//                disp_resp(resp);
//                if((resp != null) && (resp[0] == 0x61))
//                {
//                    resp = smartdrv.GetResponse(THREAD_OPENED_CARD_NO,resp[1]);
//                    disp_resp(resp);
//                }
//
//                ShowLog("Command  : sc_VerifyPIN [12345678]");
//                byte[] pin = { '1', '2', '3', '4', '5', '6', '7', '8' };
//                resp = smartdrv.VerifyPIN(THREAD_OPENED_CARD_NO, (byte)0x81, pin);
//                disp_resp(resp);
////
//                ShowLog("Command  : sc_SelectFile [4204]");
//                byte[] filename11 = { 0x42, 0x04 };
//                resp = smartdrv.SelectFile(THREAD_OPENED_CARD_NO, filename11);
//                disp_resp(resp);
//                if((resp != null) && (resp[0] == 0x61))
//                {
//                    resp = smartdrv.GetResponse(THREAD_OPENED_CARD_NO, resp[1]);
//                    disp_resp(resp);
//                }
//
//                resp = smartdrv.ReadBinary_CUR(THREAD_OPENED_CARD_NO, (byte)0, (byte)0x10);
//                disp_resp(resp);

        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            this.runflag = true;
            super.start();
        }
    }

    private void InitWidget() {
        Init_tTextView_ATR();
        Init_bExit();
//        Init_bTest(); 
        Init_ttextView_info();
    }
    
    private void StartThread() throws InterruptedException {
        
        if(THREAD_OPENED_CARD_NO == OPEN_CARD_NO)
        {
            return;
        }
        if(smartcardtd != null)
        {
            smartcardtd.runflag = false;
            smartcardtd.join();
            Log.d(TAG, "thread join finish");
            smartcardtd = null;
        }
        
        smartcardtd = new SmartcardThread();
        smartcardtd.start();
    }
    
    private Timer tButton = null;
    
    public void onRadioButtonClicked1(View v) throws InterruptedException {
            // Perform action on clicks
//            RadioButton rb = (RadioButton) v;
            
            if(tButton != null)
            {
                tButton.cancel();
            }
            tButton = new Timer();            
            TimerTask taskButton = new TimerTask() {
                
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    tButton = null;
                    OPEN_CARD_NO = USED_CARD.CARD_1;
                    Log.d(TAG, "button delay task, OPEN_CARD_NO = " + OPEN_CARD_NO);
                    try {
                        StartThread();
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            tButton.schedule(taskButton, 500);
        
        }
    
    public void onRadioButtonClicked2(View v) throws InterruptedException {
        // Perform action on clicks
//        RadioButton rb = (RadioButton) v;
        if(tButton != null)
        {
            tButton.cancel();
        }
        tButton = new Timer();            
        TimerTask taskButton = new TimerTask() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                tButton = null;
                OPEN_CARD_NO = USED_CARD.CARD_2;
                Log.d(TAG, "button delay task, OPEN_CARD_NO = " + OPEN_CARD_NO);
                try {
                    StartThread();
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        tButton.schedule(taskButton, 500);
    }
    
    private void Init_tTextView_ATR() {
        tTextView_ATR = (TextView) findViewById(R.id.textView_ATR);
        tTextView_ATR.setText(ATR_STRING + "\n ");
    }

    private StringBuilder InfoStr = new StringBuilder();
    private void Init_ttextView_info() {
        ttextView_info = (TextView) findViewById(R.id.textView_info);        
    }
    
    private interface INFO_MSG{
        int ADD_INFO = 0, NEW_INFO = 1,SHOW_ATR=2;
    };
    Handler ShowInfoHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            
            switch(msg.what)
            {
                case INFO_MSG.ADD_INFO:
                    InfoStr.append((String)msg.obj + "\n");
                    ttextView_info.setText(InfoStr.toString());
                    break;
                    
                case INFO_MSG.NEW_INFO:
                    InfoStr = new StringBuilder();
                    InfoStr.append("Info: \n");
                    InfoStr.append((String)msg.obj + "\n");
                    ttextView_info.setText(InfoStr.toString());
                    break;
                    
                case INFO_MSG.SHOW_ATR:
                    tTextView_ATR.setText(ATR_STRING + (String)msg.obj);
                    break;
                    
                default: break;    
            }
            
        };
    };
    
    
//    private void Init_bTest() {
//        bTest = (Button) findViewById(R.id.button_Test);
//        bTest.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                Log.d(TAG, "smart card test, runflag = ");
//                
//                if(smartcardtd != null)
//                {
//                    smartcardtd.runflag = false;
//                    smartdrv.Close(THREAD_OPENED_CARD_NO);
//                    Log.d(TAG, "close smart card " + THREAD_OPENED_CARD_NO);
//                }
//                
//                smartcardtd = new SmartcardThread();
//                smartcardtd.start();
//            }
//        });
//    }
    
    private void Init_bExit() {
        bExit = (Button) findViewById(R.id.button_Exit);
        bExit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "smart card exit");
                AclasSmartCardActivity.this.finish();
            }
        });
    }
}