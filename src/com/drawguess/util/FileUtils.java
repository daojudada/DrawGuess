package com.drawguess.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

import com.drawguess.base.Constant;
import com.drawguess.sql.DBOperate;

/**
 * 文件读取类
 * @author GuoJun
 *
 */
public class FileUtils {
	private static final String TAG = "FileUtils";
	/**
	 * 写入数据库
	 * @param fname
	 */
	public static void readTxtToDb(String fname, Context context){
        String[] arr;
        try {
        	DBOperate db = new DBOperate(context);
        	db.createTable();
            String encoding="utf-8";
    		InputStream in=context.getResources().getAssets().open(fname);
            InputStreamReader read = new InputStreamReader(in,encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                arr = lineTxt.split("@");
                byte[] wordBt = EncryptUtils.encrypt(arr[0].getBytes(),Constant.PASSWORD);
                byte[] kindBt = EncryptUtils.encrypt(arr[1].getBytes(),Constant.PASSWORD);
                db.add(wordBt,kindBt);
            }
            
            bufferedReader.close();
            db.close();
        } catch (Exception e) {
            LogUtils.e(TAG,"读取文件内容出错");
        }
        
    }
	
	/**
	 * 写入数据库
	 * @param fname
	 */
	public static void readBinToDb(String fname, Context context){
        String[] arr;
        try {
        	DBOperate db = new DBOperate(context);
        	db.createTable();
    		InputStream input=context.getResources().getAssets().open(fname);
    		// 挨个读取
            DataInputStream in=new DataInputStream(input);
            int e = in.readInt();
            String encoding = "gbk";
            switch(e){
            case 0:
            	encoding = "gbk";
            	break;
            case 1:
            	encoding = "utf-8";
            	break;
            case 2:
            	encoding = "utf-16";
            	break;
            }
            int count=in.readInt();
            
            for(int i=0;i<count;i++){
                int length = in.readInt();
                byte[] readB = new byte[length];
                in.read(readB);
                //直接将如上内容解密
        	    byte[] decryResult;
        	    String decryStr;
        		decryResult = EncryptUtils.decrypt(readB, Constant.PASSWORD);
        		decryStr = new String(decryResult,encoding);
    			arr = decryStr.split("@");
                byte[] wordBt = EncryptUtils.encrypt(arr[0].getBytes(),Constant.PASSWORD);
                byte[] kindBt = EncryptUtils.encrypt(arr[1].getBytes(),Constant.PASSWORD);
                db.add(wordBt,kindBt);
            }
            in.close();
            db.close();
        } catch (Exception e) {
            LogUtils.e(TAG,"读取文件内容出错");
        }
    }
}
