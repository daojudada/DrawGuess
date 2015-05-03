package com.drawguess.activity;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.drawguess.R;
import com.drawguess.base.Constant;
import com.drawguess.sql.DBOperate;
import com.drawguess.sql.WordInfo;
import com.drawguess.util.EncryptUtils;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SdDataUtils;

import android.app.Activity;
import android.content.Context;
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
	public final String TAG = "WelcomeActivity";
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
			readTxtToDb("tb.txt", WelcomeActivity.this);
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


	/**
	 * 写入数据库
	 * @param fname
	 */
	public void readTxtToDb(String fname, Context context){
        String[] arr;
        try {
        	DBOperate db = new DBOperate(context);
        	db.createTable();
            String encoding="utf-8";
    		InputStream in=context.getResources().getAssets().open(fname);
            InputStreamReader read = new InputStreamReader(in,encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                arr = lineTxt.split("@");
                byte[] wordBt = EncryptUtils.encrypt(arr[0].getBytes(),Constant.PASSWORD);
                byte[] kindBt = EncryptUtils.encrypt(arr[1].getBytes(),Constant.PASSWORD);
                db.add(wordBt,kindBt);
            }
            
            bufferedReader.close();
            db.close();
        } catch (Exception e) {
            LogUtils.e(TAG,"读取文件内容出错");
        }
    }
}