package android.AclasDemos;

import android.AclasDemos.AclasRfidActivity;
import aclasdriver.printer;
import android.AclasDemos.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class AclasPrinterActivity extends Activity {
    private enum DOT_MODE_SHARP{
        vertical, horizontal, triangle, picture, picture_text, picture_bmp;
    }
    public static ViewGroup cPrinter;
    private printerthread pThread;
    private DOT_MODE_SHARP DotModeSharp = DOT_MODE_SHARP.vertical;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printer_main);
        cPrinter = (ViewGroup) findViewById(R.id.container_printer);
        Log.d(tag, "Printer ---> onCreate, width = " + cPrinter.getWidth() + ",height = " + cPrinter.getHeight());
        //
        InitString();
        mprinter = new printer();
        InitWidGet();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        int retopen = mprinter.Open();
        mprinter.SetContrast(Contrast);
        mprinter.SetPrintMode(print_mode);
        Log.d(tag, "Printer ---> onResume open print, retopen = " + retopen);
        pThread = new printerthread();
        pThread.start();
        
//        if(AclasRfidActivity.container_rfid != null)
//        {
//            Rotate3d leftAnimation = new Rotate3d(-0, -90, 0, 0, AclasRfidActivity.container_rfid.getWidth()/2, AclasRfidActivity.container_rfid.getHeight()/2);   
//            leftAnimation.setFillAfter(true);
//            leftAnimation.setDuration(2000);   
//            AclasRfidActivity.container_rfid.startAnimation(leftAnimation);
//        }
//        Rotate3d rightAnimation = new Rotate3d(-0+90, -90+90, 0.0f, 0.0f, cPrinter.getWidth()/2, cPrinter.getHeight()/2);   
//        rightAnimation.setFillAfter(true);   
//        rightAnimation.setDuration(500);   
//        cPrinter.startAnimation(rightAnimation);  
    }
    
  
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        pThread.runflag = false;
        mprinter.Close();
        Log.d(tag, "Printer ---> onPause");
        mprinter.Stop();
        super.onPause();
        Log.d(tag, "Printer ---> onPause, width = " + cPrinter.getWidth() + ",height = " + cPrinter.getHeight());
    }
    
    private interface MSG_TYPE
    {
        int MSG_DOTWIDTH = 1, MSG_PAPERSTATUS = 2;
    }
    
    Handler gui_show = new Handler() {
      @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch(msg.arg1)
            {
                case MSG_TYPE.MSG_DOTWIDTH:
                    tDotWidth.setText((String)msg.obj);
                    break;
                    
                case MSG_TYPE.MSG_PAPERSTATUS:
                    tPaperStatus.setText((String)msg.obj);
                    break;
            }
        }  
    };
    
    private int dotwidth = -1;
    class printerthread extends Thread{
        boolean runflag = false;
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while(runflag)
            {
                try {
                    sleep(100*5);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                if(!runflag) break;
//                Log.d(tag, "Printer ---> Thread Run");
                dotwidth = mprinter.GetDotWidth();
                
                Message msg_dotwidth = gui_show.obtainMessage();
                msg_dotwidth.arg1 = MSG_TYPE.MSG_DOTWIDTH;
                msg_dotwidth.obj = new String(DOTWIDTH_STRING + dotwidth);
                gui_show.sendMessage(msg_dotwidth);
                
                
                Message msg_paperstatus = gui_show.obtainMessage();
                msg_paperstatus.arg1 = MSG_TYPE.MSG_PAPERSTATUS;
                if(mprinter.IsPaperExist())
                {
                    msg_paperstatus.obj = new String(PAPER_STATUS_STRING + HAVE_PAPER_STRING);
                }
                else
                {
                    msg_paperstatus.obj = new String(PAPER_STATUS_STRING + NO_PAPER_STRING);
                }
                gui_show.sendMessage(msg_paperstatus);
            }
        }
        
        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            runflag = true;
            super.start();
        }        
    }
    
    private printer mprinter;
    private final String tag = "AclasArmPosDBG";
    private String HAVE_PAPER_STRING,NO_PAPER_STRING,DOTWIDTH_STRING,MODE_STRING,PAPER_STATUS_STRING,
                    EPSONMODE_STRING,DOTMODE_STRING,CONTRAST_STRING;
    private void InitString() {
        HAVE_PAPER_STRING = getString(R.string.havepaper);
        NO_PAPER_STRING   = getString(R.string.nopaper);
        DOTWIDTH_STRING = getString(R.string.dotwidth);
        MODE_STRING = getString(R.string.mode);
        PAPER_STATUS_STRING = getString(R.string.paperstatus);
        EPSONMODE_STRING = getString(R.string.epsonmode);
        DOTMODE_STRING = getString(R.string.dotmode);
        CONTRAST_STRING = getString(R.string.contrast);
    }
    
    private TextView tDotWidth, tMode, tPaperStatus, tContract;
    private void InitWidGet() {
        InitTextView();
//        Init_bGetDotWidth();
        Init_bChangeMode();
//        Init_bGetPaperStatus();
        Init_bFeed();
        Init_bStop();
        Init_bConitnue();
        Init_bExit();
        Init_sbContrast();
        Init_sDotPrintPicture();
        Init_bPrint();
        InitBitMap();
        InitColorBitMap();
    }
    
    private class receiptline
    {
        String Text;
        int TextSize;
        receiptline(String text, int size) {
            this.Text = text;
            this.TextSize = size;
        }
    }
   
    private receiptline[] receiptlineTbl =
    {
    		new receiptline("    Aclas", 80), 
            new receiptline("日期:xx.xx.xx 时间xx.xx 序号:0001", 25), 
            new receiptline("名称                         ", 25), 
            new receiptline(" - - - - - - - - - - - - - - - -", 25), 

            new receiptline("繁體   蘋果                   ", 30),
            new receiptline("英语   apple                    ", 30),
            new receiptline("马其顿 Идентиф               ", 30), 
            new receiptline("希腊   αβγδεζηθι               ", 30),
            
            new receiptline("日本   あいきさけおけしずだぢ ", 30),

            new receiptline("                  ", 30),
            new receiptline("繁體   蘋果                   ", 40),
            new receiptline("英语   apple                    ", 40),
            new receiptline("马其顿 Идентиф               ", 40), 
            new receiptline("希腊   αβγδεζηθι               ", 40),
            new receiptline("日本   あいきさけおけしずだぢ ", 40),

            new receiptline(" - - - - - - - - - - - - - - - -", 25), 
            new receiptline("合计 9     ￥155.5", 50), 

    };
    
    private Bitmap PrnBitMap;
    private Bitmap PrnTextBitMap;
    private Bitmap ColorBitMap;
    private Canvas TextCanvas;
    private ImageView showview;
    private void InitBitMap() {        
        BitmapDrawable bmpDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.logo);  
        Bitmap icbitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.logo)).getBitmap();
        
        icbitmap = icbitmap.copy(Bitmap.Config.ALPHA_8, false);
        PrnBitMap = bmpDraw.getBitmap();  
        
        Log.d(tag, "height = " + PrnBitMap.getHeight() + " width = " + PrnBitMap.getWidth());
        
        int receiptwide = 384;
        
        int y_pixel = icbitmap.getHeight() + 30 + 200;
        for(int i=0; i<receiptlineTbl.length; i++)
        {
            y_pixel += receiptlineTbl[i].TextSize;
        }
        
        Log.d(tag, "y_pixel = " + y_pixel);
        PrnTextBitMap = Bitmap.createBitmap(receiptwide, y_pixel, Bitmap.Config.ARGB_8888);//create a single channel bitmap
        TextCanvas = new Canvas(PrnTextBitMap);
        TextCanvas.drawColor(Color.WHITE);  
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Typeface mType = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(30);
        mPaint.setTypeface(mType);
        
        Log.d(tag, "icbitmap.getHeight() = " + icbitmap.getHeight());
        TextCanvas.drawBitmap(icbitmap, 25, 0, mPaint);
        y_pixel = icbitmap.getHeight() + 30;
        for(int i=0; i<receiptlineTbl.length; i++)
        {
            mPaint.setTextSize(receiptlineTbl[i].TextSize);
            y_pixel += receiptlineTbl[i].TextSize;
            TextCanvas.drawText(receiptlineTbl[i].Text, 0, y_pixel, mPaint);
        }
        
        showview = (ImageView) findViewById(R.id.imageView_show); 
