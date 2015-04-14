package com.drawguess.util;


import java.util.HashMap;

import com.drawguess.msgbean.Users;

/**
 * 本机用户的相关数据类
 * @author GuoJun
 *
 */
public class SessionUtils {

    private static Users localUserInfo;
    private static HashMap<String, String> mlocalUserSession = new HashMap<String, String>(15);

    public static Users getLocalUserInfo() {
        if (localUserInfo == null) {
            localUserInfo = new Users(getAvatar(), getNickname(),getGender(), getIMEI(), getDevice(), 
                    getLocalIPaddress(), getLoginTime());

        }
        return localUserInfo;
    }

    public static void setLocalUserInfo(Users pUsers) {
        localUserInfo = pUsers;
        mlocalUserSession.put(Users.AVATAR, String.valueOf(pUsers.getAvatar()));
        mlocalUserSession.put(Users.NICKNAME, pUsers.getNickname());
        mlocalUserSession.put(Users.GENDER, pUsers.getGender());
        mlocalUserSession.put(Users.IMEI, pUsers.getIMEI());
        mlocalUserSession.put(Users.DEVICE, pUsers.getDevice());
        mlocalUserSession.put(Users.IPADDRESS, pUsers.getIpaddress());
        mlocalUserSession.put(Users.LOGINTIME, pUsers.getLogintime());
        mlocalUserSession.put(Users.ORDER, String.valueOf(pUsers.getOrder()));
    }

    public static void updateUserInfo() {
        localUserInfo = new Users(getAvatar(), getNickname(),getGender(), getIMEI(), getDevice(), 
                getLocalIPaddress(), getLoginTime());
    }

    public static String getBirthday() {
        return mlocalUserSession.get(Users.BIRTHDAY);
    }


    /**
     * 获取本地IP
     * 
     * @return localIPaddress
     */
    public static String getLocalIPaddress() {
        return mlocalUserSession.get(Users.IPADDRESS);
    }

    /**
     * 获取热点IP
     * 
     * @return serverIPaddress
     */
    public static String getServerIPaddress() {
        return mlocalUserSession.get(Users.SERVERIPADDRESS);
    }

    /**
     * 获取昵称
     * 
     * @return Nickname
     */
    public static String getNickname() {
        return mlocalUserSession.get(Users.NICKNAME);
    }

    /**
     * 获取性别
     * 
     * @return Gender
     */
    public static String getGender() {
        return mlocalUserSession.get(Users.GENDER);
    }

    /**
     * 获取IMEI
     * 
     * @return IMEI
     */
    public static String getIMEI() {
        return mlocalUserSession.get(Users.IMEI);
    }

    /**
     * 获取设备品牌型号
     * 
     * @return device
     */
    public static String getDevice() {
        return mlocalUserSession.get(Users.DEVICE);
    }

    /**
     * 获取头像编号
     * 
     * @return AvatarNum
     */
    public static int getAvatar() {
        return Integer.parseInt(mlocalUserSession.get(Users.AVATAR));
    }
    
    /**
     * 获取头像编号
     * 
     * @return AvatarNum
     */
    public static int getOrder() {
        return Integer.parseInt(mlocalUserSession.get(Users.ORDER));
    }



    /**
     * 获取是否为客户端
     * 
     * @return isClient
     */
    public static boolean getIsClient() {
        return Boolean.parseBoolean(mlocalUserSession.get(Users.ISCLIENT));
    }

    /**
     * 获取登录时间
     * 
     * @return Data 登录时间 年月日
     */
    public static String getLoginTime() {
        return mlocalUserSession.get(Users.LOGINTIME);
    }

    public static void setBirthday(String birthday) {
        mlocalUserSession.put(Users.BIRTHDAY, birthday);
    }

    /**
     * 设置用户数据库id
     * 
     * @param paramID
     */
    public static void setLocalUserID(int paramID) {
        mlocalUserSession.put(Users.ID, String.valueOf(paramID));
    }

    /**
     * 设置登录时间
     * 
     * @param paramLoginTime
     */
    public static void setLoginTime(String paramLoginTime) {
        mlocalUserSession.put(Users.LOGINTIME, paramLoginTime);
    }

    /**
     * 设置本地IP
     * 
     * @param paramLocalIPaddress
     *            本地IP地址值
     */
    public static void setLocalIPaddress(String paramLocalIPaddress) {
        mlocalUserSession.put(Users.IPADDRESS, paramLocalIPaddress);
    }

    /**
     * 设置热点IP
     * 
     * @param paramServerIPaddress
     *            热点IP地址值
     */
    public static void setServerIPaddress(String paramServerIPaddress) {
        mlocalUserSession.put(Users.SERVERIPADDRESS, paramServerIPaddress);
    }

    /**
     * 设置昵称
     * 
     * @param paramNickname
     * 
     */
    public static void setNickname(String paramNickname) {
        mlocalUserSession.put(Users.NICKNAME, paramNickname);
    }


    /**
     * 设置性别
     * 
     * @param paramGender
     * 
     */
    public static void setGender(String paramGender) {
        mlocalUserSession.put(Users.GENDER, paramGender);
    }

    /**
     * 设置IMEI
     * 
     * @param paramIMEI
     *            本机的IMEI值
     */
    public static void setIMEI(String paramIMEI) {
        mlocalUserSession.put(Users.IMEI, paramIMEI);
    }

    /**
     * 设置设备品牌型号
     * 
     * @param paramDevice
     */
    public static void setDevice(String paramDevice) {
        mlocalUserSession.put(Users.DEVICE, paramDevice);
    }


    /**
     * 设置头像编号
     * 
     * @param paramAvatar
     *            选择的头像编号
     */
    public static void setAvatar(int paramAvatar) {
        mlocalUserSession.put(Users.AVATAR, String.valueOf(paramAvatar));
    }

    /**
     * 设置游戏序号
     * 
     * @param paramAvatar
     *            选择的头像编号
     */
    public static void setOrder(int paramOrder) {
        mlocalUserSession.put(Users.ORDER, String.valueOf(paramOrder));
    }
    

    /**
     * 设置是否为客户端
     * 
     * @param paramIsClient
     */
    public static void setIsClient(boolean paramIsClient) {
        mlocalUserSession.put(Users.ISCLIENT, String.valueOf(paramIsClient));
    }

    public static boolean isLocalUser(String paramIMEI) {
        if (paramIMEI == null) {
            return false;
        }
        else if (getIMEI().equals(paramIMEI)) {
            return true;
        }
        return false;
    }

    /** 清空全局登陆Session信息 **/
    public static void clearSession() {
        mlocalUserSession.clear();
    }

}
