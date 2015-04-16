package com.drawguess.net;
/**
 * MSG协议标准，从IPMSG协议的C++版本改写而来
 * @see IPMSGProtocol
 * 
 */
public class MSGConst {	
	/**
	 * 通报在线
	 */
	public static final int ANSENTRY			 = 0x00000003;	
	
	/**
	 * 收到猜词结果
	 */
	public static final int ANSGUESS			 = 0x00000079;	
	/**
	 * 应答用户列表发送请求
	 */
	public static final int ANSLIST			 = 0x00000013;	
	/**
	 * 收到opcopy操作
	 */
	public static final int ANSOPCOPY			 = 0x00000087;	
	/**
	 * 收到opdelete操作
	 */
	public static final int ANSOPDELETE		 = 0x00000089;	
	/**
	 * 收到opdraw操作
	 */
	public static final int ANSOPDRAW			 = 0x00000083;	
	/**
	 * 收到opfill操作
	 */
	public static final int ANSOPFILL			 = 0x00000085;	
	/**
	 * 收到optrans操作
	 */
	public static final int ANSOPTRANS		 = 0x00000091;	
	/**
	 * 应答RSA公钥
	 */
	public static final int ANSPUBKEY			 = 0x00000070;	
	/**
	 * 消息打开确认通知
	 */
	public static final int ANSREADMSG		 = 0x00000032;	
	/**
	 * 收到游戏准备请求
	 */
	public static final int ANSREADY			 = 0x00000073;	
	/**
	 * 收到redo操作
	 */
	public static final int ANSREDO			 = 0x00000093;	
	/**
	 * 回复注册成功请求
	 */
	public static final int ANSREGISTER		 = 0x00000094;	
	/**
	 * 收到是否猜对
	 */
	public static final int ANSRESULT			 = 0x00000081;	
	/**
	 * 收到游戏开始请求
	 */
	public static final int ANSSTART			 = 0x00000077;	
	/**
	 * 收到取消游戏准备请求
	 */
	public static final int ANSUNREADY		 = 0x00000075;	
	/**
	 * 用户上线
	 */
	public static final int BR_ENTRY			 = 0x00000001;	
	/**
	 * 用户退出
	 */
	public static final int BR_EXIT		 	 = 0x00000002;	
	/**
	 * 寻找有效的可以发送用户列表的成员
	 */
	public static final int BR_ISGETLIST		 = 0x00000010;	
	/**
	 * 图片接收确认
	 */
	public static final int CONFIRM_IMAGE_DATA = 0x00000064;  
    /**
	 * 录音接收确认
	 */
	public static final int CONFIRM_VOICE_DATA = 0x00000067;  
    /**
	 * 消息丢弃通知
	 */
	public static final int DELMSG 			 = 0x00000031;  
    /**
	 * 发送给个人
	 */
	public static final int DIALUPOPT 		 = 0x00000512;  
	/**
	 * 加密
	 */
	public static final int ENCRYPTOPT		 = 0x00002048;  
	/**
	 * 附加文件
	 */
	public static final int FILEATTACHOPT 	 = 0x00001024;  
	/**
     * 文件接收成功
     */
    public static final int GET_FILE_SUCCESS   = 0x00000062;  
	/**
	 * 获得缺席信息
	 */
	public static final int GETABSENCEINFO	 = 0x00000050;  
	/**
	 * 获得IPMSG版本信息
	 */
	public static final int GETINFO			 = 0x00000040;  
	/**
	 * 用户列表发送请求
	 */
	public static final int GETLIST			 = 0x00000012;	
	/**
	 * 获得RSA公钥
	 */
	public static final int GETPUBKEY			 = 0x00000069;	
	
	
	
	/* option for IPMSG protocol command */
    /**
     * 不进行任何操作
     */
	public static final int NOOPERATION		 = 0x00000000;	
	/**
	 * 通知用户列表已经获得
	 */
	public static final int OKGETLIST			 = 0x00000011;	
	/**
     *  发送端口号，默认端口2425
     */
	public static final int PORT = 0x0979;	
	/**
	 * 消息打开通知
	 */
	public static final int READMSG 			 = 0x00000030;	
	/**
	 * 收到通报在线
	 */
	public static final int REANSENTRY		 = 0x00000004;	
	/**
	 * 通报收到消息
	 */
	public static final int RECVMSG 			 = 0x00000021;	
	/**
     * 图片发送请求
     */
	public static final int REQUEST_IMAGE_DATA = 0x00000063;	
	/**
	 * 录音发送请求
	 */
	public static final int REQUEST_VOICE_DATA = 0x00000066;	
	/**
     * 文件发送成功  
     */
    public static final int SEND_FILE_SUCCESS  = 0x00000061;
	/**
	 * 图片发送成功
	 */
	public static final int SEND_IMAGE_SUCCESS = 0x00000065;	
	/**
	 * 录音发送成功
	 */
	public static final int SEND_VOICE_SUCCESS = 0x00000068;	
	/**
	 * 发送缺席信息
	 */
	public static final int SENDABSENCEINFO	 = 0x00000051;	
	/**
	 * 发送猜词结果
	 */
	public static final int SENDGUESS			 = 0x00000078;	
	/**
	 * 发送IPMSG版本信息
	 */
	public static final int SENDINFO			 = 0x00000041;	
	/**
	 * 发送消息
	 */
	public static final int SENDMSG    		 = 0x00000020;	
	/**
	 * 发送opcopy操作
	 */
	public static final int SENDOPCOPY		 = 0x00000086;	
	/**
	 * 发送opdelete操作
	 */
	public static final int SENDOPDELETE		 = 0x00000088;	
	/**
	 * 发送opdraw操作
	 */
	public static final int SENDOPDRAW		 = 0x00000082;	
	/**
	 * 发送opfill操作
	 */
	public static final int SENDOPFILL		 = 0x00000084;	
	/**
	 * 发送optrans操作
	 */
	public static final int SENDOPTRANS		 = 0x00000090;	
	/* option for own program command */
	/**
	 * 发送游戏准备请求
	 */
	public static final int SENDREADY			 = 0x00000072;	
	/**
	 * 发送注册请求
	 */
	public static final int SENDREGISTER		 = 0x00000094;
	/**
	 * 发送是否猜对
	 */
	public static final int SENDRESULT		 = 0x00000080;	
	/**
	 * 发送游戏开始请求
	 */
	public static final int SENDSTART			 = 0x00000076;	
	
	/**
	 * 发送undo操作
	 */
	public static final int SENDUNDO		 	 = 0x00000092;	
	/**
	 * 发送取消游戏准备请求
	 */
	public static final int SENDUNREADY		 = 0x00000074;	
	/* option for all command */
	/**
	 * 服务器（保留）
	 */
	public static final int SERVEROPT 		 = 0x00000256;	
	/**
	 * 更新文件传输进度
	 */
    public static final int UPDATE_FILEPROCESS = 0x00000060;	
	

}
