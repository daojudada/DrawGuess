package com.drawguess.activity;



import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.drawguess.R;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;

/**
 * 选择联网模式，WIFI OR 蓝牙
 * @author GuoJun
 *
 */
public class ConnectModeActivity extends BaseActivity implements OnClickListener{


   
    private Button mBtnBack;
    private Button mBtnWiFi;
    private Button mBtnBlueTooth;

    
    private long ExitTime; // 延时退出时间变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectmode);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnWiFi = (Button) findViewById(R.id.btn_wifi);
        mBtnBlueTooth = (Button) findViewById(R.id.btn_bluetooth);
        
       
    }

    @Override
    protected void initEvents() {
        mBtnBack.setOnClickListener(this);
        mBtnWiFi.setOnClickListener(this);
        mBtnBlueTooth.setOnClickListener(this); 
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi:
                startActivity(WifiapActivity.class);
                
                break;
            case R.id.btn_bluetooth:
            	
                break;
    

            case R.id.btn_back:
                finish();
                break;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                Toast.makeText(this, getString(R.string.gameroom_toast_logout), Toast.LENGTH_SHORT)
                        .show();
                ExitTime = System.currentTimeMillis();
            }
            else {
                ActivitiesManager.finishAllActivities();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
   
}
