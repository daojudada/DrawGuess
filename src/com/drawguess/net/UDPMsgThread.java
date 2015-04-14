package com.drawguess.net;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.drawguess.base.BaseApplication;
import com.drawguess.interfaces.MSGListener;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.Users;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;

import android.content.Context;
import android.os.Message;

/**
 * UDP连接的线程
 * @author GuoJun
 *
 */
public class UDPMsgThread implements Runnable {

    private static final String TAG = "UDPMessageListener";
    private static final String BROADCASTIP = "255.255.255.255";
    private static final int POOL_SIZE = 5; // 单个CPU线程池大小
    private static final int BUFFERLENGTH = 4096; // 缓冲大小

    private static byte[] sendBuffer = new byte[BUFFERLENGTH];
    private static byte[] receiveBuffer = new byte[BUFFERLENGTH];
    
    private Thread receiveUDPThread;
    private boolean isThreadRunning;
    private List<OnNewMsgListener> mListenerList;
    private MSGListener msgListener;
    private String serverIP;
    private static ExecutorService executor;
    private static DatagramSocket UDPSocket;
    private static DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;

    private static UDPMsgThread instance;

    public UDPMsgThread() {

        mListenerList = new ArrayList<OnNewMsgListener>();

        int cpuNums = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cpuNums * POOL_SIZE); // 根据CPU数目初始化线程池
    }

    /**
     * <p>
     * 获取UDPSocketThread实例
     * <p>
     * 单例模式，返回唯一实例
     * 
     * @param paramApplication
     * @return instance
     */
    public static UDPMsgThread getInstance(Context context) {
        if (instance == null) {
            instance = new UDPMsgThread();
        }
        return instance;
    }

    public void setMSGListener(MSGListener msgListener){
    	this.msgListener = msgListener;
    }
    
    @Override
    public void run() {
        while (isThreadRunning) {

            try {
                UDPSocket.receive(receiveDatagramPacket);
            }
            catch (IOException e) {
                isThreadRunning = false;
                receiveDatagramPacket = null;
                if (UDPSocket != null) {
                    UDPSocket.close();
                    UDPSocket = null;
                }
                receiveUDPThread = null;
                LogUtils.e(TAG, "UDP数据包接收失败！线程停止");
                e.printStackTrace();
                break;
            }

            if (receiveDatagramPacket.getLength() == 0) {
                LogUtils.e(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }

            String UDPListenResStr = "";
            try {
                UDPListenResStr = new String(receiveBuffer, 0, receiveDatagramPacket.getLength(),"gbk");
            }
            catch (UnsupportedEncodingException e) {
                LogUtils.e(TAG, "系统不支持GBK编码");
            }

            MSGProtocol msgRes = new MSGProtocol(UDPListenResStr);
            int command = msgRes.getCommandNo();
            String senderIp = receiveDatagramPacket.getAddress().getHostAddress();

            if (BaseApplication.isDebugmode) {
            	switch(command){
            	case MSGConst.BR_ENTRY:
            		sendUDPdata(MSGConst.REANSENTRY,senderIp,senderIp);
            		break;
            	case MSGConst.REANSENTRY:
            		serverIP = msgRes.getAddStr();
            	}
            }
            else {
                if (!SessionUtils.isLocalUser(msgRes.getSenderIMEI())) 
                {
                	switch(command){
                	case MSGConst.BR_ENTRY:
                		sendUDPdata(MSGConst.REANSENTRY,senderIp,senderIp);
                		break;
                	case MSGConst.REANSENTRY:
                		serverIP = msgRes.getAddStr();
                	}
                }
            }

            // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
            if (receiveDatagramPacket != null) {
                receiveDatagramPacket.setLength(BUFFERLENGTH);
            }

        }

        receiveDatagramPacket = null;
        if (UDPSocket != null) {
            UDPSocket.close();
            UDPSocket = null;
        }
        receiveUDPThread = null;

    }

    


	/** 建立Socket连接 **/
    public void connectUDPSocket() {
        try {
            // 绑定端口
            if (UDPSocket == null)
                UDPSocket = new DatagramSocket(MSGConst.PORT);

            
            LogUtils.i(TAG, "connectUDPSocket() 绑定端口成功");

            // 创建数据接受包
            if (receiveDatagramPacket == null)
                receiveDatagramPacket = new DatagramPacket(receiveBuffer, BUFFERLENGTH);

            startUDPSocketThread();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /** 开始监听线程 **/
    public void startUDPSocketThread() {
        if (receiveUDPThread == null) {
            receiveUDPThread = new Thread(this);
            receiveUDPThread.start();
        }
        isThreadRunning = true;
        LogUtils.i(TAG, "startUDPSocketThread() 线程启动成功");
    }

    /** 暂停监听线程 **/
    public void stopUDPSocketThread() {
        isThreadRunning = false;
        if (receiveUDPThread != null)
            receiveUDPThread.interrupt();
        receiveUDPThread = null;
        instance = null; // 置空, 消除静态变量引用
        LogUtils.i(TAG, "stopUDPSocketThread() 线程停止成功");
    }

    public void addMsgListener(OnNewMsgListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeMsgListener(OnNewMsgListener listener) {
        this.mListenerList.remove(listener);
    }


    /**
     * 发送UDP数据包
     * 
     * @param commandNo
     *            消息命令
     * @param targetIP
     *            目标地址
     * @param addData
     *            附加数据
     * @see MSGConst
     */
    public static void sendUDPdata(int commandNo, String targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP, Object addData) {
        sendUDPdata(commandNo, targetIP.getHostAddress(), addData);
    }

    public static void sendUDPdata(int commandNo, String targetIP, Object addData) {
        MSGProtocol ipmsgProtocol = null;
        String imei = SessionUtils.getIMEI();

        if (addData == null) {
            ipmsgProtocol = new MSGProtocol(imei, commandNo);
        }
        else if (addData instanceof Entity) {
            ipmsgProtocol = new MSGProtocol(imei, commandNo, (Entity) addData);
        }
        else if (addData instanceof String) {
            ipmsgProtocol = new MSGProtocol(imei, commandNo, (String) addData);
        }
        sendUDPdata(ipmsgProtocol, targetIP);
    }

    public static void sendUDPdata(final MSGProtocol ipmsgProtocol, final String targetIP) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    InetAddress targetAddr = InetAddress.getByName(targetIP); // 目的地址
                    sendBuffer = ipmsgProtocol.getProtocolJSON().getBytes("gbk");
                    sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, targetAddr, MSGConst.PORT);
                    UDPSocket.send(sendDatagramPacket);
                    LogUtils.i(TAG, "sendUDPdata() 数据发送成功");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "sendUDPdata() 发送UDP数据包失败");
                }

            }
        });

    }
    /**
     * 新消息处理接口
     */
    public interface OnNewMsgListener {
        public void processMessage(android.os.Message pMsg);
    }

}