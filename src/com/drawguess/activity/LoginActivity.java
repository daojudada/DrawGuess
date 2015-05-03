package com.drawguess.activity;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.drawguess.R;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.Constant;
import com.drawguess.msgbean.User;
import com.drawguess.sql.DBOperate;
import com.drawguess.sql.WordInfo;
import com.drawguess.util.DataUtils;
import com.drawguess.util.EncryptUtils;
import com.drawguess.util.ImageUtils;
import com.drawguess.util.SdDataUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TextUtils;
import com.squareup.picasso.*;

/**
 * 登陆界面以及修改资料
 * @author GuoJun
 *
 */
public class LoginActivity extends BaseActivity implements OnClickListener{


    private int mAvatar;
    private Button mBtnChangeUser;

    private Button mBtnExit;
    private Button mBtnNext;
    private String mDevice;
    private EditText mEtNickname;
    private String mGender;
    private String mIMEI;
    private ImageView mImgExAvatar;
    
    private ImageView mIvAvatar;
    private ImageView mIvExGender;
    private String mLastLogintime; // 上次登录时间
    private LinearLayout mLayoutExGender; // 性别根布局
    private LinearLayout mLlayoutExMain; // 二次登陆页面

    private LinearLayout mLlayoutMain; // 首次登陆主界面
    private String mLogintime;
    private String mNickname = "";
    private RadioGroup mRgGender;
    private TelephonyManager mTelephonyManager;
    private TextView mTvExLogintime; // 上次登录时间
    private TextView mTvExNickmame;

    /**
     * 执行下一步跳转
     * <p>
     * 同时获取客户端的IMIE信息
     */
    private void doLoginNext() {
        if (mNickname.length() == 0) {
            if ((!isValidated())) {
                return;
            }
        }
        
        try {
            mIMEI = mTelephonyManager.getDeviceId(); // 获取IMEI
            mDevice = getPhoneModel();
            mLogintime = DataUtils.getNowtime();
            
            // 设置用户Session信息
            SessionUtils.setIMEI(mIMEI);
            SessionUtils.setDevice(mDevice);
            SessionUtils.setNickname(mNickname);
            SessionUtils.setGender(mGender);
            SessionUtils.setAvatar(mAvatar);
            SessionUtils.setLoginTime(mLogintime);
            
            // 在SD卡中存储登陆信息
            SdDataUtils mSPutUtils = new SdDataUtils();
            SharedPreferences.Editor mEditor = mSPutUtils.getEditor();
            
            mEditor.putString(User.IMEI, mIMEI)
            	.putString(User.DEVICE, mDevice)
            	.putString(User.NICKNAME, mNickname)
            	.putString(User.GENDER, mGender)
                .putString(User.LOGINTIME, mLogintime)
        		.putInt(User.AVATAR, mAvatar);
            mEditor.commit();
            
            startActivity(ConnectModeActivity.class);
            
        }
        catch (Exception e) {
            showShortToast(R.string.login_toast_loginfailue);
        }
        

    }

    public String getPhoneModel() {
        String str1 = Build.BRAND;
        String str2 = Build.MODEL;
        str2 = str1 + "_" + str2;
        return str2;
    }

    @Override
    protected void initEvents() {
    	mIvAvatar.setOnClickListener(this);
        mBtnExit.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnChangeUser.setOnClickListener(this);
    }


