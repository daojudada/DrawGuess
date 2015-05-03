package com.drawguess.util;


import java.util.HashMap;

import com.drawguess.msgbean.User;

/**
 * 本机用户的相关数据类
 * @author GuoJun
 *
 */
public class SessionUtils {

    private static User localUserInfo;
    private static HashMap<String, String> mlocalUserSession = new HashMap<String, String>(15);

    /** 清空全局登陆Session信息 **/
    public static void clearSession() {
        mlocalUserSession.clear();
    }

    /**
     * 获取头像编号
     * 
     * @return AvatarNum
     */
    public static int getAvatar() {
        return Integer.parseInt(mlocalUserSession.get(User.AVATAR));
    }

    /**
     * 获取设备品牌型号
     * 
     * @return device
     */
    public static String getDevice() {
        return mlocalUserSession.get(User.DEVICE);
    }


    /**
     * 获取性别
     * 
     * @return Gender
     */
    public static String getGender() {
        return mlocalUserSession.get(User.GENDER);
    }

    /**
     * 获取IMEI
     * 
     * @return IMEI
     */
    public static String getIMEI() {
        return mlocalUserSession.get(User.IMEI);
    }

    /**
     * 获取是否为客户端
     * 
     * @return isClient
     */
    public static boolean getIsClient() {
        return Boolean.parseBoolean(mlocalUserSession.get(User.ISCLIENT));
    }

    /**
     * 获取本地IP
     * 
     * @return localIPaddress
     */
    public static String getLocalIPaddress() {
        return mlocalUserSession.get(User.IPADDRESS);
    }

    public static User getLocalUserInfo() {
        if (localUserInfo == null) {
            localUserInfo = new User(getAvatar(), getNickname(),getGender(), getIMEI(), getDevice(), 
                    getLocalIPaddress(), getLoginTime());

        }
        return localUserInfo;
    }

    /**
     * 获取登录时间
     * 
     * @return Data 登录时间 年月日
     */
    public static String getLoginTime() {
        return mlocalUserSession.get(User.LOGINTIME);
    }

    /**
     * 获取昵称
     * 
     * @return Nickname
     */
    public static String getNickname() {
        return mlocalUserSession.get(User.NICKNAME);
    }
    
    /**
     * 获取游戏顺序
     * 
     * @return AvatarNum
     */
    public static int getOrder() {
        return Integer.parseInt(mlocalUserSession.get(User.ORDER));
    }

    /**
     * 获取热点IP
     * 
     * @return serverIPaddress
     */
    public static String getServerIPaddress() {
        return mlocalUserSession.get(User.SERVERIPADDRESS);
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

    /**
     * 设置头像编号
     * 
     * @param paramAvatar
     *            选择的头像编号
     */
    public static void setAvatar(int paramAvatar) {
        mlocalUserSession.put(User.AVATAR, String.valueOf(paramAvatar));
    }

    /**
     * 设置设备品牌型号
     * 
     * @param paramDevice
     */
    public static void setDevice(String paramDevice) {
        mlocalUserSession.put(User.DEVICE, paramDevice);
    }

    /**
     * 设置性别
     * 
     * @param paramGender
     * 
     */
    public static void setGender(String paramGender) {
        mlocalUserSession.put(User.GENDER, paramGender);
    }

    /**
     * 设置IMEI
     * 
     * @param paramIMEI
     *            本机的IMEI值
     */
    public static void setIMEI(String paramIMEI) {
        mlocalUserSession.put(User.IMEI, paramIMEI);
    }

    /**
     * 设置是否为客户端
     * 
     * @param paramIsClient
     */
    public static void setIsClient(boolean paramIsClient) {
        mlocalUserSession.put(User.ISCLIENT, String.valueOf(paramIsClient));
    }


    /**
     * 设置本地IP
     * 
     * @param paramLocalIPaddress
     *            本地IP地址值
     */
    public static void setLocalIPaddress(String paramLocalIPaddress) {
        mlocalUserSession.put(User.IPADDRESS, paramLocalIPaddress);
    }

    /**
     * 设置用户数据库id
     * 
     * @param paramID
     */
    public static void setLocalUserID(int paramID) {
        mlocalUserSession.put(User.ID, String.valueOf(paramID));
    }

    public static void setLocalUserInfo(User pUsers) {
        localUserInfo = pUsers;
        mlocalUserSession.put(User.AVATAR, String.valueOf(pUsers.getAvatar()));
        mlocalUserSession.put(User.NICKNAME, pUsers.getNickname());
        mlocalUserSession.put(User.GENDER, pUsers.getGender());
        mlocalUserSession.put(User.IMEI, pUsers.getIMEI());
        mlocalUserSession.put(User.DEVICE, pUsers.getDevice());
        mlocalUserSession.put(User.IPADDRESS, pUsers.getIpaddress());
        mlocalUserSession.put(User.LOGINTIME, pUsers.getLogintime());
        mlocalUserSession.put(User.ORDER, String.valueOf(pUsers.getOrder()));
        mlocalUserSession.put(User.SCORE, String.valueOf(pUsers.getScore()));
    }


    /**
     * 设置登录时间
     * 
     * @param paramLoginTime
     */
    public static void setLoginTime(String paramLoginTime) {
        mlocalUserSession.put(User.LOGINTIME, paramLoginTime);
    }

    /**
     * 设置昵称
     * 
     * @param paramNickname
     * 
     */
    public static void setNickname(String paramNickname) {
        mlocalUserSession.put(User.NICKNAME, paramNickname);
    }
    

    /**
     * 设置游戏序号
     * 
     * @param paramAvatar
     *            选择的头像编号
     */
    public static void setOrder(int paramOrder) {
        mlocalUserSession.put(User.ORDER, String.valueOf(paramOrder));
    }
    
    /**
     * 设置热点IP
     * 
     * @param paramServerIPaddress
     *            热点IP地址值
     */
    public static void setServerIPaddress(String paramServerIPaddress) {
        mlocalUserSession.put(User.SERVERIPADDRESS, paramServerIPaddress);
    }

    public static void updateUserInfo() {
        localUserInfo = new User(getAvatar(), getNickname(),getGender(), getIMEI(), getDevice(), 
                getLocalIPaddress(), getLoginTime());
    }

}
