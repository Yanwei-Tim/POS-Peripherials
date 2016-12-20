package android.AclasDemos;

import aclasdriver.AclasLcd0;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


//import java.io.File;





public class Aclas_Lcd0Activity extends Activity {
    private static final String TAG = "AclasArmPosDBG";
    protected Button mSendButton;
    protected Button mExitButton;
    protected Button mClearButton;
    protected Button mSendMatrix;
    protected Bitmap bLcdDotMatrix;
    protected ImageView iMap;
    private AclasLcd0 mAclasLcd0 = null;
    private int lcd0state = 0;
    private ImageView showview;
    /** Called when the activity is first created. */
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcd_main);
        
        Log.d(TAG, "Lcd--->onCreate");

        bLcdDotMatrix = ((BitmapDrawable) getResources().getDrawable(R.drawable.android_man)).getBitmap();
        mSendMatrix = (Button) this.findViewById(R.id.button_senddot);
        mSendButton = (Button) this.findViewById(R.id.button_send);
        mExitButton = (Button) this.findViewById(R.id.button_exit);
        mClearButton = (Button) this.findViewById(R.id.clear);
        mAclasLcd0 = new AclasLcd0();
        showview = (ImageView) findViewById(R.id.imageView_show); 
        
        addLisenner();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        if(mAclasLcd0.open() > 0)
        {
            lcd0state = 1;
        }else
        {
            return;
        }
        
        if(lcd0state > 0)
        {
            mAclasLcd0.SetBacklight(1);//on:1 off:0
            mAclasLcd0.SetContrast(4);//1~8
        }
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        
        if(lcd0state > 0)
        {
            mAclasLcd0.close();    
        }
        Log.d(TAG, "Lcd--->onPause");
    }           
    
    

    
    public void addLisenner() {
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {             
                // Send the message bytes and tell the Bluetooth to write
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }                
            }
        });
        
        mExitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { 
                Log.d(TAG, "lcd0 Exit");
                Aclas_Lcd0Activity.this.finish();
            }     
        });
        
        mClearButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ClearTheLcd();
                
            }
        });
        
        mSendMatrix.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "write bit map config = " + bLcdDotMatrix.getConfig().toString() + ",width = " + bLcdDotMatrix.getWidth() + ", height = " + bLcdDotMatrix.getHeight());
                
                    Bitmap testmap = Bitmap.createBitmap(mAclasLcd0.GetWidth(), mAclasLcd0.GetHeight(), Bitmap.Config.ARGB_8888);
                    
                    Canvas picCanvas = new Canvas(testmap);
                    picCanvas.drawColor(Color.WHITE);  
                    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    Typeface mType = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                    mPaint.setColor(Color.BLACK);
                    mPaint.setTextSize(16);
                    mPaint.setTypeface(mType);
                    
//                    picCanvas.drawBitmap(bLcdDotMatrix, 0, 0, mPaint);
                    picCanvas.drawText("Aclas Printer", 0, 32, mPaint);
//                    picCanvas.drawLine(0, 0, 143, 0, mPaint);
                        
                        
                    showview.setImageBitmap(testmap);
                    mAclasLcd0.WriteBitMap(testmap);
                    
            }
        });
    }
    static int page = 0;
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage()
    {
        // Get a message using content of the edit text widget
        TextView view = (TextView) findViewById(R.id.edit_text_out);
        String message = view.getText().toString();
        byte[] send = message.getBytes();
        if (message.length() > 0) 
        {   
            if(lcd0state > 0)
            {
                mAclasLcd0.writeMsg(send);//WRITE aclas
            } 
        }

    }     
    
    private void ClearTheLcd() {
        final byte clear[] ={0x0c};
        mAclasLcd0.writeMsg(clear);
    }
    
}