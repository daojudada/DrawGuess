package com.drawguess.interfaces;

import com.drawguess.net.MSGProtocol;

/**
 * 消息处理接口
 * @author GuoJun
 *
 */
public interface OnMsgRecListener{
	void processMessage(MSGProtocol ipmsg);
}