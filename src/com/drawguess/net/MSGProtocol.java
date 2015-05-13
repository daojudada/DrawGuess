package com.drawguess.net;


import org.json.JSONException;
import org.json.JSONObject;



import com.alibaba.fastjson.annotation.JSONField;
import com.drawguess.msgbean.DataDraw;
import com.drawguess.msgbean.DataGuess;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.User;
import com.drawguess.msgbean.UserList;
import com.drawguess.util.JsonUtils;

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
    	STRING, USER, DATADRAW, DATAGUESS, USERLIST
    }
    private static final String ADDOBJECT = "addObject";
    private static final String ADDSTR = "addStr";
    private static final String ADDTYPE = "addType";
    private static final String COMMANDNO = "commandNo";
    private static final String ID = "id";

    
    private Entity addObject; // 附加对象
    private String addStr; // 附加信息
    private ADDITION_TYPE addType; // 附加数据类型
    private int commandNo; // 命令
    private String senderIMEI; // 发送者IMEI
    private int id;
    
    public MSGProtocol() {
    }

    // 根据协议字符串初始化
    public MSGProtocol(String paramProtocolJSON) throws JSONException {
        JSONObject protocolJSON = new JSONObject(paramProtocolJSON);
        id = protocolJSON.getInt(ID);
        commandNo = protocolJSON.getInt(COMMANDNO);
        senderIMEI = protocolJSON.getString(User.IMEI);
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
                    addObject = JsonUtils.getObject(addJSONStr, User.class);
                    break;

                case DATADRAW: // 为消息数据
                    addObject = JsonUtils.getObject(addJSONStr, DataDraw.class);
                    break;
                    
                case DATAGUESS://猜词数据
                	addObject = JsonUtils.getObject(addJSONStr, DataGuess.class);
                
                case STRING: // 为String数据
                    addStr = addJSONStr;
                    break;
                    
                case USERLIST:
                	addObject = JsonUtils.getObject(addJSONStr, UserList.class);
                	break;
                	
                default:
                    break;
            }

        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo) {
        super();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.id = 0;
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, Entity paramObject) {
        super();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addObject = paramObject;
        this.id = 0;
        if (paramObject instanceof DataDraw) { // 若为DATADRAW对象
            this.addType = ADDITION_TYPE.DATADRAW;
        }
        if (paramObject instanceof User) { // 若为People对象
            this.addType = ADDITION_TYPE.USER;
        }
        if (paramObject instanceof DataGuess) { // 若为猜词对象
            this.addType = ADDITION_TYPE.DATAGUESS;
        }
        if (paramObject instanceof UserList) { // 若为猜词对象
            this.addType = ADDITION_TYPE.USERLIST;
        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, String paramStr) {
        super();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addStr = paramStr;
        this.addType = ADDITION_TYPE.STRING;
        this.id = 0;
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

    @JSONField(name = ID)
    public int getId() {
        return this.id;
    }

    // 输出协议JSON串
    @JSONField(serialize = false)
    public String getProtocolJSON() {
        return JsonUtils.createJsonString(this);
    }


    @JSONField(name = User.IMEI)
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
    
    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

    public void setId(int paramId) {
        this.id = paramId;
    }

}
