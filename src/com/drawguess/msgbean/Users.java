package com.drawguess.msgbean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;
import com.drawguess.R;

/**
 * @fileName NearByPeople.java
 * @description 附近个人实体类
 * @author _Hill3
 */
public class Users extends Entity implements Parcelable {

    /** 用户常量 **/

    // 共有
    public static final String AVATAR = "avatar";
    public static final String NICKNAME = "Nickname";
    public static final String GENDER = "Gender";
    public static final String IMEI = "IMEI";
    public static final String DEVICE = "Device";
    public static final String BIRTHDAY = "birthday";
    public static final String IPADDRESS = "Ipaddress";
    public static final String LOGINTIME = "LoginTime";
    public static final String ORDER = "order";

    // 个人
    public static final String ID = "ID";
    public static final String ISCLIENT = "isClient";
    public static final String SERVERIPADDRESS = "serverIPaddress";
    public static final String ENTITY_PEOPLE = "entity_people";

    private int mAvatar;
    private String mNickname;
    private String mGender;
    private String mIMEI;
    private String mDevice;
    private String mIpaddress;
    private String mLogintime;

    private int mGenderId;
    private int mGenderBgId;
    private int msgCount;
    private int order;
    
    public Users() {
        this.msgCount = 0;
        order = -1;
    }

    public Users(int avatar, String nickname, String gender, String IMEI,
            String device, String ip, String logintime) {
        this.mAvatar = avatar;
        this.mNickname = nickname;
        this.setGender(gender);
        this.mIMEI = IMEI;
        this.mDevice = device;
        this.mIpaddress = ip;
        this.mLogintime = logintime;
        this.order = -1;
    }


    @JSONField(name = Users.AVATAR)
    public int getAvatar() {
        return this.mAvatar;
    }



    @JSONField(name = Users.NICKNAME)
    public String getNickname() {
        return this.mNickname;
    }

    @JSONField(name = Users.GENDER)
    public String getGender() {
        return this.mGender;
    }

    @JSONField(name = Users.IMEI)
    public String getIMEI() {
        return this.mIMEI;
    }

    @JSONField(name = Users.DEVICE)
    public String getDevice() {
        return this.mDevice;
    }


    @JSONField(name = Users.IPADDRESS)
    public String getIpaddress() {
        return this.mIpaddress;
    }

    @JSONField(name = Users.LOGINTIME)
    public String getLogintime() {
        return this.mLogintime;
    }

    public void setAvatar(int paramAvatar) {
        this.mAvatar = paramAvatar;
    }


    public void setNickname(String paramNickname) {
        this.mNickname = paramNickname;
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

    public void setIMEI(String paramIMEI) {
        this.mIMEI = paramIMEI;
    }

    public void setDevice(String paramDevice) {
        this.mDevice = paramDevice;
    }


    public void setIpaddress(String paramIpaddress) {
        this.mIpaddress = paramIpaddress;
    }

    public void setLogintime(String paramLogintime) {
        this.mLogintime = paramLogintime;
    }

    /** 个人变量 get set **/

    @JSONField(serialize = false)
    public int getGenderId() {
        return this.mGenderId;
    }

    public void setGenderId(int paramGenderId) {
        this.mGenderId = paramGenderId;
    }

    @JSONField(serialize = false)
    public int getGenderBgId() {
        return this.mGenderBgId;
    }

    public void setGenderBgId(int paramGenderBgId) {
        this.mGenderBgId = paramGenderBgId;
    }

    @JSONField(serialize = false)
    public int getMsgCount() {
        return this.msgCount;
    }

    public void setMsgCount(int paramMsgCount) {
        this.msgCount = paramMsgCount;
    }

    @JSONField(serialize = false)
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int paramOrder) {
        this.order = paramOrder;
    }
    
    public static Parcelable.Creator<Users> getCreator() {
        return CREATOR;
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
        dest.writeInt(msgCount);
    }

    public static final Parcelable.Creator<Users> CREATOR = new Parcelable.Creator<Users>() {

        @Override
        public Users createFromParcel(Parcel source) {
            Users user = new Users();
            user.setAvatar(source.readInt());
            user.setNickname(source.readString());
            user.setGender(source.readString());
            user.setIMEI(source.readString());
            user.setDevice(source.readString());
            user.setIpaddress(source.readString());
            user.setLogintime(source.readString());
            user.setMsgCount(source.readInt());
            return user;
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
