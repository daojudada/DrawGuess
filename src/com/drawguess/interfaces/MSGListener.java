package com.drawguess.interfaces;

import com.drawguess.net.MSGProtocol;

/**
 * 消息处理接口
 * @author GuoJun
 *
 */
public interface MSGListener{
	void processMessage(MSGProtocol ipmsg);
}