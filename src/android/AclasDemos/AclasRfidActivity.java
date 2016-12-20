package android.AclasDemos;

import aclasdriver.rfid;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AclasRfidActivity extends Activity {
    private String CARD_NO_STRING;
    private String CARD_INFO_STRING;
    private String BLOCK_TEST_STRING;
    private final String TAG = "AclasArmPosDBG";
    private final String MONEY_STRING = " $";
    private static final int BLOCK_NUM_LEN = 16;
    private static final int MONEY_NUM_LEN = 9;
    private TextView tCardNo,tCardInfo,tBlockTest,tBlockTest1,tBlockTest2;
    private Button bRecharge,bExit,bInitCard,bDecharge;
    private EditText eMoney;
    private rfid mrfid = new rfid();
    private final byte[] defaulthead = {'m', 'o', 'n', 'e','y',':', ' ', '0','0','0', '0','0','0', '9','9','9'};
    private final char[] defaultheadchar = {'m', 'o', 'n', 'e','y',':', ' ', '0','0','0', '0','0','0', '9','9','9'};

    private final byte[] defaultpsw = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private enum RFID_EVT{ EVT_NOEVT, EVT_RECHARGE, EVT_DECHARGE, EVT_INITCARD, EVT_BLOCKTEST };
    private RFID_EVT rfidevt = RFID_EVT.EVT_NOEVT;
    private RfidThread rfidthread;
    private String gRechargeMoneyStr;
    private int TotalCardMoney;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rfid_main);

        CARD_NO_STRING    = getString(R.string.CardNo);
        CARD_INFO_STRING  = getString(R.string.CardInfo);
        BLOCK_TEST_STRING = getString(R.string.BlockTest);

        InitWidget();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mrfid.open();
        rfidthread = new RfidThread();
        rfidthread.start();
        Log.d(TAG, "Rfid -------> onResume");
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        rfidthread.runflag = false;
        mrfid.close();
        Log.d(TAG, "Rfid -------> onPause");
        super.onPause();
    }

   //handler
    Handler Rfidhandler = new Handler(){

        @Override
        public void handleMessage (Message msg) {
            // TODO Auto-generated method stub

            switch(msg.arg1)
            {
                case 0://show string
                  tCardNo.setText((String)msg.obj);
                  break;

                case 1:
                  tCardInfo.setText((String)msg.obj);
                  break;

                case 2:
                  tBlockTest.setText(BLOCK_TEST_STRING + " :" + (String)msg.obj);
                  break;

                case 3:
                  tBlockTest1.setText(BLOCK_TEST_STRING + "1:" + (String)msg.obj);
                  break;

                case 4:
                  tBlockTest2.setText(BLOCK_TEST_STRING + "2:" + (String)msg.obj);
                  break;

                default: break;
            }
        }
    };


    class RfidThread extends Thread
    {
        boolean runflag = true;
        int beepflag = 1;
        @Override
        public void run() {
            // TODO Auto-generated method stub
             while(runflag)
            {
                try {
                    sleep(100*3);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                if(!this.runflag) break;
                Log.d(TAG, "Rfid -------> Thread");

                //read card no
                Message msg =  Rfidhandler.obtainMessage();
                Message msg1 =  Rfidhandler.obtainMessage();
                Message msga =  Rfidhandler.obtainMessage();
                msga.arg1 = 2;
                Message msgb =  Rfidhandler.obtainMessage();
                msgb.arg1 = 3;
                Message msgc =  Rfidhandler.obtainMessage();
                msgc.arg1 = 4;                String CardNo = mrfid.ReadCardNo();
                if(CardNo == null)
                {
                    if(beepflag == 2)
                    {
                        
                        beepflag =1;
                        mrfid.beep();
                    }
                    Log.d(TAG, "card no null!");
                    rfidevt = RFID_EVT.EVT_NOEVT;
                    msg.arg1 = 0;
                    msg.obj  = new String(CARD_NO_STRING + " no card");
                    Rfidhandler.sendMessage(msg);

                    msg1.arg1 = 1;
                    msg1.obj  = new String(CARD_INFO_STRING + " ");
                    Rfidhandler.sendMessage(msg1);

                    msga.obj  = "null";
                    Rfidhandler.sendMessage(msga);
                    msgb.obj  = "null";
                    Rfidhandler.sendMessage(msgb);
                    msgc.obj  = "null";
                    Rfidhandler.sendMessage(msgc);
                    continue;
                }
                 
                if(beepflag == 1)
                {
                    beepflag = 2;
                    mrfid.beep();
                }
                msg.arg1 = 0;
                msg.obj  = new String(CARD_NO_STRING + CardNo);
                Rfidhandler.sendMessage(msg);
//                tCardNo.setText(CARD_NO_STRING + CardNo);


                //authorize card
                if(mrfid.AuthKey(RFID_SECTOR0, defaultpsw))
                {//empty card,set password first
                     Log.d(TAG, "Empty card");
                }
                else
                {
                    msg1.arg1 = 1;
                    msg1.obj  = new String(CARD_INFO_STRING + "Not empty Card");
                    Rfidhandler.sendMessage(msg1);
//                     Log.d(TAG, "Not empty Card");
                    rfidevt = RFID_EVT.EVT_NOEVT;
                     continue;
                }

                //read card
                byte[] rdbuf = mrfid.ReadCardBlock(RF_BLK_STORE_NO);
                if(rdbuf == null)
                {
                    Log.d(TAG, "read buffer null!");
                    continue;
                }

                boolean isinitcard = true;

                Message msg2 =  Rfidhandler.obtainMessage();
                boolean isheadright = true;
                for(int i=0; i<7; i++)
                {
                    if(rdbuf[i] != defaulthead[i])
                    {
                        isheadright = false;
                        break;
                    }
                }

                if(isheadright)
                {
//                    msg.arg1 = 1;
//                    msg.obj  = new String(CARD_INFO_STRING + "inited card");
//                    Rfidhandler.sendMessage(msg);
//                    tCardInfo.setText(CARD_INFO_STRING + "inited card");
                    Log.d(TAG, "array same");
                    boolean ismoneyright = true;
                    for(int i=7; i<BLOCK_NUM_LEN-1; i++)
                    {
                        if((rdbuf[i] > '9') || (rdbuf[i] < '0'))
                        {
                            msg2.arg1 = 1;
                            Log.d(TAG, i + "err, is " + rdbuf[i]);
                            msg2.obj  = new String(CARD_INFO_STRING + "inited card" + "money data err");
                            Rfidhandler.sendMessage(msg2);
                            ismoneyright = false;
                            break;
                        }
                    }
                    if(!ismoneyright)
                    {
                        rfidevt = RFID_EVT.EVT_NOEVT;
                        continue;
                    }

                    char[] showbuf = new char[MONEY_NUM_LEN];
                    for(int i=0; i<MONEY_NUM_LEN; i++) { showbuf[i] = (char) rdbuf[i+7]; }
                    StringBuilder showstr = new StringBuilder();
                    showstr.append(showbuf, 0, MONEY_NUM_LEN);
                    msg2.arg1 = 1;
                    msg2.obj  = new String(CARD_INFO_STRING + MONEY_STRING + showstr.toString());
                    Rfidhandler.sendMessage(msg2);
                    TotalCardMoney = Integer.valueOf(showstr.toString());
                }
                else
                {
                    msg2.arg1 = 1;
                    msg2.obj  = new String(CARD_INFO_STRING + "Not init card");
                    Rfidhandler.sendMessage(msg2);
                    isinitcard = false;
                    Log.d(TAG, "array not same");
//                    tCardInfo.setText(CARD_INFO_STRING + "Not init card");
                }



               //read card
                rdbuf = mrfid.ReadCardBlock(RF_BLK_STORE_NO1);
                if(rdbuf == null)
                {
                    msga.obj  = "null";
                    Rfidhandler.sendMessage(msga);
                }
                else
                {
                    StringBuilder str1 = new StringBuilder();
                    for(int i=0; i<rdbuf.length; i++)
                    {;
                        str1.append(Integer.toHexString(rdbuf[i] & 0x000000FF));
                    }
                    msga.obj  = new String(str1.toString());
                    Rfidhandler.sendMessage(msga);
                }

                //read card
                rdbuf = mrfid.ReadCardBlock(RF_BLK_STORE_NO2);
                if(rdbuf == null)
                {
                    msgb.obj  = "null";
                    Rfidhandler.sendMessage(msgb);
                }
                else
                {
                    StringBuilder str2 = new StringBuilder();
                    for(int i=0; i<rdbuf.length; i++)
                    {;
                        str2.append(Integer.toHexString(rdbuf[i] & 0x000000FF));
                    }
                    msgb.obj  = new String(str2.toString());
                    Rfidhandler.sendMessage(msgb);
                }

                //read card
                rdbuf = mrfid.ReadCardBlock(RF_BLK_STORE_NO3);
                if(rdbuf == null)
                {
                    msgc.obj  = "null";
                    Rfidhandler.sendMessage(msga);
                }
                else
                {
                    StringBuilder str3 = new StringBuilder();
                    for(int i=0; i<rdbuf.length; i++)
                    {
                        str3.append(Integer.toHexString(rdbuf[i] & 0x000000FF));
                    }
                    msgc.obj  = new String(str3.toString());
                    Rfidhandler.sendMessage(msgc);
                }


                //do user event
                Message msg3 =  Rfidhandler.obtainMessage();
                switch(rfidevt)
                {
                    case EVT_BLOCKTEST:

                        break;

                    case EVT_RECHARGE:
                        rfidevt = RFID_EVT.EVT_NOEVT;
                        if(isinitcard)
                        {
                            RechargeMoney(TotalCardMoney, true);
                        }
                        else
                        {
                            msg3.arg1 = 1;
                            msg3.obj  = new String(CARD_INFO_STRING + "Not init card" + ",can't not read");
                            Rfidhandler.sendMessage(msg3);
                        }
                        break;

                    case EVT_DECHARGE:
                        if(isinitcard)
                        {
                            RechargeMoney(TotalCardMoney, false);
                        }
                        else
                        {
                            msg3.arg1 = 1;
                            msg3.obj  = new String(CARD_INFO_STRING + "Not init card" + ",can't not read");
                            Rfidhandler.sendMessage(msg3);
                        }
                        rfidevt = RFID_EVT.EVT_NOEVT;
                        break;

                    case EVT_INITCARD:
                        rfidevt = RFID_EVT.EVT_NOEVT;
                        Log.d(TAG, "do_init_card_event");
                        InitCard();
                        break;

                    case EVT_NOEVT:
                        break;

                    default: break;
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

    public void InitWidget() {
        Init_tCardNo();
        Init_bRecharge();
        Init_bDecharge();
        Init_eMoney();
        Init_bExit();
        Init_tCardInfo();
        Init_bInitCard();
        Init_tBlockTest();
    }


    public void Init_tCardNo() {
        tCardNo    = (TextView) this.findViewById(R.id.textView_CardNo);
        tCardNo.setText(CARD_NO_STRING);
    }

    public void Init_tCardInfo() {
        tCardInfo    = (TextView) this.findViewById(R.id.textView_CardInfo);
        tCardInfo.setText(CARD_NO_STRING);
    }

    public void Init_bDecharge() {
        bDecharge = (Button) this.findViewById(R.id.button_Decharge);
        bDecharge.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "decharge ");
                gRechargeMoneyStr = eMoney.getText().toString();
                while(rfidevt != RFID_EVT.EVT_NOEVT);
                rfidevt = RFID_EVT.EVT_DECHARGE;
            }
        });
    }


    public void Init_bRecharge() {
        bRecharge = (Button) this.findViewById(R.id.button_Recharge);
        bRecharge.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "recharge ");
                gRechargeMoneyStr = eMoney.getText().toString();
                while(rfidevt != RFID_EVT.EVT_NOEVT);
                rfidevt = RFID_EVT.EVT_RECHARGE;
            }
        });
    }

    public void Init_bInitCard() {
        bInitCard = (Button) this.findViewById(R.id.button_InitCard);
        bInitCard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "button init card");
                while(rfidevt != RFID_EVT.EVT_NOEVT);
                rfidevt = RFID_EVT.EVT_INITCARD;
            }
        });
