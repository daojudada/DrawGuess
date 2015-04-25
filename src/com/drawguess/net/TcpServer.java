package com.drawguess.net;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.MSGListener;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;

import android.content.Context;

public class TcpServer implements Runnable {
    private static final String TAG = "TcpServer";
    private static TcpServer instance;
    private static final int POOL_SIZE = 5; // 单个CPU线程池大小
    

    private List<ServerThread> threadList ;
    private ArrayBlockingQueue<MSGProtocol> msgQueue;
    
    private SendMsgThread sendMsgThread;
    private Thread receiveThread;
    private ServerSocket serverSocket ;
    private List<MSGListener> mListenerList;
    private boolean isThreadRunning ; // 是否线程开始标志

    private TcpServer() {
        int cpuNums = Runtime.getRuntime().availableProcessors();
        mListenerList = new ArrayList<MSGListener>();
        threadList = new LinkedList<ServerThread>(); 
        msgQueue = new ArrayBlockingQueue<MSGProtocol>(cpuNums * POOL_SIZE);
        LogUtils.i(TAG, "建立线程成功");

    }

    public Thread getThread() {
        return receiveThread ;
    }


    public void addMsgListener(MSGListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeMsgListener(MSGListener listener) {
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
        }
        return instance;
    }

    private TcpServer(Context context) {
        this();
        LogUtils.i(TAG, "TCP_Client初始化完毕");
    }

    public void start() {
        connect();
        sendMsgThread = new SendMsgThread();
        sendMsgThread.start();
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
    	sendMsgThread = null;
        instance = null; // 置空, 消除静�?�变量引�?
        LogUtils.i(TAG, "stopUDPSocketThread() 线程停止成功");
    }
    
    /** 建立Socket连接 **/
    public void connect() {
        try {
            // 绑定端口
            if (serverSocket == null)
            	serverSocket = new ServerSocket(Constant.TCP_PORT);
            LogUtils.i(TAG, "ServerSocket() 绑定端口成功");
        }
	    catch (UnknownHostException e) {
	        LogUtils.e(TAG, "建立服务器socket失败");
	        isThreadRunning = false;
	        e.printStackTrace();
	    }
	    catch (IOException e) {
	        LogUtils.e(TAG, "建立服务器socket失败");
	        isThreadRunning = false;
	        e.printStackTrace();
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
				serverThread = new ServerThread(socket);
                threadList.add(serverThread);
				LogUtils.i(TAG, "监听到客户端"+socket.getInetAddress().getHostAddress());   
			}catch (Exception e) {   
				e.printStackTrace();
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
    	for(ServerThread thread : threadList){
    			thread.isSend = true;
    	}
    	try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			LogUtils.e(TAG, "Queue put wrong");
			e.printStackTrace();
		}
    }
    
    /**
     * 发送给除了id的所有客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToAllExClient(MSGProtocol msg, String imei) {
    	for(ServerThread thread : threadList){
    		if(thread.getIMEI() != imei)
    			thread.isSend = true;
    	}
    	try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			LogUtils.e(TAG, "Queue put wrong");
			e.printStackTrace();
		}
    }
    
    /**
     * 发送给指定id客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToClient(MSGProtocol msg, String imei) {
    	for(ServerThread thread : threadList){
    		if(thread.getIMEI() == imei)
    			thread.isSend = true;
    	}
    	
    	try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			LogUtils.e(TAG, "Queue put wrong");
			e.printStackTrace();
		}
    }
    
    
    /**
     * 监听是否有输出消息请求线程类,向客户端发送消息
     */
    class SendMsgThread extends Thread{
        @Override
        public void run() {
            while(true){
                MSGProtocol message = null;
				try {
					message = msgQueue.take();
				} catch (InterruptedException e) {
					LogUtils.e(TAG, "msgQueue wrong");
					e.printStackTrace();
				}
				
                for (ServerThread thread : threadList) {
                    thread.sendData(message);
                }
            }
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
        private boolean isSend;
        private String imei;
        
        public ServerThread(Socket s) throws IOException {
            client = s;
			dataOutPut = new DataOutputStream(client.getOutputStream());
			dataInPut = new DataInputStream(client.getInputStream());
            isSend = false;
            start();
        }
          
        public String getIMEI(){
        	return imei;
        }
        public void setIMEI(String i){
        	imei = i;
        }
        public void setIsSend(boolean b){
        	isSend = b;
        }
        
        /**
         * 向客户端socket写入数据
         * @param msg协议串
         */
        public void sendData(MSGProtocol msg){
        	if(isSend){
	        	try {
					sendBuffer = msg.getProtocolJSON().getBytes("gbk");
					dataOutPut.write(sendBuffer);
					dataOutPut.flush();
				} catch (UnsupportedEncodingException e) {
				    LogUtils.e(TAG, "系统不支持GBK编码");
					e.printStackTrace();
				} catch (IOException e) {
					LogUtils.e(TAG, "send to client error");
					e.printStackTrace();
				}
	        	isSend = false;
				LogUtils.i(TAG, "send to client successful");
        	}
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
				    e.printStackTrace();
				}
				
				String TCPListenResStr = "";
				try {
				    TCPListenResStr = new String(receiveBuffer,"gbk");
                    receiveBuffer = new byte[Constant.BUFFER_SIZE];
				}
				catch (UnsupportedEncodingException e) {
				    LogUtils.e(TAG, "系统不支持GBK编码");
			    }
				
			    MSGProtocol msgRes = new MSGProtocol(TCPListenResStr);
			    int command = msgRes.getCommandNo();
				LogUtils.i(TAG, "收到TCP消息" + command);
			    if (!SessionUtils.isLocalUser(msgRes.getSenderIMEI())) 
			    {
			    	switch(command){
			    	case MSGConst.SEND_ONLINE:
			    		setIMEI(msgRes.getSenderIMEI());
			    		break;
		    		default:
		    			break;
			    	}
			
			        for (MSGListener msgListener: mListenerList) {
			            msgListener.processMessage(msgRes);
			        }
			    }
			}
            try {
            	dataOutPut.close();
            	dataInPut.close();
                client.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            threadList.remove(this);
        }
          
        
          
    }
}


