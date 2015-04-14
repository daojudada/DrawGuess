package com.drawguess.net;


import java.io.IOException;
import java.net.InetAddress;

import com.drawguess.interfaces.MSGListener;
import com.drawguess.msgbean.Entity;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * 网络层接口，还未抽象
 * @author GuoJun
 *
 */
public class WifiNet{
	public enum SocketMode{TCP,UDP};
    private static final String TAG = "WifiNet";
    private Client mClient;
    private Server mServer;
    private InetAddress serverIp = null;
    private MSGListener clientListener;
    private MSGListener serverListener;
    private boolean isServer;
    
    public WifiNet(MSGListener clientListener, MSGListener serverListener) {
        this.clientListener = clientListener;
        this.serverListener = serverListener;
    }
    
    public Client getClient(){
    	return mClient;
    }
    
    public Server getServer(){
    	return mServer;
    }
    
    public void createClient(){
    	isServer = false;
    	mClient = new Client();
    	mClient.start();
    	
    	mClient.getKryo().register(MSGProtocol.class);
    	mClient.getKryo().register(RegisterIMEI.class);
    	
    	mClient.addListener(new Listener() {
			public void connected (Connection connection) {
				RegisterIMEI ri = new RegisterIMEI();
				ri.imei = SessionUtils.getIMEI();
				sendToServer(ri,SocketMode.TCP);
			}

			public void received (Connection connection, Object object) {
				if (object instanceof MSGProtocol) {
					MSGProtocol ipmsg = (MSGProtocol)object;
					clientListener.handleIPMSG(connection,ipmsg);
			        LogUtils.i(TAG, "收到数据包成功"+ipmsg.getSenderIMEI());
					return;
				}
			}
			public void disconnected (Connection connection) {
				
			}
		});
		
    }
    
    public void createServer() throws IOException{
    	isServer = true;
    	mServer = new Server(){
    		protected Connection newConnection(){
    			return new PlayerConnection();
    		}
    	};
    	
    	mServer.getKryo().register(MSGProtocol.class);
    	mServer.getKryo().register(RegisterIMEI.class);
    	
    	mServer.addListener(new Listener() {
			public void received (Connection c, Object object) {
				PlayerConnection connection = (PlayerConnection)c;
				if (object instanceof MSGProtocol){
					if (connection.imei == null) return;
					MSGProtocol ipmsg = (MSGProtocol)object;
					serverListener.handleIPMSG(connection,ipmsg);
		            LogUtils.i(TAG, "收到客户端消息"+ipmsg.getSenderIMEI());
				}
				else if(object instanceof RegisterIMEI){
					if (connection.imei != null) return;
					String imei = ((RegisterIMEI)object).imei;
					if (imei == null) return;
					connection.imei = imei;
		            LogUtils.i(TAG, "收到客户端注册"+imei);
				}
			}
			public void disconnected (Connection c) {
				
			}
		});
    	
        mServer.bind(Constant.TCP_PORT, Constant.UDP_PORT);
        mServer.start();
    }
    
    public boolean findServer(){
    	serverIp = mClient.discoverHost(Constant.UDP_PORT, 3000);
    	if(serverIp==null)
    		return false;
    	return true;
    }
    
    public boolean connectServer(){
		try {
			mClient.connect(3000, serverIp, Constant.TCP_PORT, Constant.UDP_PORT);
		} catch (IOException e) {
            LogUtils.i(TAG, "客户端连接失败");
			e.printStackTrace();
			return false;
		}
		return true;
    }
    
    public void stopNet(){
    	if(isServer){
    		mServer.stop();
    	}
    	else{
    		mClient.stop();
    	}
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
    
    /**
     * 发送ipmsg数据到客户端
     * @param commandNo
     * @param addData
     * @param sm
     */
    public void sendToServer(int commandNo, Object addData, SocketMode sm) {
    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
    	if(ipmsg!=null){
    		sendToServer(ipmsg,sm);
    	}
    }
    /**
     * 发送数据到客户端
     * @param object
     * @param sm
     */
    public void sendToServer(Object object, SocketMode sm) {
        try {
        	if(sm == SocketMode.TCP)
        		mClient.sendTCP(object);
    		else
    			mClient.sendUDP(object);
            LogUtils.i(TAG, "sendToServer() 发送数据包成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "sendToServer() 发送数据包失败");
        }
    }
    
    /**
     * 发送ipmsg数据到指定客户端
     * @param commandNo 
     * @param addData 
     * @param connectionID
     * @param sm
     */
    public void sendToClient(int commandNo, Object addData,int connectionID, SocketMode sm) {
    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
    	if(ipmsg!=null){
    		sendToClient(ipmsg,connectionID,sm);
    	}
    }
    /**
     * 发送数据到指定客户端
     * @param object
     * @param connectionID
     * @param sm
     */
    public void sendToClient(Object object, int connectionID, SocketMode sm) {
    	try {
        	if(sm == SocketMode.TCP)
        		mServer.sendToTCP(connectionID,object);
    		else
        		mServer.sendToUDP(connectionID,object);
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "sendToClient() 发送数据包失败");
        }
        LogUtils.i(TAG, "sendToServer() 发送数据包成功");
    }
    
    public void sendToAllClient(int commandNo, Object addData, SocketMode sm) {
    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
    	if(ipmsg!=null){
	        sendToAllClient(ipmsg,sm);
	    }
    }
    public void sendToAllClient(Object object, SocketMode sm) {
    	try {
        	if(sm == SocketMode.TCP)
        		mServer.sendToAllTCP(object);
    		else
        		mServer.sendToAllUDP(object);
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "sendToClient() 发送数据包失败");
        }
        LogUtils.i(TAG, "sendToAllClient() 发送数据包成功");
    }
    
    public void sendToAllExClient(int commandNo, Object addData, int connectionID, SocketMode sm) {
    	MSGProtocol ipmsg = packageMsg(commandNo,addData);
    	if(ipmsg!=null){
	        sendToAllExClient(ipmsg,connectionID,sm);
	    }
    }
    public void sendToAllExClient(Object object, int connectionID, SocketMode sm) {
    	try {
        	if(sm == SocketMode.TCP)
        		mServer.sendToAllExceptTCP(connectionID, object);
    		else
        		mServer.sendToAllExceptUDP(connectionID, object);
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "sendToClient() 发送数据包失败");
        }
        LogUtils.i(TAG, "sendToAllExClient() 发送数据包成功");
    }
    

    // This holds per connection state.
 	static class PlayerConnection extends Connection {
 		public String imei;
 	}
 		
 	static public class RegisterIMEI {
		public String imei;
	}
}