//        eMoney.set
    }


    public void Init_eMoney() {
        eMoney = (EditText) this.findViewById(R.id.editText_Money);
//        eMoney.set
    }

    public void Init_tBlockTest() {
        tBlockTest = (TextView)this.findViewById(R.id.textView_BlockTest);
        tBlockTest.setText(BLOCK_TEST_STRING);

        tBlockTest1 = (TextView)this.findViewById(R.id.textView_BlockTest1);
        tBlockTest1.setText(BLOCK_TEST_STRING + "1");
        tBlockTest2 = (TextView)this.findViewById(R.id.textView_BlockTest2);
        tBlockTest2.setText(BLOCK_TEST_STRING + "2");

    }

    public void Init_bExit() {
        bExit = (Button) this.findViewById(R.id.button_Exit);
        bExit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "rfid exit");
                AclasRfidActivity.this.finish();
            }
        });
    }
    private final int RFID_SECTOR0 = 5; //第5个扇区
    private final int RF_BLK_STORE_NO = (0+4*RFID_SECTOR0);//block adress
    private final int RF_BLK_STORE_NO1 = (1+4*RFID_SECTOR0);
    private final int RF_BLK_STORE_NO2 = (2+4*RFID_SECTOR0);
    private final int RF_BLK_STORE_NO3 = (3+4*RFID_SECTOR0);


    public int RechargeMoney(int oldvalue, boolean addorsub) {

        int newval;
        Message msg5 =  Rfidhandler.obtainMessage();
        Log.d(TAG, "do_recharge");
        String getstr = gRechargeMoneyStr;
        if(addorsub)
        {
            long testval = oldvalue;
            if((testval + Integer.valueOf(getstr)) > 999999999)
            {
                msg5.arg1 = 1;
                msg5.obj  = new String(CARD_NO_STRING + "overflow");
                Rfidhandler.sendMessage(msg5);
                return -1;
            }
            newval = oldvalue + Integer.valueOf(getstr);
        }else
        {
            newval = oldvalue - Integer.valueOf(getstr);
            if(newval < 0)
            {
                msg5.arg1 = 1;
                msg5.obj  = new String(CARD_NO_STRING + "not enough money");
                Rfidhandler.sendMessage(msg5);
                return -1;
            }
        }
        Log.d(TAG, "new val = " + newval);

        getstr = Integer.toString(newval);
        StringBuilder tmpstr = new StringBuilder();
        tmpstr.append(defaultheadchar, 0, 7);
        for(int i=getstr.length(); i<MONEY_NUM_LEN; i++)
        {
            tmpstr.append('0');
        }
        tmpstr.append(getstr);
        getstr = tmpstr.toString();

        byte rdbuf[] = getstr.getBytes();

        if(mrfid.WriteCardBlock(RF_BLK_STORE_NO, rdbuf))
        {
            Log.d(TAG, "write ok");
            msg5.arg1 = 1;
            msg5.obj  = new String(CARD_INFO_STRING + "recharge success");
            Rfidhandler.sendMessage(msg5);
            return 0;
        }else
        {
            Log.d(TAG, "write error");
            return -1;
        }
    }


    public boolean InitCard() {

        Message msg =  Rfidhandler.obtainMessage();

        if(mrfid.WriteCardBlock(RF_BLK_STORE_NO, defaulthead))
        {
            msg.arg1 = 1;
            msg.obj  = new String(CARD_INFO_STRING + "Init Card Success");
            Rfidhandler.sendMessage(msg);
            return true;
        }else
        {
            msg.arg1 = 1;
            msg.obj  = new String(CARD_INFO_STRING + "Init Card Error");
            Rfidhandler.sendMessage(msg);
            return false;
        }
    }
}




