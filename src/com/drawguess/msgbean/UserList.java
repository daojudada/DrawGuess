package com.drawguess.msgbean;

import java.util.ArrayList;

/**
 * 消息实体类
 * 
 * @author GuoJun
 */
public class UserList extends Entity {

    private ArrayList<User> userList;

    public UserList() {
    }

    public UserList(ArrayList<User> userList) {
    	this.userList = userList;
    }



    /**
     * 获取用户列表
     * 
     * @return userList
     */

    public ArrayList<User> getUserList() {
        return userList;
    }

    /**
     * 发送用户列表
     * 
     * @param userList
     * 
     */
    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }



    /**
     * 克隆对象
     * 
     * @param
     */
    public UserList clone() {
        return new UserList(userList);
    }

}
