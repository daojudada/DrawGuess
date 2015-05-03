package com.drawguess.util;

import com.drawguess.base.BaseApplication;
import com.drawguess.msgbean.User;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * sd卡数据管理类
 * @author GuoJun
 *
 */
public class SdDataUtils {
    private static final String GlobalSharedName = "LocalUserInfo";
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mSP;

    public SdDataUtils() {
    	BaseApplication instance = BaseApplication.getInstance();
        mSP = instance.getSharedPreferences(GlobalSharedName,Context.MODE_PRIVATE);
        mEditor = mSP.edit();
    }

    public int getAvatarId() {
        return mSP.getInt(User.AVATAR, 0);
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public String getGender() {
        return mSP.getString(User.GENDER, "获取失败");
    }

    public String getIMEI() {
        return mSP.getString(User.IMEI, "");
    }


    public boolean getIsFirst(){
    	return mSP.getBoolean("FIRST",true);
    }


    public String getLogintime() {
        return mSP.getString(User.LOGINTIME, "获取失败");
    }

    public String getNickname() {
        return mSP.getString(User.NICKNAME, "");
    }
}