    @Override
    protected void initViews() {
    	
    	mIvAvatar = (ImageView) findViewById(R.id.setting_my_avatar_img);
        mEtNickname = (EditText) findViewById(R.id.login_et_nickname);
        mRgGender = (RadioGroup) findViewById(R.id.login_baseinfo_rg_gender);

        mBtnExit = (Button) findViewById(R.id.login_btn_exit);
        mBtnNext = (Button) findViewById(R.id.login_btn_next);
        mBtnChangeUser = (Button) findViewById(R.id.login_btn_changeUser);

        SdDataUtils sp = new SdDataUtils();
        mNickname = sp.getNickname();
        
        
        // 若mNickname有内容，则读取本地存储的用户信息
        if (mNickname.length() != 0) {
            mTvExNickmame = (TextView) findViewById(R.id.login_tv_existName);
            mImgExAvatar = (ImageView) findViewById(R.id.login_img_existImg);
            mLayoutExGender = (LinearLayout) findViewById(R.id.login_layout_gender);
            mIvExGender = (ImageView) findViewById(R.id.login_iv_gender);
            mTvExLogintime = (TextView) findViewById(R.id.login_tv_lastlogintime);
            mLlayoutExMain = (LinearLayout) findViewById(R.id.login_linearlayout_existmain);
            mLlayoutMain = (LinearLayout) findViewById(R.id.login_linearlayout_main);
            mLlayoutMain.setVisibility(View.GONE);
            mLlayoutExMain.setVisibility(View.VISIBLE);

            mAvatar = sp.getAvatarId();
            mGender = sp.getGender();
            mLastLogintime = sp.getLogintime();
            
            Picasso.with(mContext).load(ImageUtils.getImageID(User.AVATAR + mAvatar)).into(mImgExAvatar);
            Picasso.with(mContext).load(ImageUtils.getImageID(User.AVATAR + mAvatar)).into(mIvAvatar);
            
            mTvExNickmame.setText(mNickname);
            mTvExLogintime.setText(DataUtils.getBetweentime(mLastLogintime));
            
            if ("女".equals(mGender)) {
                mIvExGender.setBackgroundResource(R.drawable.ic_user_famale);
                mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_famal);
            }
            else {
                mIvExGender.setBackgroundResource(R.drawable.ic_user_male);
                mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_male);
            }
        }
    }

    /**
     * 登录资料完整性验证，不完整则无法登陆，完整则记录输入的信息。
     * 
     * @return boolean 返回是否为完整， 完整(true),不完整(false)
     */
    private boolean isValidated() {
        mNickname = "";
        mGender = null;
        if (TextUtils.isNull(mEtNickname) ) {
            showShortToast(R.string.login_toast_nickname);
            mEtNickname.requestFocus();
            return false;
        }

        if (mEtNickname.getText().toString().trim().length()>5) {
            showShortToast(R.string.login_toast_nicknamelong);
            mEtNickname.requestFocus();
            return false;
        }
        
        switch (mRgGender.getCheckedRadioButtonId()) {
            case R.id.login_baseinfo_rb_female:
                mGender = "女";
                break;
            case R.id.login_baseinfo_rb_male:
                mGender = "男";
                break;
            default:
                showShortToast(R.string.login_toast_sex);
                return false;
        }

        mNickname = mEtNickname.getText().toString().trim(); // 获取昵称
        return true;
    }

 
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data_intent){
		super.onActivityResult(requestCode, resultCode, data_intent);
		if(resultCode == RESULT_CANCELED)
			setTitle("cancel");
		else if (resultCode == RESULT_OK){
			int result = data_intent.getExtras().getInt("result");
            mAvatar = result + 1;
            Picasso.with(mContext).load(ImageUtils.getImageID(User.AVATAR + mAvatar)).into(mIvAvatar);
       }
	}
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 更换用户,清空数据
            case R.id.login_btn_changeUser:
                mNickname = "";
                mGender = null;
                mIMEI = null;
                mAvatar = 0;
                SessionUtils.clearSession(); // 清空Session数据
                mLlayoutMain.setVisibility(View.VISIBLE);
                mLlayoutExMain.setVisibility(View.GONE);
                break;
            case R.id.setting_my_avatar_img:
                Intent intent = new Intent(this, ChooseAvatarActivity.class);
                startActivityForResult(intent, 0);
                break;
                
            case R.id.login_btn_exit:
                ActivitiesManager.finishAllActivities();
                break;

            case R.id.login_btn_next:
                doLoginNext();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initViews();
        initEvents();
    }
}
