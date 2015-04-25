package com.drawguess.activity;


import com.drawguess.R;
import com.drawguess.base.Constant;
import com.drawguess.util.SdDataUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;

/**
 * 欢迎加载界面
 * @author GuoJun
 *
 */
public class WelcomeActivity extends Activity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		long beginTime = System.currentTimeMillis();
		
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
       	Constant.WIN_X = metric.widthPixels;  // 宽度（PX）
        Constant.WIN_Y = metric.heightPixels;  // 高度（PX）
        Constant.DENSITY = metric.density;  // 密度（0.75 / 1.0 / 1.5）
        
		SdDataUtils sp = new SdDataUtils();
		Boolean user_first = sp.getIsFirst();
		if(user_first){//第一次启动
			sp.getEditor().putBoolean("FIRST", false).commit();
			Constant.IS_FIRST = true;
			//do something to init 
			//...
		}else{
			Constant.IS_FIRST = false;
		}

		long waitTime = 2000 - (System.currentTimeMillis() - beginTime);
		
		//欢迎界面等待
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
			    Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
			    startActivity(intent);
			    WelcomeActivity.this.finish();
			}
		}, waitTime > 0 ? waitTime : 1);
	}

}