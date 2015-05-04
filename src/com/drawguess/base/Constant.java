package com.drawguess.base;

public class Constant {
    public static float DENSITY;
    public static boolean IS_FIRST;
	public static int WIN_X;
	public static int WIN_Y;
    public static final int BUFFER_SIZE = 1024;// 流缓冲大小
    public static final int TCP_PORT = 4447; // 主机接收端口 
	public static final int UDP_PORT = 3647; // 主机接收端口
	
	// Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}