//        showview.setImageBitmap(PrnTextBitMap);
    }
    
    private void InitColorBitMap()
    {        
        ColorBitMap = ((BitmapDrawable)getResources().getDrawable(R.drawable.pow)).getBitmap().copy(Bitmap.Config.ARGB_8888, false);
        showview = (ImageView) findViewById(R.id.imageView_show); 
    }
    
    private void InitTextView() {
        tDotWidth = (TextView) findViewById(R.id.textView_dotwidth);
        tMode = (TextView) findViewById(R.id.textView_mode);
        showmode(print_mode);
        tPaperStatus = (TextView) findViewById(R.id.textView_paperstatus);
        tContract = (TextView) findViewById(R.id.textView_contrast);
        tContract.setText(CONTRAST_STRING + Contrast);
    }
    
    private Button bChangeMode, bFeed, bStop, bContinue, bExit,bPrint;   
    private void EpsonModePrint() {
        final byte Aligns[] = { 0x1b, 0x21, 0x00,0x1b, 0x61, 0x02, 'A', 'c', 'l', 'a', 's','\n'};
        final byte aclas[] = {'A', 'c', 'l', 'a', 's', '\n' };
        final byte aclas_big[] = { 0x1b, 0x21, (byte) 0xb0, '-', '-', '-', '-', '-', 'A', 'c', 'l', 'a', 's', '-', '-', '-', '-', '-', '-', '\n' };
        final byte cmd_1[] = {0x1d ,0x68 ,0x41};//CHOOSE THE BARCODE HEIGHT AS 0x35(max = 0x41)
        final byte cmd_2[] = {0x1d ,0x48 ,0x03};//CHOOSE THE PRINTING POSITION OF BARCODE VALUE 0x1 UPPER 0x02 LOWER 0x03 UPPER AND LOWER
        final byte cmd_3[] = {0x1d ,0x77 ,0x2};//CHOOSE THE BARCODE WIDTH AS 0x2(max = 0x8)
        final byte BAR128[]= {0x1d ,0x6b  ,0x49 ,0x08 ,0x37 ,0x31  ,0x31 ,0x32 ,0x33 ,0x34 ,0x37 ,0x38};//71123478
        final byte BARITF[]= {0x1d ,0x6b  ,0x46 ,0x04 ,0x37 ,0x31  ,0x31 ,0x32 ,0x0d ,0x0a};
        final byte BAREN8[]= {0x1d ,0x6b  ,0x03 ,0x34 ,0x37 ,0x31  ,0x31 ,0x32 ,0x33 ,0x34  ,0x37 ,0x38 ,0x39 ,0x30  ,0x38 ,0x39 ,0x00 ,0x0d ,0x0a};//en8 4 711234 789089
        final byte BARUPCA[]= {0x1d ,0x6b ,0x00 ,0x30 ,0x31 ,0x32  ,0x34 ,0x35 ,0x36 ,0x37  ,0x38 ,0x39 ,0x30 ,0x30   ,0x00, 0x0d ,0x0a};//012345 678905
        final byte BAREN13[]= {0x1d ,0x6b ,0x02 ,0x39 ,0x37 ,0x38  ,0x37 ,0x31 ,0x31 ,0x35  ,0x31 ,0x34 ,0x35 ,0x35,0x34,0x33 ,0x00 , 0x0d ,0x0a};//9 787115 145543
//        final byte cutcmdstep[]= {0x1d,0x56,0x41,0x2};
        final byte cutcmd[]    = {0x1d,0x56,0x00};   
        final byte halfcutcmd[]    = {0x1d,0x56,0x01};  
        final byte tmpfeed[]    = {0xd,0xa};    
        final byte testchar[] = { 0x1b, 0x21, 0x00,
                                  'A', 'A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A',
                                  'B', 'B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B',
                                  'C', 'C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C',   
                                  'D', 'D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D',
                                  'E', 'E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E',
                                  'F', 'F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F',
                                  'a', 'a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a','a',
                                  'b', 'b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b','b',
                                  'c', 'c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c','c',
                                  'd', 'd','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d','d',
                                  'e', 'e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e','e',
                                  'f', 'f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f','f',
                                  '1', '1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1',
                                  '2', '2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2',
                                  '3', '3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3',
                                  '\n'
                                 };
        
        for(int j=0;j<10;j++)
        	mprinter.Write(tmpfeed);
        
        for(int j=0;j<2;j++)
        {   
            mprinter.Write(Aligns);
            mprinter.Write(aclas);
            mprinter.Write(testchar);
           
            mprinter.Write(aclas_big);

            //BARCODE PARAMETER
            mprinter.Write(cmd_1);
            mprinter.Write(cmd_2);
            mprinter.Write(cmd_3);
            
                //PRINT BARCODE
            mprinter.Write(BAR128);
            mprinter.Write(BARITF);
            mprinter.Write(BAREN8);
            mprinter.Write(BARUPCA);
            mprinter.Write(BAREN13);

            for(int i = 0;i<30;i++)
            {
                byte[] tmpbuf = { 0x0d, 0x0a };
                mprinter.Write(tmpbuf);
            }
            if(j == 0)
            	mprinter.Write(halfcutcmd);
            mprinter.Write(cutcmd);
        }
//        mprinter.Write(cutcmd);
    }
    
    private void printspaceline() {
        byte[] wrbuf = new byte[384/8];
        for(int i=0; i<wrbuf.length; i++) wrbuf[i] = 0;
        for(int i=0; i<50; i++) mprinter.Write(wrbuf);
    }
    
