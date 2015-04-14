package com.drawguess.interfaces;

import com.drawguess.net.MSGProtocol;
import com.esotericsoftware.kryonet.Connection;

/**
 * 消息处理接口
 * @author GuoJun
 *
 */
public interface MSGListener{
	void handleIPMSG(Connection conection, MSGProtocol ipmsg);
}