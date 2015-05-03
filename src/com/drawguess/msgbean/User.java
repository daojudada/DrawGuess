package com.drawguess.msgbean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;
import com.drawguess.R;

/**
 * 附近个人实体类
 * @author GuoJun
 * 
 */
public class User extends Entity implements Parcelable {

    /** 用户常量 **/

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            User user = new User();
            user.setAvatar(source.readInt());
            user.setNickname(source.readString());
            user.setGender(source.readString());
            user.setIMEI(source.readString());
            user.setDevice(source.readString());
            user.setIpaddress(source.readString());
            user.setLogintime(source.readString());
            return user;
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    
    // 共有
    public static final String AVATAR = "avatar";
    public static final String DEVICE = "Device";
    public static final String ENTITY_PEOPLE = "entity_people";
    public static final String GENDER = "Gender";
    // 个人
    public static final String ID = "ID";
    public static final String IMEI = "IMEI";
    public static final String IPADDRESS = "Ipaddress";

    public static final String ISCLIENT = "isClient";
    public static final String LOGINTIME = "LoginTime";
    public static final String NICKNAME = "Nickname";
    public static final String ORDER = "Order";
    public static final String SCORE = "Score";
    public static final String SERVERIPADDRESS = "serverIPaddress";
    
    public static Parcelable.Creator<User> getCreator() {
        return CREATOR;
    }
    private int mAvatar;
    private String mDevice;
    private String mGender;
    private int mGenderBgId;
    private int mGenderId;

    private String mIMEI;
    private String mIpaddress;
    private String mLogintime;
    private String mNickname;
    
    private int order;
    private int score;

    public User() {
    	score = 0;
        order = -1;
    }



    public User(int avatar, String nickname, String gender, String IMEI,
            String device, String ip, String logintime) {
        this.mAvatar = avatar;
        this.mNickname = nickname;
        this.setGender(gender);
        this.mIMEI = IMEI;
        this.mDevice = device;
        this.mIpaddress = ip;
        this.mLogintime = logintime;
        this.order = -1;
        this.score = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @JSONField(name = User.AVATAR)
    public int getAvatar() {
        return this.mAvatar;
    }

    @JSONField(name = User.DEVICE)
    public String getDevice() {
        return this.mDevice;
    }


    @JSONField(name = User.GENDER)
    public String getGender() {
        return this.mGender;
    }

    @JSONField(serialize = false)
    public int getGenderBgId() {
        return this.mGenderBgId;
    }

    /** 个人变量 get set **/

    @JSONField(serialize = false)
    public int getGenderId() {
        return this.mGenderId;
    }


    @JSONField(name = User.IMEI)
    public String getIMEI() {
        return this.mIMEI;
    }

    @JSONField(name = User.IPADDRESS)
    public String getIpaddress() {
        return this.mIpaddress;
    }

    @JSONField(name = User.LOGINTIME)
    public String getLogintime() {
        return this.mLogintime;
    }


    @JSONField(name = User.NICKNAME)
    public String getNickname() {
        return this.mNickname;
    }

    @JSONField(serialize = false)
    public int getOrder() {
        return this.order;
    }


    @JSONField(serialize = false)
    public int getScore() {
        return this.score;
    }
    
    public void setAvatar(int paramAvatar) {
        this.mAvatar = paramAvatar;
    }

    public void setDevice(String paramDevice) {
        this.mDevice = paramDevice;
    }

    public void setGender(String paramGender) {
        this.mGender = paramGender;
        if ("女".equals(paramGender)) {
            setGenderId(R.drawable.ic_user_famale);
            setGenderBgId(R.drawable.bg_gender_famal);
        }
        else {
            setGenderId(R.drawable.ic_user_male);
            setGenderBgId(R.drawable.bg_gender_male);
        }
    }

    public void setGenderBgId(int paramGenderBgId) {
        this.mGenderBgId = paramGenderBgId;
    }

    public void setGenderId(int paramGenderId) {
        this.mGenderId = paramGenderId;
    }

    public void setIMEI(String paramIMEI) {
        this.mIMEI = paramIMEI;
    }

    public void setIpaddress(String paramIpaddress) {
        this.mIpaddress = paramIpaddress;
    }

    public void setLogintime(String paramLogintime) {
        this.mLogintime = paramLogintime;
    }
    
    public void setNickname(String paramNickname) {
        this.mNickname = paramNickname;
    }

    public void setOrder(int paramOrder) {
        this.order = paramOrder;
    }

    public void setScore(int paramScore){
    	this.score = paramScore;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mAvatar);
        dest.writeString(mNickname);
        dest.writeString(mGender);
        dest.writeString(mIMEI);
        dest.writeString(mDevice);
        dest.writeString(mIpaddress);
        dest.writeString(mLogintime);
    }

}
