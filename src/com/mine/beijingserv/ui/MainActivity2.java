package com.mine.beijingserv.ui;






import com.crittercism.app.Crittercism;
import com.mine.beijingserv.R;
import com.mine.beijingserv.R.layout;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.push.PushService;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.BitmapZoom;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SDcardUtil;
import com.mine.beijingserv.sys.SendDeviceInfoThread;
import com.mine.beijingserv.sys.SysUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity2 extends Activity 
{
	
	
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /////初始化crash lib
        Crittercism.init(MainActivity2.this, "519636188b2e3341db000018");
        
        final ImageView  indexbackground = (ImageView)findViewById(R.id.indexbackground);
        final ImageView  indeximage = (ImageView)findViewById(R.id.inedximage);
        AlphaAnimation  alphaAnimation = new AlphaAnimation(1,1);
        alphaAnimation.setDuration(2000);        
        alphaAnimation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				
				SharedPreferences sharedPreferences = getSharedPreferences(
						"pref", Context.MODE_PRIVATE);
				boolean isfirst = sharedPreferences.getBoolean(
						"key_pref_isfirstuse", true);

				if (isfirst) {
					Editor editor = sharedPreferences.edit();
					editor.putBoolean("key_pref_isfirstuse", false);
					editor.commit();
					///第一次启动导航栏
					startActivity(new Intent(MainActivity2.this,
							AppGuideActivity.class));
					finish();
				}else{
					//第二次启动TabsMain
					startActivity(new Intent(MainActivity2.this, TabsMain.class));
					finish();
				}
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        //开始2秒开场动画
        indexbackground.startAnimation(alphaAnimation);
        indeximage.startAnimation(alphaAnimation);      	
		
		}
       

    
    protected void onResume() {
    	super.onResume();
    	
    }
    
   

    
}
