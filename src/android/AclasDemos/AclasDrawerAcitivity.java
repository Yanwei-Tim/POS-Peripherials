package android.AclasDemos;


import aclasdriver.drawer;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AclasDrawerAcitivity extends Activity{
    
    private drawer mdrawer;
    private int fd;
    private Button OpenButton;
    public static final String tag = "AclasArmPosDBG";
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);
        mdrawer = new drawer();
        OpenButton = (Button) findViewById(R.id.DrawerButton);
        OpenButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stubs
                mdrawer.open();
            }
        });
        
    }
    
 
}
