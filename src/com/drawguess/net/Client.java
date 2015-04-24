package com.drawguess.net;

import java.io.*;
import java.net.*;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.MSGListener;
import com.drawguess.util.LogUtils;




/**
 * TCP客户端，没写完
 * @author GuoJun
 *
 */
public class Client implements Runnable {
	private static final int BUFFERLENGTH = 4096; // 缓冲大小
    private static byte[] receiveBuffer = new byte[BUFFERLENGTH];
    private static byte[] sendBuffer = new byte[BUFFERLENGTH];
    private static Socket serverSocket = null;
	private static final String TAG = "Client";
	private InetAddress address = null;
	private InputStream dis = null;
	private OutputStream dos = null;
	private boolean isConnect = false;
	private MSGListener msgListener;

	public Client() {
		connect();
	}

	void connect() {
		try {
			address = InetAddress.getByName("localhost");
			serverSocket = new Socket(address, Constant.TCP_PORT);
			dos = new ObjectOutputStream(serverSocket.getOutputStream());
			dis = new ObjectInputStream(serverSocket.getInputStream());
			isConnect = true;
		} catch (IOException e) {
			isConnect = false;
			exit();
		}
	}

	private void exit() {
		System.out.println("Close Client");
		isConnect = false;
		try {
			if (dis != null)
				dis.close();
			if (dos != null)
				dos.close();
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receive() throws IOException {
		try{
			dis.read(receiveBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    String resStr = "";
        try {
            resStr = new String(receiveBuffer, 0, receiveBuffer.length,"gbk");
        }
        
        catch (UnsupportedEncodingException e) {
            LogUtils.e(TAG, "系统不支持GBK编码");
        }
        
		MSGProtocol msg = new MSGProtocol(resStr);
		
		
	}

	@Override
	public void run() {
		while (isConnect) {
			try {
				receive();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public void sendToServer(MSGProtocol ipmsg) {
		try {
			sendBuffer = ipmsg.getProtocolJSON().getBytes("gbk");
			dos.write(sendBuffer);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void setMsgListener(MSGListener msgListener){
		this.msgListener = msgListener;
	}

	
	public void startGame() {
		Thread thread = new Thread(this);
		thread.start();
	}

}
