package com.drawguess.activity;


import com.drawguess.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

/**
 * 欢迎加载界面
 * @author GuoJun
 *
 */
public class WelcomeActivity extends Activity {
	
	Button drawButton,serverButton,clientButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		

		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				//do something
				//...
				
			    Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
			    startActivity(intent);
			    WelcomeActivity.this.finish();
			}
		}, 2000);
	}

}