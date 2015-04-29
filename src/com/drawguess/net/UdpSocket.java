package com.drawguess.net;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.msgbean.Entity;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import android.content.Context;

/**
 * UDP连接的线程
 * @author GuoJun
 *
 */
/**
 * UDP连接的线�?
 * @author GuoJun
 *
 */
public class UdpSocket implements Runnable {


    private static final String TAG = "UDPMessageListener";
    private static final String BROADCASTIP = "255.255.255.255";
    private static UdpSocket instance;
    
    private static byte[] receiveBuffer = new byte[Constant.BUFFER_SIZE];
    private static byte[] sendBuffer = new byte[Constant.BUFFER_SIZE];
    private static DatagramPacket sendDatagramPacket;
    private static DatagramSocket UDPSocket;
    private boolean isThreadRunning;
    private List<OnMsgRecListener> mListenerList;
	private DatagramPacket receiveDatagramPacket;
    private Thread receiveUDPThread;
    private String serverIp;
    
    public UdpSocket() {
        mListenerList = new ArrayList<OnMsgRecListener>();
        serverIp = null;
    }

    public void addMsgListener(OnMsgRecListener listener) {
        this.mListenerList.add(listener);
    }
    
    public String getServerIp(){
    	return serverIp;
    }


    /** �?始监听线�? **/
    public void start() {
        if (receiveUDPThread == null) {
            receiveUDPThread = new Thread(this);
            receiveUDPThread.start();
        }
        isThreadRunning = true;
        LogUtils.i(TAG, "startUDPSocketThread() 线程启动成功");
    }
    
    /** 暂停监听线程 **/
    public void stop() {
        isThreadRunning = false;
        if (receiveUDPThread != null)
            receiveUDPThread.interrupt();
        receiveUDPThread = null;
        instance = null; // 置空
        LogUtils.i(TAG, "stopUDPSocketThread() 线程停止成功");
    }
    
    /** 建立Socket连接 **/
    public void connectUDPSocket() {
        try {
            // 绑定端口
            if (UDPSocket == null)
                UDPSocket = new DatagramSocket(Constant.UDP_PORT);

            
            LogUtils.i(TAG, "connectUDPSocket() 绑定端口成功");

            // 创建数据接受包
            if (receiveDatagramPacket == null)
                receiveDatagramPacket = new DatagramPacket(receiveBuffer, Constant.BUFFER_SIZE);

        }
        catch (SocketException e) {
        }
    }

    public void removeMsgListener(OnMsgRecListener listener) {
        this.mListenerList.remove(listener);
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
                break;
            }

            if (receiveDatagramPacket.getLength() == 0) {
                LogUtils.e(TAG, "无法接收UDP数据或�?�接收到的UDP数据为空");
                continue;
            }

            String UDPListenResStr = "";
            try {
                UDPListenResStr = new String(receiveBuffer, 0, receiveDatagramPacket.getLength(),"gbk");
            }
            catch (UnsupportedEncodingException e) {
                LogUtils.e(TAG, "系统不支持GBK编码");
            }

            MSGProtocol msgRes;
			try {
				msgRes = new MSGProtocol(UDPListenResStr);
				int command = msgRes.getCommandNo();
	            
	            //调试模式下允许自己
	            if (!SessionUtils.isLocalUser(msgRes.getSenderIMEI())) 
	            {
	            	switch(command){
	            	case MSGConst.BROAD_FIND:
	            		if(NetManage.getState() == 2)
	            			sendUDPdata(MSGConst.BROAD_REC,receiveDatagramPacket.getAddress(),SessionUtils.getLocalIPaddress());
	            		break;
	            	case MSGConst.BROAD_REC:
	            		serverIp = msgRes.getAddStr();
	            		LogUtils.i(TAG, "找到服务器IP");
	                    break;
	            	}

	                for (OnMsgRecListener msgListener: mListenerList) {
	                    msgListener.processMessage(msgRes);
	                }
	            }
			} catch (JSONException e) {
        		LogUtils.e(TAG, "UDP json解析失败");
			}
            

            // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断
            if (receiveDatagramPacket != null) {
                receiveDatagramPacket.setLength(Constant.BUFFER_SIZE);
            }

        }

        receiveDatagramPacket = null;
        if (UDPSocket != null) {
            UDPSocket.close();
            UDPSocket = null;
        }
        receiveUDPThread = null;

    }

    public void notifiBroad(){
        sendUDPdata(MSGConst.BROAD_FIND, UdpSocket.BROADCASTIP, SessionUtils.getLocalIPaddress());
    }
    



    /**
     * 获取UDPSocketThread实例
     * 单例模式，返回唯一实例
     * 
     * @param paramApplication
     * @return instance
     */
    public static UdpSocket getInstance(Context context) {
        if (instance == null) {
            instance = new UdpSocket();
        }
        return instance;
    }

    /**
     * 发�?�UDP数据�?
     * 
     * @param commandNo
     *            消息命令
     * @param targetIP
     *            目标地址
     * @param addData
     *            附加数据
     * @see MSGConst
     */
    public void sendUDPdata(int commandNo, InetAddress targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }
    public void sendUDPdata(int commandNo, InetAddress targetIP, Object addData) {
        sendUDPdata(commandNo, targetIP.getHostAddress(), addData);
    }
    public void sendUDPdata(int commandNo, String targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public void sendUDPdata(int commandNo, String targetIP, Object addData) {
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

    public void sendUDPdata(MSGProtocol msg, String targetIP) {
		try {
			sendBuffer = msg.getProtocolJSON().getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			LogUtils.e(TAG, "json error");
		}
    	SendThread sendThread = new SendThread(targetIP);
    	sendThread.start();
    }

    private class SendThread extends Thread{
        private boolean SEND_FLAG = true; // 是否发送广播标志
    	private String targetIP;
    	public SendThread(String targetIP) {
    		this.targetIP = targetIP;
    	}
    	 
		@Override
		public void run() {
            if (SEND_FLAG) {
				try {
					InetAddress targetAddr = InetAddress.getByName(targetIP); // 目的地址
					sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, targetAddr, Constant.UDP_PORT);
					UDPSocket.send(sendDatagramPacket);
				  	LogUtils.i(TAG, "sendUDPdata() 数据发送成功");
			  	}
				catch (Exception e) {
	                SEND_FLAG = false;
					LogUtils.e(TAG, "sendUDPdata() 数据发送失败");
				}
            }
		}
    }
}