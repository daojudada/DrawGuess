package com.drawguess.sql;

import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author GuoJun
 *
 */
public class DBOperate {
    private DBHelper userSQLHelper; // 数据库类(t_word)
    private SQLiteDatabase userDataBase;// 数据库(t_word)的操作类

    /**
     * 构造函数参数：context对象通过db的方法来操作数据库的增删改查
     */
    public DBOperate(Context context) {
        userSQLHelper = new DBHelper(context);
        userDataBase = userSQLHelper.getWritableDatabase();
    }

    /* 关闭数据库 */
    public void close() {
        userSQLHelper.close();
        userDataBase.close();
    }
    
    /**
     * 参数：userInfo类 作用：用来添加用户信息
     */
    public void add(byte[] w,byte[]k) {
        ContentValues values = new ContentValues();
        values.put("word", w);
        values.put("kind", k);
        userDataBase.replace(userSQLHelper.getTableName(), "kind", values);
    }


    public void createTable(){
    	userDataBase.execSQL("create table " + userSQLHelper.getTableName() +
				" (word BLOB primary key,kind BLOB)");
    }
    
    public void dropTable(){
		userDataBase.execSQL("DROP TABLE " + userSQLHelper.getTableName()); 
    }
    /**
     * 随机从数据库中取一个词
     * @param randomSeed
     * @return randomWord
     */
    public WordInfo getRandomWord(long randomSeed) {
        // db = helper.getWritableDatabase();
        // db.query(table, columns, selection, selectionArgs, groupBy, having,
        // orderBy)
    	String sql;
    	int random =  new Random(randomSeed).nextInt(getCount()-1);
    	sql = "select word,kind " +
    			"from " + userSQLHelper.getTableName() + 
    			" limit 1 offset "+ random;


    	Cursor cursor = userDataBase.rawQuery(sql, null);

        if (cursor.moveToNext()) {
        	byte[] w = cursor.getBlob(cursor.getColumnIndex("word"));
        	byte[] k = cursor.getBlob(cursor.getColumnIndex("kind"));
            WordInfo userInfo = new WordInfo(w,k);
            cursor.close();
            return userInfo;
        }
        cursor.close();
        return null;
    }

    

    /**
     * 作用: 用来获取表中用户总数量
     */
    public int getCount() {
        Cursor cursor = userDataBase.query(userSQLHelper.getTableName(),
                new String[] { "count(*)" }, null, null, null, null, null);
        if (cursor.moveToNext()) {
            long count = cursor.getLong(0);
            cursor.close();
            return (int) count;
        }
        cursor.close();
        return 0;
    }

    
}
