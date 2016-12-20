package android.AclasDemos;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;

/*
 * �Զ���TabHost��ʹtab�л���ʱ���ж���Ч��
 */
public class CustomTabHost extends TabHost {
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
    final int rotateWidth = 600;
    final int rotateHeight = 686;
	private int tabCount;//tabҳ����

	public CustomTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	    slideLeftIn = AnimationUtils.loadAnimation(context,R.anim.slide_left_in);
	    slideLeftOut = AnimationUtils.loadAnimation(context,R.anim.slide_left_out);
	    slideRightIn = AnimationUtils.loadAnimation(context,R.anim.slide_right_in);
	    slideRightOut = AnimationUtils.loadAnimation(context,R.anim.slide_right_out);
		
//		slideLeftOut = new Rotate3d(-0, -90, 0, 0, rotateWidth/2, rotateHeight/2);   
//		slideLeftOut.setFillAfter(true);
//		slideLeftOut.setDuration(500);   
//
//		slideLeftIn = new Rotate3d(90, 0, 0, 0, rotateWidth/2, rotateHeight/2);   
//		slideLeftIn.setFillAfter(true);
//		slideLeftIn.setDuration(500);   
//		
//		slideRightIn = new Rotate3d(-90, 0, 0.0f, 0.0f, rotateWidth/2, rotateHeight/2);   
//		slideRightIn.setFillAfter(true);   
//		slideRightIn.setDuration(500);  
//
//		slideRightOut = new Rotate3d(-90+90, -0+90, 0.0f, 0.0f, rotateWidth/2, rotateHeight/2);   
//		slideRightOut.setFillAfter(true);   
//		slideRightOut.setDuration(500);  
	}
	
	public int getTabCount() {
		return tabCount;
	}

	@Override
	public void addTab(TabSpec tabSpec) {
		tabCount++;
		super.addTab(tabSpec);
	}
	
//	@Override
//	public void setCurrentTab(int index) {  
//        //index为要切换到的tab页索引，currentTabIndex为现在要当前tab页的索引  
//        int currentTabIndex = getCurrentTab();  
//          
//        String tag = "AclasArmPosDBG";
//        Log.d(tag, "1 - currentTabIndex = " + currentTabIndex + ",index = " + index + ",tabCount = " + tabCount);
//        //设置当前tab页退出时的动画  
//
//        if (null != getCurrentView())
//        {//第一次进入MainActivity时，getCurrentView()取得的值为空  
// 
////            slideLeftOut = new Rotate3d(-0, -90, 0, 0, rotateWidth/2, rotateHeight/2);   
////            slideLeftOut.setFillAfter(true);
////            slideLeftOut.setDuration(500);   
////
////            slideLeftIn = new Rotate3d(90, 0, 0, 0, rotateWidth/2, rotateHeight/2);   
////            slideLeftIn.setFillAfter(true);
////            slideLeftIn.setDuration(500);   
////            
////            slideRightIn = new Rotate3d(-90, 0, 0.0f, 0.0f, rotateWidth/2, rotateHeight/2);   
////            slideRightIn.setFillAfter(true);   
////            slideRightIn.setDuration(500);  
////
////            slideRightOut = new Rotate3d(-90+90, -0+90, 0.0f, 0.0f, rotateWidth/2, rotateHeight/2);   
////            slideRightOut.setFillAfter(true);   
////            slideRightOut.setDuration(500);  
//            
//            if (currentTabIndex == (tabCount - 1) && index == 0) 
//            {//处理边界滑动  
//                getCurrentView().startAnimation(slideRightOut);  
//                Log.d(tag, "slideLeftOut");
//            } else if (currentTabIndex == 0 && index == (tabCount - 1)) 
//            {//处理边界滑动  
//                getCurrentView().startAnimation(slideLeftOut);  
//                Log.d(tag, "slideRightOut");
//            } else if (index > currentTabIndex) 
//            {//非边界情况下从右往左fleep  
//                getCurrentView().startAnimation(slideRightOut);  
//                Log.d(tag, "slideLeftOut");
//            } else if (index < currentTabIndex) 
//            {//非边界情况下从左往右fleep  
//                getCurrentView().startAnimation(slideLeftOut);  
//                Log.d(tag, "slideRightOut");
//            }  
//        }  
//         Log.d(tag,"super setCurrentTab");
//        super.setCurrentTab(index);  
//        
//        //设置即将显示的tab页的动画  
//        if (currentTabIndex == (tabCount - 1) && index == 0)
//        {//处理边界滑动  
//            getCurrentView().startAnimation(slideRightIn);  
//            Log.d(tag, "slideLeftIn");
//        } else if (currentTabIndex == 0 && index == (tabCount - 1)) 
//        {//处理边界滑动  
//            getCurrentView().startAnimation(slideLeftIn);  
//            Log.d(tag, "slideRightIn");
//        } else if (index > currentTabIndex) 
//        {//非边界情况下从右往左fleep  
//            getCurrentView().startAnimation(slideRightIn);  
//            Log.d(tag, "slideLeftIn");
//        } else if (index < currentTabIndex) 
//        {//非边界情况下从左往右fleep  
//            getCurrentView().startAnimation(slideLeftIn);  
//            Log.d(tag, "slideRightIn");
//        }  
//    }  
}
