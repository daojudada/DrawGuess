package com.drawguess.net;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.net.MSGProtocol.ADDITION_TYPE;
import com.drawguess.util.LogUtils;

import android.content.Context;

public class TcpServer implements Runnable {
    private static final String TAG = "TcpServer";
    private static ArrayList<MSGProtocol> msgCacheList;
    private static int msgId = 0;
    private static TcpServer instance;

    private List<ServerThread> threadList ;
    
    
    private Thread receiveThread;
    private ServerSocket serverSocket ;
    private List<OnMsgRecListener> mListenerList;
    private boolean isThreadRunning ; // 是否线程开始标志

  
    private TcpServer() {
        mListenerList = new ArrayList<OnMsgRecListener>();
        threadList = new LinkedList<ServerThread>(); 
        LogUtils.i(TAG, "建立线程成功");

    }

    public Thread getThread() {
        return receiveThread ;
    }

    public int getMsgId(){
    	return msgId;
    }

    public void addMsgListener(OnMsgRecListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeMsgListener(OnMsgRecListener listener) {
        this.mListenerList.remove(listener);
    }

    /**
     * <p>
     * 获取TcpService实例
     * <p>
     * 单例模式，返回唯一实例
     */
    public static TcpServer getInstance(Context context) {
        if (instance == null) {
            instance = new TcpServer();
            msgCacheList = new ArrayList<MSGProtocol>(100);
        }
        return instance;
    }

    private TcpServer(Context context) {
        this();
        LogUtils.i(TAG, "TCP_Client初始化完毕");
    }

    public void start() {
        connect();
        isThreadRunning  = true; // 使能发送标识
        if (receiveThread == null) {
            receiveThread = new Thread(this);
            receiveThread.start();
        }
        LogUtils.i(TAG, "发送线程开启");
    }

    /** 暂停监听线程 **/
    public void stop() {
    	isThreadRunning  = false;
        if (receiveThread != null)
            receiveThread.interrupt();
        receiveThread = null;
        instance = null; // 置空, 消除静�?�变量引�?
        LogUtils.i(TAG, "stopUDPSocketThread() 线程停止成功");
    }
    
    /** 建立Socket连接 **/
    public void connect() {
        try {
            // 绑定端口
            if (serverSocket == null){
            	serverSocket = new ServerSocket();
            	serverSocket.setReceiveBufferSize(Constant.BUFFER_SIZE);
            	serverSocket.bind(new InetSocketAddress(Constant.TCP_PORT));
            }
            LogUtils.i(TAG, "ServerSocket() 绑定端口成功");
        }
	    catch (UnknownHostException e) {
	        LogUtils.e(TAG, "建立服务器socket失败");
	        isThreadRunning = false;
	    }
	    catch (IOException e) {
	        LogUtils.e(TAG, "建立服务器socket失败");
	        isThreadRunning = false;
	    }
    }
    
    
    @Override
    public void run(){
		Socket socket = null;
		ServerThread serverThread = null;
    	while(isThreadRunning){  
    		try {            
				//监听客户端请求，启个线程处理                
				socket = serverSocket.accept();  
				socket.setTcpNoDelay(true);
				serverThread = new ServerThread(socket);
                threadList.add(serverThread);
				LogUtils.i(TAG, "监听到客户端"+socket.getInetAddress().getHostAddress());   
			}catch (Exception e) {   
				LogUtils.e(TAG, "监听客户端失败");
			}
    	}
        

    }
    
   
    /**
     * 发送给所有客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToAllClient(MSGProtocol msg) {
		//如果是绘图数据，给数据包打上编号。并缓存
		if(msg.getAddType() == ADDITION_TYPE.DATADRAW){
    		msg.setId(msgId);
    		msgCacheList.add(msg);
    		msgId++;
		}
    	for(ServerThread thread : threadList){
    			thread.sendData(msg);
    	}
    }
    
    /**
     * 发送给除了id的所有客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToAllExClient(MSGProtocol msg, String imei) {
    	for(ServerThread thread : threadList){
    		if(!thread.getIMEI().equals(imei))
    			thread.sendData(msg);
    	}
    }
    
    /**
     * 发送给指定id客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToClient(MSGProtocol msg, String imei) {
    	for(ServerThread thread : threadList){
    		if(thread.getIMEI().equals(imei))
    			thread.sendData(msg);
    	}
    }
    
    
    /**
     * 服务器线程类
     */
    class ServerThread extends Thread{
        private Socket client;
        private DataOutputStream dataOutPut;
        private DataInputStream dataInPut;
        private byte[] receiveBuffer = new byte[Constant.BUFFER_SIZE];// 数据报内容
        private byte[] sendBuffer = new byte[Constant.BUFFER_SIZE]; // 数据报内容
        private String imei;
        
        public ServerThread(Socket s) throws IOException {
            client = s;
			dataOutPut = new DataOutputStream(client.getOutputStream());
			dataInPut = new DataInputStream(client.getInputStream());
            start();
        }
          
        public String getIMEI(){
        	return imei;
        }
        public void setIMEI(String i){
        	imei = i;
        }

        
        /**
         * 向客户端socket写入数据
         * @param msg协议串
         */
        public void sendData(MSGProtocol msg){
        	try {
        		String msgProtocol = msg.getProtocolJSON();
        		String msgAll =  msgProtocol + "@sp"; 
				sendBuffer = msgAll.getBytes("gbk");
				dataOutPut.write(sendBuffer);
				dataOutPut.flush();
			} catch (UnsupportedEncodingException e) {
			    LogUtils.e(TAG, "系统不支持GBK编码");
			} catch (IOException e) {
				LogUtils.e(TAG, "send to client error");
			}
            sendBuffer = new byte[Constant.BUFFER_SIZE];
			LogUtils.i(TAG, "send to client successful");
        }
        
        @Override
        public void run() {
        	while(isThreadRunning)
			{
				try {
					dataInPut.read(receiveBuffer);
				}
				catch (IOException e) {
				    isThreadRunning = false;
				    receiveThread = null;
				    LogUtils.e(TAG, "数据包接收失败！线程停止");
				}
				
				String TCPListenResStr = "";
				try {
				    TCPListenResStr = new String(receiveBuffer,"gbk");
				}
				catch (UnsupportedEncodingException e) {
				    LogUtils.e(TAG, "系统不支持GBK编码");
			    }
				receiveBuffer = new byte[Constant.BUFFER_SIZE];
			    MSGProtocol msgRes;
				try {
					msgRes = new MSGProtocol(TCPListenResStr);
				    int command = msgRes.getCommandNo();
					LogUtils.i(TAG, "收到TCP消息     " + command);
			    	switch(command){
			    	case MSGConst.SEND_ONLINE:
			    		setIMEI(msgRes.getSenderIMEI());
			    		break;
			    	case MSGConst.SEND_OFFLINE:
			    		threadList.remove(ServerThread.this);
			    		break;
		    		default:
		    			break;
			    	}
			
			        for (OnMsgRecListener msgListener: mListenerList) {
			            msgListener.processMessage(msgRes);
			        }
				} catch (JSONException e) {
					LogUtils.e(TAG, "server recBuffer json解析失败");
				}
			}
            try {
            	dataOutPut.close();
            	dataInPut.close();
                client.close();
            }catch (IOException e) {
            }
            threadList.remove(this);
        }
    }
}