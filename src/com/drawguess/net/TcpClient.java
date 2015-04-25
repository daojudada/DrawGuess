package com.drawguess.net;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.MSGListener;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class TcpClient implements Runnable {
    private static final String TAG = "TcpClient";
    private static TcpClient instance;
    private static byte[] sendBuffer = new byte[Constant.BUFFER_SIZE]; // 数据报内容
    private static byte[] receiveBuffer = new byte[Constant.BUFFER_SIZE];// 数据报内容
    private static boolean isConnect;
    private MyHandler mHandler;
    private OutputStream output = null;
    private InputStream input = null;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;
    private Thread connectThread;
    private recDataThread receiveThread;
    
    private Socket socket ;
    private List<MSGListener> mListenerList;
    private String serverIp;
    private boolean isThreadRunning ; // 是否线程开始标志

    private TcpClient() {
        mListenerList = new ArrayList<MSGListener>();
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
    public static TcpClient getInstance(Context context) {
        if (instance == null) {
            instance = new TcpClient();
        }
        return instance;
    }

    private TcpClient(Context context) {
        this();
        LogUtils.i(TAG, "TCP_Client初始化完毕");
    }

    /** 开始监听线程 **/
    public void start() {
        isThreadRunning  = true; // 使能发送标识
        LogUtils.i(TAG, "发送线程开启");
    }

    /** 暂停监听线程 **/
    public void stop() {
        try {
        	input.close();
        	dataInput.close();
            output.close();
			dataOutput.close();
	        socket.close();
	    	isThreadRunning  = false;
	        if (receiveThread != null)
	            receiveThread.interrupt();
	        receiveThread = null;
	        instance = null; // 置空,
	        LogUtils.i(TAG, "stopSocketThread() 线程停止成功");
		} catch (IOException e) {
	        LogUtils.i(TAG, "stopSocketThread() 线程停止失败");
			e.printStackTrace();
		}
    }

    /** 建立Socket连接 **/
    public void connect(String targetIp){
        isConnect = false;
    	this.serverIp = targetIp;
        if (connectThread == null) {
            connectThread = new Thread(this);
            connectThread.start();
        }

    }
    
    /**
	 * 主线程处理
	 */
    @Override
    public void run() {
    	try {
            // 绑定端口
            if (socket == null){
        		socket = new Socket(serverIp,Constant.TCP_PORT);
            }
            
            Looper.prepare();  
            
            mHandler = new MyHandler();

            output = socket.getOutputStream();
            input = socket.getInputStream();
            dataOutput = new DataOutputStream(output);
            dataInput = new DataInputStream(input);
            mHandler.sendEmptyMessage(1);
            LogUtils.i(TAG, "connectSocket() 绑定端口成功");
        	Looper.loop();  
        }catch (BindException e) {  
	        LogUtils.e(TAG,"IP地址或端口绑定异常！");
    	} catch (UnknownHostException e) { 
	        LogUtils.e(TAG, "未识别主机地址！"); 
    	}catch (SocketTimeoutException e) {  
	        LogUtils.e(TAG, "连接超时！");
    	}catch (ConnectException e) {  
	        LogUtils.e(TAG, "拒绝连接！");
    	} catch (IOException e) {
	        LogUtils.e(TAG, "连接失败");
			e.printStackTrace();
		}
    }
    
    public class MyHandler extends Handler{
    	@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
        	case 1: 
                isConnect = true;

                if (receiveThread == null) {
                	receiveThread =  new recDataThread();
                	receiveThread.start();
                }
                
        		break;
    	 	default:
    	 		break;
            }
		}
    }
    
        
    	
    public class recDataThread extends Thread{
        @Override
        public void run() {
        	while (isThreadRunning) {
            	try {
            		dataInput.read(receiveBuffer);
                }
                catch (IOException e) {
                    isThreadRunning = false;
                    if (socket != null) {
                        try {
    						socket.close();
    					} catch (IOException e1) {
    						e1.printStackTrace();
    					}
                        socket = null;
                    }
                    receiveThread = null;
                    LogUtils.e(TAG, "数据包接收失败！线程停止");
                    e.printStackTrace();
                    break;
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
                if (!SessionUtils.isLocalUser(msgRes.getSenderIMEI())) 
                {
                	switch(command){
                		default:
                			break;
                	}

                    for (MSGListener msgListener: mListenerList) {
                        msgListener.processMessage(msgRes);
                    }
                }

            }
            if (socket != null) {
                try {
    				socket.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                socket = null;
            }
            receiveThread = null;

        }
    }
        
    public void sendData(final MSGProtocol msg) {
     	while(!isConnect){
     	};
		new Thread(new Runnable() {
		     @Override
		     public void run() {
		         try {
		             sendBuffer = msg.getProtocolJSON().getBytes("gbk");
		             dataOutput.write(sendBuffer);
		             dataOutput.flush();
		             LogUtils.i(TAG, "sendData() 发送服务器数据包成功");
		         }
		         catch (Exception e) {
		             e.printStackTrace();
		             LogUtils.e(TAG, "sendData() 发送服务器数据包失败");
		         }
		     }
		}).start();
    }
    
}
