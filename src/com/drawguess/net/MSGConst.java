package com.drawguess.net;
/**
 * 自定义MSG协议指令标准，
 * 
 * SEND代表客户端发送给服务器
 * 
 * ANS代表服务器发送给客户端
 * 
 * @author GuoJun
 * 
 */
public class MSGConst {	
	

	/**
	 * DEBUG中测试用
	 */
	public static final int DEBUG_MSG			= 0x00000000;
	
	/**
	 * UDP广播在线
	 */
	public static final int BROAD_FIND			= 0x00000001;
	
	/**
	 * UDP回应广播在线
	 */
	public static final int BROAD_REC			= 0x00000002;
	
	/**
	 * 客户端通报在线
	 */
	public static final int SEND_ONLINE			= 0x00000003;
	

	/**
	 * 服务器端应答在线
	 */
	public static final int ANS_ONLINE			= 0x00000004;
	

	/**
	 * 客户端通报下线
	 */
	public static final int SEND_OFFLINE		= 0x00000005;
	

	/**
	 * 服务器端应答下线
	 */
	public static final int ANS_OFFLINE			= 0x00000006;
	

	/**
	 * 客户端通报准备
	 */
	public static final int SEND_READY			= 0x00000007;
	

	/**
	 * 服务器端应答准备
	 */
	public static final int ANS_READY			= 0x00000008;
	

	/**
	 * 客户端通报准备
	 */
	public static final int SEND_UNREADY		= 0x00000009;
	

	/**
	 * 服务器端应答准备
	 */
	public static final int ANS_UNREADY			= 0x00000010;
	
	
	/**
	 * 服务器端通报开始
	 */
	public static final int SEND_START			= 0x00000011;
	

	/**
	 * 客户器端应答开始
	 */
	public static final int ANS_START			= 0x00000012;
	

	/**
	 * 服务器端通报确认开始
	 */
	public static final int ANS_RESTART			= 0x00000013;
	

	/**
	 * 绘图同步数据
	 */
	public static final int ANS_DRAW			= 0x00000014;
}
