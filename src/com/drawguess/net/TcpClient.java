package com.drawguess.net;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.JSONException;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.util.LogUtils;

import android.content.Context;

public class TcpClient implements Runnable {
    private static final String TAG = "TcpClient";
    private static TcpClient instance;
    private static byte[] sendBuffer = new byte[Constant.BUFFER_SIZE]; // 数据报内容
    private static byte[] receiveBuffer = new byte[Constant.BUFFER_SIZE];// 数据报内容
    private OutputStream output = null;
    private InputStream input = null;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;
    private Thread connectThread;
    
    private recDataThread receiveThread;
    private sendDataThread sendThread;
    
    private ArrayBlockingQueue<MSGProtocol> msgQueue;
    
    private Socket socket ;
    private List<OnMsgRecListener> mListenerList;
    private String serverIp;
    private boolean isThreadRunning ; // 是否线程开始标志
    
    private TcpClient() {
        mListenerList = new ArrayList<OnMsgRecListener>();
        LogUtils.i(TAG, "建立线程成功");
        msgQueue = new ArrayBlockingQueue<MSGProtocol>(100);
    }

    public Thread getThread() {
        return receiveThread ;
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
		}
    }

    /** 建立Socket连接 **/
    public void connect(String targetIp){
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
        		socket = new Socket();
        		socket.setReceiveBufferSize(Constant.BUFFER_SIZE);
        		socket.setSendBufferSize(Constant.BUFFER_SIZE);
        		socket.setTcpNoDelay(true);
        		socket.connect(new InetSocketAddress(serverIp , Constant.TCP_PORT));
            }

            output = socket.getOutputStream();
            input = socket.getInputStream();
            dataOutput = new DataOutputStream(output);
            dataInput = new DataInputStream(input); 
            
            if (receiveThread == null) {
            	receiveThread =  new recDataThread();
            	receiveThread.start();
            }
            
            if (sendThread == null) {
            	sendThread =  new sendDataThread();
            	sendThread.start();
            }
            
            LogUtils.i(TAG, "connectSocket() 绑定端口成功");
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
		}
    }
    
    public class recDataThread extends Thread{
    	String saveStr = "";
    	
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
    					}
                        socket = null;
                    }
                    receiveThread = null;
                    LogUtils.e(TAG, "数据包接收失败！线程停止");
                    break;
                }

                String TCPListenResStr = "";
                try {
                    TCPListenResStr = new String(receiveBuffer,"gbk");
                }
                catch (UnsupportedEncodingException e) {
                    LogUtils.e(TAG, "系统不支持GBK编码");
                }
                
                receiveBuffer = new byte[Constant.BUFFER_SIZE];
                TCPListenResStr = saveStr + TCPListenResStr;
                
                MSGProtocol msgRes = null;
    	    	String[] strArray = null; 
    	    	strArray = TCPListenResStr.split("@sp"); 
        		for(int i = 0; i<strArray.length ; i++){
        			String sendMsg = strArray[i];
        			if(i == strArray.length -1 ){
    					saveStr = sendMsg.trim();
        			}
        			else{
        				try {
        					msgRes = new MSGProtocol(sendMsg);
        	                for (OnMsgRecListener msgListener: mListenerList) {
        	                    msgListener.processMessage(msgRes);
        	                }
        				} catch (JSONException e) {
        					LogUtils.e(TAG, "Client recBuffer json解析失败");
        				}
        			}
        		}
        		

            }
            if (socket != null) {
                try {
    				socket.close();
    			} catch (IOException e) {
    			}
                socket = null;
            }
            receiveThread = null;

        }
    }
    
    public class sendDataThread extends Thread{
        @Override
        public void run() {
        	while (isThreadRunning) {
            	MSGProtocol message = null;
				try {
					message = msgQueue.take();
				} catch (InterruptedException e) {
					LogUtils.e(TAG, "msgQueue wrong");
				}
				
             	try {
             		sendBuffer = message.getProtocolJSON().getBytes("gbk");
             		dataOutput.write(sendBuffer);
             		dataOutput.flush();
             		LogUtils.i(TAG, "sendData() 发送服务器数据包成功");
             	}
             	catch (Exception e) {
             		LogUtils.e(TAG, "sendData() 发送服务器数据包失败");
             	}
         		sendBuffer = new byte[Constant.BUFFER_SIZE];
            }
            if (socket != null) {
                try {
    				socket.close();
    			} catch (IOException e) {
    			}
                socket = null;
            }
            sendThread = null;
        }
    }
    

    /**
     * 发送给所有客户端
     * @param commandNo protocol command
     * @param addData add data
     */
    public void sendToServer(MSGProtocol msg) {
    	try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			LogUtils.e(TAG, "Queue put wrong");
		}
    }
    
}