//    public Bitmap toGrayscale(Bitmap bmpOriginal) {
//        int width, height;
//        height = bmpOriginal.getHeight();
//        width = bmpOriginal.getWidth();
//
//        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
//                Bitmap.Config.ALPHA_8);
//        Canvas c = new Canvas(bmpGrayscale);
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//        paint.setColorFilter(f);
//        c.drawBitmap(bmpOriginal, 0, 0, paint);
//        return bmpGrayscale;
//    }
    private void picutre_bmp_print()
    {
        int height = ColorBitMap.getHeight(); 
        int width = ColorBitMap.getWidth();
        int byteofline = (width + 7)/8;
        byte[] BitMapBuf = new byte[byteofline];
        int[] tmpbuf = new int[width + 8];
        
        for(int i=0; i<height; i++)
        {
            ColorBitMap.getPixels(tmpbuf, 0, width, 0, i, width, 1);
            
            for(int j=0; j<width; j+=8)
            {
                for(int k=0; k<8; k++)
                {
                    if(i < 1)
                    {                        
                        Log.d(tag, "pos (" + (j+k) + "," + i + ") = " + Integer.toHexString(tmpbuf[j+k]));
                    }
                    if((tmpbuf[j+k] == Color.TRANSPARENT) || (tmpbuf[j+k] == Color.WHITE))
                    {
                        BitMapBuf[j/8] &= ~(0x80 >> k);
                    }else
                    {
                        BitMapBuf[j/8] |= (0x80 >> k);
                    }
                }
            }
            mprinter.Write(BitMapBuf);
        }               

    }
    
    
    private void DotModePrint() {
        final int BYTE_OF_LINE = this.dotwidth/8;
        
        Log.d(tag, "DotModePrint :sharp is " + this.DotModeSharp);
        switch(this.DotModeSharp)
        {
            case vertical:
                byte[] VerticalBuf = new byte[BYTE_OF_LINE];
                for(int i=0; i<VerticalBuf.length; i++) VerticalBuf[i] = (byte) 0x80;
                
                for(int j=0; j<600; j++)
                {
                    mprinter.Write(VerticalBuf);
                }
                break;
                
            case horizontal:
                byte[] HorizontalBuf = new byte[BYTE_OF_LINE];
                for(int i=0; i<HorizontalBuf.length; i++) HorizontalBuf[i] = (byte) 0xFF;
                byte[] HorizontalBuf1 = new byte[BYTE_OF_LINE];
                for(int i=0; i<HorizontalBuf1.length; i++) HorizontalBuf1[i] = (byte) 0x00;
                
                for(int j=0; j<600/4; j++)
                {
                    mprinter.Write(HorizontalBuf);
                    mprinter.Write(HorizontalBuf1);
                    mprinter.Write(HorizontalBuf1);
                    mprinter.Write(HorizontalBuf1);

                    mprinter.Write(HorizontalBuf1);
                    mprinter.Write(HorizontalBuf1);
                    mprinter.Write(HorizontalBuf1);
                    mprinter.Write(HorizontalBuf1);
                }
                break;
                
            case triangle:
                final byte[][] trangledotline = 
                { 
                     { 0x01, 0x00 },
                     { 0x03, 0x00 },
                     { 0x05, 0x00 },
                     { 0x09, 0x00 },
                     
                     { 0x11, 0x00 },
                     { 0x21, 0x00 },
                     { 0x41, 0x00 },
                     { (byte) 0x81, 0x00 },
                     
                     { 0x01, 0x01 },
                     { 0x01, 0x02 },
                     { 0x01, 0x04 },
                     { 0x01, 0x08 },
                     
                     { 0x01, 0x10 },
                     { 0x01, 0x20 },
                     { 0x01, 0x40 },
                     { 0x01, (byte) 0x80 },   
                };
                
                byte[] wrbuf = new byte[384/8];
                
                for(int k=0; k<60; k++)
                {
                    for(int j=0; j<trangledotline.length; j++)
                    {
                        for(int i=0; i<384/8; i+=2)
                        {
                            wrbuf[i] = trangledotline[j][0];
                            wrbuf[i+1] = trangledotline[j][1];
                        }
                        mprinter.Write(wrbuf);
                    }
                }
                break;
            case picture:
                int height = PrnBitMap.getHeight(); 
                int width = PrnBitMap.getWidth();
                int byteofline = (width + 7)/8;
                byte[] BitMapBuf = new byte[byteofline];
                int[] tmpbuf = new int[width + 8];
                
                for(int i=0; i<height; i++)
                {
                    PrnBitMap.getPixels(tmpbuf, 0, width, 0, i, width, 1);
                    
                    for(int j=0; j<width; j+=8)
                    {
                        for(int k=0; k<8; k++)
                        {
                            if(tmpbuf[j+k] == 0xFF000000)
                            {
                                BitMapBuf[j/8] |= (0x80 >> k);
                            }else
                            {
                                BitMapBuf[j/8] &= ~(0x80 >> k);
                            }
                        }
                    }
                    mprinter.Write(BitMapBuf);
                }
                break;
                
            case picture_bmp:
                picutre_bmp_print();
                break;
                
            case picture_text:
                int height1 = PrnTextBitMap.getHeight(); 
                int width1 = PrnTextBitMap.getWidth();
                int byteofline1 = width1/8;
                byte[] BitMapBuf1 = new byte[byteofline1];
                int[] tmpbuf1 = new int[width1];
                
                for(int i=0; i<height1; i++)
                {
                    PrnTextBitMap.getPixels(tmpbuf1, 0, width1, 0, i, width1, 1);
                    
                    for(int j=0; j<width1; j+=8)
                    {
                        for(int k=0; k<8; k++)
                        {
                            if(tmpbuf1[j+k] == 0xFF000000)
                            {
                                BitMapBuf1[j/8] |= (0x80 >> k);
                            }else
                            {
                                BitMapBuf1[j/8] &= ~(0x80 >> k);
                            }
                        }
                    }
                    mprinter.Write(BitMapBuf1);
                }
                break;    
                
                default: return;
        }
        
        printspaceline();
//        mprinter.SetPrintMode(0);
//        for(int i = 0;i<30;i++)
//        {
//            byte[] tmpbuf = { 0x0d, 0x0a };
//            mprinter.Write(tmpbuf);
//        }
//        mprinter.Write(halfcutcmd);
//        mprinter.SetPrintMode(1);
    }
    
    private void Init_bPrint() {
        bPrint = (Button) findViewById(R.id.button_print);
        bPrint.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(print_mode == 0)
                {//epson mode
                    EpsonModePrint();
                }
                else
                {//dot mode
                    Log.d(tag, "dot print");
                    DotModePrint();
                }
            }
        });
        
    }
    
    private int print_mode = 0;//0 epson/1 dot
    private void showmode(int mode) {
        switch(mode)
        {
            case 0:
                tMode.setText(MODE_STRING + EPSONMODE_STRING);
                break;
                
            default:
                tMode.setText(MODE_STRING + DOTMODE_STRING);
                break;
        }
    }
    private void Init_bChangeMode() {
        bChangeMode = (Button) findViewById(R.id.button_changemode);
        bChangeMode.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(tag, "change mode");
                print_mode = 1 - print_mode;
                showmode(print_mode);
                mprinter.SetPrintMode(print_mode);
            }
        });
    }
       
    private void Init_bFeed() {
        bFeed = (Button) findViewById(R.id.button_feed);
        bFeed.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int feedret = mprinter.Feed(1000);
                Log.d(tag, "feed, ret = " + feedret);
            }
        });
    }

    private void Init_bConitnue() {
        bContinue = (Button) findViewById(R.id.button_continue);
        bContinue.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mprinter.Conitnue();
            }
        });
    }

    private void Init_bStop() {
        bStop = (Button) findViewById(R.id.button_stop);
        bStop.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mprinter.Stop();
                Log.d(tag, "stop");
            }
        });
    }
    
    private void Init_bExit() {
        bExit = (Button) findViewById(R.id.button_exit);
        bExit.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(tag, "exit");
                AclasPrinterActivity.this.finish();
            }
        });
    }
    
        
    private SeekBar sbContrast;
    private final int DEFAULT_CONTRAST = 3;
    private int Contrast = DEFAULT_CONTRAST;
    private final int MIN_CONTRAST = 1;
    private final int MAX_CONTRAST = 8;
    private void Init_sbContrast() {
        sbContrast = (SeekBar) findViewById(R.id.seekBar1);
        sbContrast.setMax(MAX_CONTRAST - MIN_CONTRAST);
        sbContrast.setProgress(DEFAULT_CONTRAST - MIN_CONTRAST);
        mprinter.SetContrast(Contrast);

        sbContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                Log.d(tag, "set Contrast = " + Contrast);
                tContract.setText(CONTRAST_STRING + Contrast);
                mprinter.SetContrast(Contrast);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                Log.d(tag, "progress = " + progress);
                Contrast = progress + MIN_CONTRAST;
                tContract.setText(CONTRAST_STRING + Contrast);
            }
        });
    }
    
    private Spinner sDotPrintPicture;
    private void Init_sDotPrintPicture() {
        sDotPrintPicture = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter_DotPrintPicture = ArrayAdapter.createFromResource(
                this, R.array.DotModePic, android.R.layout.simple_spinner_item);
        adapter_DotPrintPicture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDotPrintPicture.setAdapter(adapter_DotPrintPicture);
        sDotPrintPicture.setOnItemSelectedListener(new DotPrintPicture_OnItemSelectedListener());
        
    }
    
    public class DotPrintPicture_OnItemSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                long id) {
            // TODO Auto-generated method stub
            for(DOT_MODE_SHARP i: DOT_MODE_SHARP.values())
            {
                switch(pos)
                {
                    case 1:
                        DotModeSharp = DOT_MODE_SHARP.horizontal;
                        showview.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
                        break;
                    
                    case 2:
                        DotModeSharp = DOT_MODE_SHARP.triangle;
                        showview.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
                        break;
                        
                    case 3:
                        DotModeSharp = DOT_MODE_SHARP.picture;
                        showview.setImageBitmap(PrnBitMap);
                        break;
                        
                    case 4:
                        DotModeSharp = DOT_MODE_SHARP.picture_text;
                        showview.setImageBitmap(PrnTextBitMap);
                        break;
                        
                    case 5:
                        DotModeSharp = DOT_MODE_SHARP.picture_bmp;
                        showview.setImageBitmap(ColorBitMap);                       
                        break;
                        
                    case 0:
                        DotModeSharp = DOT_MODE_SHARP.vertical;
                        break;
                 
                    default: break;    
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            Log.d(tag, "nothing select");
        }
     }
}


