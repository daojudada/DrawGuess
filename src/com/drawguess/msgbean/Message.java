package com.drawguess.msgbean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 消息实体类，猜词用
 * @author GuoJun
 * 
 */
public class Message extends Entity {

    /** 消息内容类型 **/
    public enum CONTENT_TYPE {
        DATA, TEXT;
    }
    private CONTENT_TYPE contentType;
    private String MsgContent;
    private int percent;
    private String senderIMEI;

    private String sendTime;

    public Message() {
    }

    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
    }

    /**
     * 克隆对象
     * 
     * @param
     */

    public Message clone() {
        return new Message(senderIMEI, sendTime, MsgContent, contentType);
    }

    /**
     * 获取消息内容类型
     * 
     * @return
     * @see CONTENT_TYPE
     */
    public CONTENT_TYPE getContentType() {
        return contentType;
    }

    /**
     * 获取消息内容
     * 
     * @return
     */
    public String getMsgContent() {
        return MsgContent;
    }

    @JSONField(serialize = false)
    public int getPercent() {
        return percent;
    }

    /**
     * 获取消息发送方IMEI
     * 
     * @return
     */

    public String getSenderIMEI() {
        return senderIMEI;
    }

    /**
     * 获取消息发送时间
     * 
     * @return
     */
    public String getSendTime() {
        return sendTime;
    }

    /**
     * 设置消息内容类型
     * 
     * @param paramContentType
     * @see CONTENT_TYPE
     */
    public void setContentType(CONTENT_TYPE paramContentType) {
        this.contentType = paramContentType;
    }

    /**
     * 设置消息内容
     * 
     * @param paramMsgContent
     */
    public void setMsgContent(String paramMsgContent) {
        this.MsgContent = paramMsgContent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    /**
     * 设置消息发送方IMEI
     * 
     * @param paramSenderIMEI
     * 
     */
    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

    /**
     * 设置消息发送时间
     * 
     * @param paramSendTime
     *            发送时间,格式 xx年xx月xx日 xx:xx:xx
     */
    public void setSendTime(String paramSendTime) {
        this.sendTime = paramSendTime;
    }

}
