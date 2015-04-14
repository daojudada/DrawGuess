package com.drawguess.util;

import com.drawguess.base.BaseApplication;
import com.drawguess.msgbean.Users;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * sd卡数据管理类
 * @author GuoJun
 *
 */
public class SdDataUtils {
    private static final String GlobalSharedName = "LocalUserInfo";
    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;

    public SdDataUtils() {
    	BaseApplication instance = BaseApplication.getInstance();
        mSP = instance.getSharedPreferences(GlobalSharedName,Context.MODE_PRIVATE);
        mEditor = mSP.edit();
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public String getIMEI() {
        return mSP.getString(Users.IMEI, "");
    }

    public String getNickname() {
        return mSP.getString(Users.NICKNAME, "");
    }

    public int getAvatarId() {
        return mSP.getInt(Users.AVATAR, 0);
    }

    public String getBirthday() {
        return mSP.getString(Users.BIRTHDAY, "000000");
    }


    public String getGender() {
        return mSP.getString(Users.GENDER, "获取失败");
    }


    public String getLogintime() {
        return mSP.getString(Users.LOGINTIME, "获取失败");
    }

    
}
