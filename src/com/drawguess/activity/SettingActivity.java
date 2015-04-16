package com.drawguess.activity;


import com.drawguess.R;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.BaseApplication;
import com.drawguess.base.BaseDialog;
import com.drawguess.view.SettingSwitchButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 设置界面
 * @author GuoJun
 *
 */
public class SettingActivity extends BaseActivity implements OnClickListener,
        OnCheckedChangeListener, DialogInterface.OnClickListener {

    private Button mAboutUsButton;
    private Button mBackButton;
    private Button mExitApplicationButton;

    private BaseDialog mExitDialog;
    private ImageView mSettingInfoButton;
    private RelativeLayout mSettingInfoLayoutButton;
    private SettingSwitchButton mSoundSwitchButton;

    private SettingSwitchButton mVibrateSwitchButton;



    protected void init() {

        mExitDialog = BaseDialog.getDialog(this, R.string.dialog_tips,
                getString(R.string.setting_dialog_logout_confirm),
                getString(R.string.setting_dialog_logout_cancel), this,
                getString(R.string.setting_dialog_logout_ok), this);

        mSoundSwitchButton.setChecked(BaseApplication.getSoundFlag());
        mVibrateSwitchButton.setChecked(BaseApplication.getVibrateFlag());
    }
    
    @Override
    protected void initEvents() {
        mSettingInfoButton.setOnClickListener(this);
        mSettingInfoLayoutButton.setOnClickListener(this);
        mSoundSwitchButton.setOnCheckedChangeListener(this);
        mVibrateSwitchButton.setOnCheckedChangeListener(this);
        mBackButton.setOnClickListener(this);
        mAboutUsButton.setOnClickListener(this);
        mExitApplicationButton.setOnClickListener(this);

    }

    @Override
    protected void initViews() {
        mSettingInfoButton = (ImageView) findViewById(R.id.btn_setting_my_information);
        mSettingInfoLayoutButton = (RelativeLayout) findViewById(R.id.setting_my_info_layout);
        mSoundSwitchButton = (SettingSwitchButton) findViewById(R.id.checkbox_sound);
        mVibrateSwitchButton = (SettingSwitchButton) findViewById(R.id.checkbox_vibration);
        mBackButton = (Button) findViewById(R.id.btn_back);
        mAboutUsButton = (Button) findViewById(R.id.btn_about_us);
        mExitApplicationButton = (Button) findViewById(R.id.btn_exit_application);
    }

    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_sound:
                buttonView.setChecked(isChecked);
                BaseApplication.setSoundFlag(!isChecked);
                break;

            case R.id.checkbox_vibration:
                buttonView.setChecked(isChecked);
                BaseApplication.setVibrateFlag(isChecked);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == 0) {
            mExitDialog.dismiss();
        }
        else if (which == 1) {
            setAsyncTask();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.setting_my_info_layout:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.btn_back:
                finish();
                break;

            case R.id.btn_about_us:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.btn_exit_application:
                mExitDialog.show();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settting);
        initViews();
        initEvents();
        init();
    }

    private void setAsyncTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
				return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                ActivitiesManager.finishAllActivities();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mExitDialog.dismiss();
            }
        });
    }

}
