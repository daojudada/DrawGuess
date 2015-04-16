package com.drawguess.net;


import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


import com.alibaba.fastjson.annotation.JSONField;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.Message;
import com.drawguess.msgbean.Users;
import com.drawguess.util.JsonUtils;
import com.drawguess.util.LogUtils;

/**
 * IPMSG协议抽象类
 * <p>
 * 数据包编号：一般是取毫秒数。用来唯一地区别每个数据包；
 * <p>
 * SenderIMEI：指的是发送者的设备IMEI
 * <p>
 * 命令：协议中定义的一系列命令，具体见下文；
 * <p>
 * 附加数据：额外发送的数据
 * 
 * @see MSGConst
 * 
 */
public class MSGProtocol {
    public enum ADDITION_TYPE {
        MSG, STRING, USER
    }
    private static final String ADDOBJECT = "addObject";
    private static final String ADDSTR = "addStr";
    private static final String ADDTYPE = "addType";
    private static final String COMMANDNO = "commandNo";
    private static final String PACKETNO = "packetNo";

    
    private static final String TAG = "IPMSGPProtocol";
    private Entity addObject; // 附加对象
    private String addStr; // 附加信息
    private ADDITION_TYPE addType; // 附加数据类型
    private int commandNo; // 命令
    private String packetNo;// 数据包编号

    private String senderIMEI; // 发送者IMEI

    public MSGProtocol() {
        this.packetNo = getSeconds();
    }

    // 根据协议字符串初始化
    public MSGProtocol(String paramProtocolJSON) {
        try {
            JSONObject protocolJSON = new JSONObject(paramProtocolJSON);
            packetNo = protocolJSON.getString(PACKETNO);
            commandNo = protocolJSON.getInt(COMMANDNO);
            senderIMEI = protocolJSON.getString(Users.IMEI);
            if (protocolJSON.has(ADDTYPE)) { // 若有附加信息
                String addJSONStr = null;
                if (protocolJSON.has(ADDOBJECT)) { // 若为Entity类型
                    addJSONStr = protocolJSON.getString(ADDOBJECT);
                }
                else if (protocolJSON.has(ADDSTR)) { // 若为String类型
                    addJSONStr = protocolJSON.getString(ADDSTR);
                }
                switch (ADDITION_TYPE.valueOf(protocolJSON.getString(ADDTYPE))) {
                    case USER: // 为用户数据
                        addObject = JsonUtils.getObject(addJSONStr, Users.class);
                        break;

                    case MSG: // 为消息数据
                        addObject = JsonUtils.getObject(addJSONStr, Message.class);
                        break;

                    case STRING: // 为String数据
                        addStr = addJSONStr;
                        break;

                    default:
                        break;
                }

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "非标准JSON文本");
        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, Entity paramObject) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addObject = paramObject;
        if (paramObject instanceof Message) { // 若为Message对象
            this.addType = ADDITION_TYPE.MSG;
        }
        if (paramObject instanceof Users) { // 若为NearByPeople对象
            this.addType = ADDITION_TYPE.USER;
        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, String paramStr) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addStr = paramStr;
        this.addType = ADDITION_TYPE.STRING;
    }

    @JSONField(name = ADDOBJECT)
    public Entity getAddObject() {
        return this.addObject;
    }

    @JSONField(name = ADDSTR)
    public String getAddStr() {
        return this.addStr;
    }

    @JSONField(name = ADDTYPE)
    public ADDITION_TYPE getAddType() {
        return this.addType;
    }

    @JSONField(name = COMMANDNO)
    public int getCommandNo() {
        return this.commandNo;
    }

    @JSONField(name = PACKETNO)
    public String getPacketNo() {
        return this.packetNo;
    }

    // 输出协议JSON串
    @JSONField(serialize = false)
    public String getProtocolJSON() {
        return JsonUtils.createJsonString(this);
    }

    // 得到数据包编号，毫秒数
    @JSONField(serialize = false)
    private String getSeconds() {
        Date nowDate = new Date();
        return Long.toString(nowDate.getTime());
    }

    @JSONField(name = Users.IMEI)
    public String getSenderIMEI() {
        return this.senderIMEI;
    }

    public void setAddObject(Entity paramObject) {
        this.addObject = paramObject;
    }

    public void setAddStr(String paramStr) {
        this.addStr = paramStr;
    }

    public void setAddType(ADDITION_TYPE paramType) {
        this.addType = paramType;
    }

    public void setCommandNo(int paramCommandNo) {
        this.commandNo = paramCommandNo;
    }
    

    public void setPacketNo(String paramPacketNo) {
        this.packetNo = paramPacketNo;
    }

    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

}
