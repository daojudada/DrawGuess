package com.drawguess.net;


import java.util.HashMap;

import android.content.Context;

import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.interfaces.NetInterface;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.User;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;

/**
 * 网络层管理类
 * @author GuoJun
 *
 */
public class NetManage implements NetInterface{
    public enum SocketMode{TCP,UDP}
    
    private static HashMap<String,User> mServerUsersMap; // 服务器在线用户列表
    private static HashMap<String,User> mLocalUsersMap; // 客户端在线用户列表
    
    private static final String TAG = "WifiNet";
    private static NetManage instance;
    private static Context mContext;
    private static int state = 0;
    private SocketMode sm;
    private UdpSocket mUDPListener;
    private TcpClient mClient;
    private TcpServer mServer;
    private String serverIp = null;
    

    /**
     * 得到目前网络管理状态
     * 0：空
     * 1：客户端
     * 2：服务器
     */
    public static int getState(){
    	return state;
    }

    /**
     * 得到目前网络管理状态
     * 0：空
     * 1：客户端
     * 2：服务器
     */
    public static void setState(int s){
    	state = s;
    }
    
    public static NetManage getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            mLocalUsersMap = new HashMap<String,User>();
            mServerUsersMap = new HashMap<String,User>();
            instance = new NetManage();
        }
        return instance;
    }
    
    public static HashMap<String,User> getLocalUserMap(){
    	return mLocalUsersMap;
    }
    

    public static HashMap<String,User> getServerUserMap(){
    	return mServerUsersMap;
    }
    
    @Override
    public void addClientListener(OnMsgRecListener listener){
    	mClient.addMsgListener(listener);
    }
    

    @Override
    public void addServerListener(OnMsgRecListener listener){
    	mServer.addMsgListener(listener);
    }
    
    public void addUdpListener(OnMsgRecListener listener){
    	mUDPListener.addMsgListener(listener);
    }
    
    @Override
    public void removeClientListener(OnMsgRecListener listener){
    	mClient.removeMsgListener(listener);
    }

    @Override
    public void removeServerListener(OnMsgRecListener listener){
    	mServer.removeMsgListener(listener);
    }
    
    public void removeUdpListener(OnMsgRecListener listener){
    	mUDPListener.removeMsgListener(listener);
    }

    public void setSocketMode(SocketMode sm){
    	this.sm = sm;
    }

	public void createUDP(){
        mUDPListener = UdpSocket.getInstance(mContext);
		mUDPListener.connectUDPSocket();
		mUDPListener.start();
	}

    @Override
    public void createClient(){
    	mClient = TcpClient.getInstance(mContext);
    }

    @Override
    public void connectServer(String ip){
		mClient.connect(ip);
    }
    
    
    @Override
    public void startServer(){
    	mServer.start();
    }
    
    @Override
    public void startClient(){
    	mClient.start();
    }
    
    @Override
    public void createServer(){
    	mServer= TcpServer.getInstance(mContext);
    }

    @Override
    public boolean findServer(long timeOut){
    	if(state == 0){
    		int n = (int) (timeOut / 300);
    		while(n-->0){
        		mUDPListener.notifiBroad();
        		try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
        		serverIp = mUDPListener.getServerIp();
    			if(serverIp != null){
    				LogUtils.i(TAG, "找到服务器"+serverIp);
    				return true;
    			}
    		}
    		LogUtils.i(TAG, "没有找到服务器");
			return false;
    	}
    	else return false;
    }

    @Override
    public TcpClient getClient(){
    	return mClient;
    }

    @Override
    public TcpServer getServer(){
    	return mServer;
    }
    
    public String getServerIp(){
    	return serverIp;
    }
    
    /**
     * 打包数据包
     * 
     * @param commandNo
     *            消息命令
     * @param addData
     *            附加数据
     * @see MSGConst
     */
    private MSGProtocol packageMsg(int commandNo, Object addData) {
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
        return ipmsgProtocol;
    }

    @Override
    public void sendToAllClient(int commandNo, Object addData) {
    	if(state == 2){
	    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
	    	if(ipmsg!=null){
            	if(sm == SocketMode.TCP)
            		mServer.sendToAllClient(ipmsg);
		    }
    	}
    }
    
    @Override
    public void sendToAllExClient(int commandNo, Object addData, String imei) {
    	if(state == 2){
	    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
	    	if(ipmsg!=null){
	
	        	try {
	            	if(sm == SocketMode.TCP)
	            		mServer.sendToAllExClient(ipmsg, imei);
	        		//else
	        			
	            }
	            catch (Exception e) {
	                LogUtils.e(TAG, "sendToClient() 发送数据包失败");
	            }
	            LogUtils.i(TAG, "sendToAllExClient() 发送数据包成功");
		    }
    	}
    }
    
    @Override
    public void sendToClient(int commandNo, Object addData, String imei) {
    	if(state == 2){
	    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
	    	if(ipmsg!=null){
	        	try {
	            	if(sm == SocketMode.TCP)
	            		mServer.sendToClient(ipmsg, imei);
	            }
	            catch (Exception e) {
	                LogUtils.e(TAG, "sendToClient() 发送数据包失败");
	            }
	            LogUtils.i(TAG, "sendToServer() 发送数据包成功");
	    	}
    	}
    }
    
    

    @Override
    public void sendToServer(int commandNo, Object addData) {
    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
    	if(ipmsg!=null){
        	if(sm == SocketMode.TCP)
        		mClient.sendToServer(ipmsg);
    		else
    			mUDPListener.sendUDPdata(ipmsg, serverIp);
    	}
    }
    
    
 	public void stopUdp(){
 		mUDPListener.stop();
    }

    @Override
 	public void stopNet(){
 		mUDPListener.stop();
 		if(state == 1){
    		mClient.stop();
    	}
    	else if(state == 2){
    		mServer.stop();
    		mClient.stop();
    	}
    }
}