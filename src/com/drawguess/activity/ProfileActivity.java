package com.drawguess.activity;

import com.drawguess.R;
import com.drawguess.base.BaseActivity;
import com.drawguess.msgbean.Users;
import com.drawguess.util.ImageUtils;
import com.drawguess.util.SdDataUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TextUtils;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * 资料更改
 * @author GuoJun
 *
 */
public class ProfileActivity extends BaseActivity implements OnClickListener {
    private int mAvatar;
    private Button mBtnBack;
    private Button mBtnSave;
    private EditText mEtNickname;
    private String mGender;
    private ImageView mIvAvatar;
    private String mNickname = "";
    
    private RadioButton mRbBoy;
    private RadioButton mRbGirl;
    private RadioGroup mRgGender;
    
    /**
     * 执行下一步跳转
     * <p>
     * 同时获取客户端的IMIE信息
     */
    private void doLoginNext() {
    	
        if (isValidated()) {
            if (mNickname.length() == 0) {
                return;
            }
        }
        
        try {
            
            // 设置用户Session信息
            SessionUtils.setNickname(mNickname);
            SessionUtils.setGender(mGender);
            SessionUtils.setAvatar(mAvatar);
            
            // 在SD卡中存储登陆信息
            SdDataUtils mSPutUtils = new SdDataUtils();
            SharedPreferences.Editor mEditor = mSPutUtils.getEditor();
            
            mEditor.putString(Users.NICKNAME, mNickname)
            	.putString(Users.GENDER, mGender)
        		.putInt(Users.AVATAR, mAvatar);
            mEditor.commit();
            
            finish();
            
        }
        catch (Exception e) {
            showShortToast(R.string.login_toast_loginfailue);
        }

    }

    @Override
    protected void initEvents() {

        setTitle(getString(R.string.setting_text_profile));
        
        mIvAvatar.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
    }

    private void initProfile() {

        SdDataUtils sp = new SdDataUtils();
        mNickname = sp.getNickname();
    	mAvatar = sp.getAvatarId();
    	mGender = sp.getGender();
    	
    	mEtNickname.setText(mNickname);
    	Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + mAvatar)).into(mIvAvatar);
    	if ("女".equals(mGender)) {
    		mRgGender.check(mRbGirl.getId());
        }
        else {
        	mRgGender.check(mRbBoy.getId());
        }
        
    }


    @Override
    protected void initViews() {

    	mIvAvatar = (ImageView) findViewById(R.id.profile_my_avatar_img);
        mEtNickname = (EditText) findViewById(R.id.profile_et_nickname);
        mRgGender = (RadioGroup) findViewById(R.id.profile_baseinfo_rg_gender);
        
        mRbBoy = (RadioButton) findViewById(R.id.profile_baseinfo_rb_male);
        mRbGirl = (RadioButton) findViewById(R.id.profile_baseinfo_rb_female);
        mBtnSave = (Button) findViewById(R.id.profile_btn_save);
        mBtnBack = (Button) findViewById(R.id.profile_btn_back);
        

    }
    
    /**
     * 登录资料完整性验证，不完整则无法登陆，完整则记录输入的信息。
     * 
     * @return boolean 返回是否为完整， 完整(true),不完整(false)
     */
    private boolean isValidated() {
        mNickname = "";
        mGender = null;
        if (TextUtils.isNull(mEtNickname)) {
            showShortToast(R.string.login_toast_nickname);
            mEtNickname.requestFocus();
            return false;
        }

        switch (mRgGender.getCheckedRadioButtonId()) {
            case R.id.profile_baseinfo_rb_female:
                mGender = "女";
                break;
            case R.id.profile_baseinfo_rb_male:
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
               Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + mAvatar)).into(mIvAvatar);
          }
   	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_my_avatar_img:
                Intent intent = new Intent(this, ChooseAvatarActivity.class);
                startActivityForResult(intent, 0);
                break;
                
            case R.id.profile_btn_back:
                finish();
                break;

            case R.id.profile_btn_save:
                doLoginNext();
                break;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        initEvents();
        initProfile();
    }

}
