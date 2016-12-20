package android.AclasDemos;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;


public class AclasDemosActivity extends TabActivity implements OnTabChangeListener,OnGestureListener {
    public static final String tag = "AclasArmPosDBG";
    private CustomTabHost tabHost;
    private TabWidget tabWidget;
    private FrameLayout frameLayout;
    private GestureDetector gestureDetector;
       
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tabHost = (CustomTabHost) findViewById(android.R.id.tabhost);
        tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        
        Log.d(tag,"from the  tab begin");
//        LayoutInflater.from(this).inflate(R.layout.main,tabHost.getTabContentView(), true);
//        LayoutInflater.from(this.tabHost.getContext()).inflate(R.layout.main, null);
        tabHost.setup();  
        Log.d(tag,"from the  tab ok");       
//        Intent km3printer = new Intent().setClass(AclasDemosActivity.this, AclasKM3PrinterAcitivity.class);
//        Log.d(tag,"from tab1 begin");     
//        tabHost.addTab(tabHost.newTabSpec(getString(R.string.KM3Demo))
//                .setIndicator("tab1")
//                .setContent(km3printer));
//        Log.d(tag,"km3 tab add ok");
        
        Intent iprinter = new Intent().setClass(AclasDemosActivity.this, AclasPrinterActivity.class); 
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.printer_tab))
                .setContent(iprinter));
        
//        Intent irfid = new Intent().setClass(AclasDemosActivity.this, AclasRfidActivity.class);
//        
//        tabHost.addTab(tabHost.newTabSpec("tab2")
////                .setIndicator(getString(R.string.rfid_tab), res.getDrawable(R.drawable.rfid))
//                .setIndicator(getString(R.string.rfid_tab))
//                .setContent(irfid));        
//        Log.d(tag,"rfid tab add ok");
        
        Intent ilcd = new Intent().setClass(AclasDemosActivity.this, Aclas_Lcd0Activity.class);
        tabHost.addTab(tabHost.newTabSpec("tab3")
//                .setIndicator(getString(R.string.lcd_tab), res.getDrawable(R.drawable.lcd))
                .setIndicator(getString(R.string.lcd_tab))
                .setContent(ilcd));
        Log.d(tag,"lcd tab add ok");
        
        Intent imagcard = new Intent().setClass(AclasDemosActivity.this, AclasMagcardActivity.class);
        tabHost.addTab(tabHost.newTabSpec("tab4")
//                .setIndicator(getString(R.string.magcard_tab), res.getDrawable(R.drawable.magcard))
                .setIndicator(getString(R.string.magcard_tab))
                .setContent(imagcard));
        Log.d(tag,"mag card tab add ok");
        
        Intent ismartcard = new Intent().setClass(AclasDemosActivity.this, AclasSmartCardActivity.class);
        tabHost.addTab(tabHost.newTabSpec("tab5")
//                .setIndicator(getString(R.string.smartcard_tab), res.getDrawable(R.drawable.smartcardpng))
                .setIndicator(getString(R.string.smartcard_tab))
                .setContent(ismartcard));
        Log.d(tag,"smartcard tab add ok");
        
        Intent ihdq = new Intent().setClass(AclasDemosActivity.this, AclasHdqActivity.class);
        tabHost.addTab(tabHost.newTabSpec("tab6")
                .setIndicator(getString(R.string.hdq_tab))
                .setContent(ihdq));        
        Log.d(tag,"hdq tab add ok");
        Intent iserial = new Intent().setClass(AclasDemosActivity.this, AclasSerialAcitivity.class);
        tabHost.addTab(tabHost.newTabSpec("tab7")
                .setIndicator(getString(R.string.serial_tab))
                .setContent(iserial));   
//        
//        Log.d(tag,"usbrfid tab add ok");
//        Intent iusbrfid = new Intent().setClass(AclasDemosActivity.this, AclasUSBRFIDAcitivity.class);
//        tabHost.addTab(tabHost.newTabSpec("tab8")
//                .setIndicator(getString(R.string.USBRFID))
//                .setContent(iusbrfid));  
        
        
        Intent idrawer = new Intent().setClass(AclasDemosActivity.this, AclasDrawerAcitivity.class);
        tabHost.addTab(tabHost.newTabSpec("tab10")
                .setIndicator(getString(R.string.DrawerOpen))
                .setContent(idrawer));    

//        Intent iscale = new Intent().setClass(AclasDemosActivity.this, AclasScaleActivity.class);
//        tabHost.addTab(tabHost.newTabSpec("tab11")
//                .setIndicator(getString(R.string.scale))
//                .setContent(iscale));
        
        tabHost.setOnTabChangedListener(this);
        
        gestureDetector = new GestureDetector(this);
        new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        frameLayout = tabHost.getTabContentView();
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }
    private static final int FLEEP_DISTANCE = 120;
    private int currentTabID = 0;
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        // TODO Auto-generated method stub
//        Log.d(tag, "onFling e1x=" + e1.getX() + ",e2x=" + e2.getX());
//        if (e1.getX() - e2.getX() <= (-FLEEP_DISTANCE)) {//�������һ���
//            currentTabID = tabHost.getCurrentTab() + 1;
//            if (currentTabID >= tabHost.getTabCount()) {
//                currentTabID = 0;
//            }
//        } else if (e1.getX() - e2.getX() >= FLEEP_DISTANCE) {//�������󻬶�
//            currentTabID = tabHost.getCurrentTab() - 1;
//            if (currentTabID < 0) {
//                currentTabID = tabHost.getTabCount() - 1;
//            }
//        }
//        tabHost.setCurrentTab(currentTabID);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        if((e2 != null) && (e1 != null))
        {
            // TODO Auto-generated method stub
            Log.d(tag, "onScroll e1x=" + e1.getX() + ",e2x=" + e2.getX());
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if (gestureDetector.onTouchEvent(ev)) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public void onTabChanged(String tabId) {
        // TODO Auto-generated method stub
//        int tabID = Integer.valueOf(tabId);
//        for (int i = 0; i < tabWidget.getChildCount(); i++) {
//            if (i == tabID) {
//                tabWidget.getChildAt(Integer.valueOf(i)).setBackgroundColor(R.color.bule);
//            } else {
//                tabWidget.getChildAt(Integer.valueOf(i)).setBackgroundColor(R.color.white);
//            }
//        }        
    }     
}
