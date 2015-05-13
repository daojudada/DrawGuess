package com.drawguess.base;

public class Constant {
	/**
	 * 通信方式
	 */
	public static boolean CONNECT_WAY;
	/**
	 * 是否第一次使用
	 */
    public static boolean IS_FIRST;
	/**
	 * 屏幕密度
	 */
    public static float DENSITY;
	/**
	 * 屏幕宽度
	 */
	public static int WIN_X;
	/**
	 * 屏幕长度
	 */
	public static int WIN_Y;
	/**
	 * 流缓冲区大小
	 */
    public static final int BUFFER_SIZE = 512; 
	/**
	 * 主机TCP端口 
	 */
    public static final int TCP_PORT = 4447;
	/**
	 * 主机UDP端口
	 */
	public static final int UDP_PORT = 3647;
	/**
	 * 游戏时间
	 */
	public static final int GAME_TIME = 300;
	/**
	 * DES加密密钥 
	 */
	public static final	String PASSWORD = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";

<<<<<<< HEAD
=======
    // public static final int BUFFER_SIZE = 1024;// 流缓冲大小
    // public static final int TCP_PORT = 4447; // 主机接收端口 
	// public static final int UDP_PORT = 3647; // 主机接收端口
>>>>>>> origin/master
	
	// Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
<<<<<<< HEAD
=======

>>>>>>> origin/master
}

