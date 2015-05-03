package com.drawguess.sql;


/**
 * 猜词数据类
 * @author GuoJun， Kuang Zongqiang
 */
public class WordInfo {
    private byte[] word; // 聊天信息的记录时间
    private byte[] kind; // 聊天信息的内容

    // 以下是该类的构造函数
    public WordInfo() {

    }

    public WordInfo(byte[] date, byte[] info) {
        this.word = date;
        this.kind = info;
    }


    
    /** 设置聊天时间 */
    public void setWord(byte[] date) {
        this.word = date;
    }

    /** 获取聊天时间 */
    public byte[] getWord() {
        return word;
    }

    /** 设置聊天信息 */
    public void setKind(byte[] info) {
        this.kind = info;
    }

    /** 获取聊天信息 */
    public byte[] getKind() {
        return kind;
    }
    
    
}
