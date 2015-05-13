package com.drawguess.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drawguess.msgbean.User;


/**
 * 数据类型转换工具类
 * 提供了不同数据类型之间的转化
 * @author GuoJun
 * 
 */

public class TypeUtils {
	public static String cListToString(List<String> list){
		String lists = "";
	   	for(String s : list) {
	   		if(s != list.get(list.size()-1))
	   			lists += s + ",";
				else
					lists += s;
	   	}
	   	return lists;
    }
    
    public static List<String> cStringToList(String lists){
    	List<String> list = new ArrayList<String>();     
    	if(!lists.equals("")){
	    	String[] strArray = null; 
	    	strArray = lists.split(","); 
	    	for(int i = 0; i<strArray.length; i++){
	    		list.add(strArray[i]);
	    	}
    	}
    	return list;
    }
	
    /**
     * 将用户表HashMap转成ArrayList
     * 
     * @param application
     */
    public static ArrayList<User> cMapToList(HashMap<String,User> mUsersMap) {
    	ArrayList<User> mUsersList = new ArrayList<User>(mUsersMap.size());
        for (Map.Entry<String, User> entry : mUsersMap.entrySet()) {
            mUsersList.add(entry.getValue());
        }
        return mUsersList;
    }
}