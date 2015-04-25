package com.drawguess.interfaces;

import com.drawguess.interfaces.MSGListener;
import com.drawguess.net.TcpClient;
import com.drawguess.net.TcpServer;


/**
 * 消息处理接口
 * @author GuoJun
 *
 */

public interface NetInterface {
	    
		/**
		 * 添加客户端消息处理监听回调
		 * @param listener
		 */
	    public void addClientListener(MSGListener listener);

		/**
		 * 添加服务器消息处理监听回调
		 * @param listener
		 */
	    public void addServerListener(MSGListener listener);

		/**
		 * 删除客户端消息处理监听回调
		 * @param listener
		 */
	    public void removeClientListener(MSGListener listener);

		/**
		 * 删除服务器消息处理监听回调
		 * @param listener
		 */
	    public void removeServerListener(MSGListener listener);

	    /**
	     * 连接服务器
	     * @return is connected successful
	     */
	    public void connectServer();
		   
	    /**
	     * 创建客户端
	     */
	    public void createClient();
	    
	    /**
	     * 创建服务器
	     */
	    public void createServer();
	    
	    /**
	     * 开启客户端线程
	     */
	    public void startClient();
	    
	    /**
	     * 开启服务器线程
	     */
	    public void startServer();
	    
	    /**
	     * 寻找服务器
	     * @return is find successful
	     * @param find time
	     */
	    public boolean findServer(long timeOut);
	    
	    /**
	     * 得到客户端实例
	     * @return client
	     */
	    public TcpClient getClient();
	    
	    /**
	     * 得到服务器实例
	     * @return server
	     */
	    public TcpServer getServer();
	    
	    /**
	     * 发送ipmsg数据到所有客户端
	     * @param commandNo 
	     * @param addData 
	     * @param connectionID
	     * @param sm
	     */
	    public void sendToAllClient(int commandNo, Object addData);
	    
	    /**
	     * 发送ipmsg数据到除了某客户端的所有客户端
	     * @param commandNo 
	     * @param addData 
	     * @param connectionID
	     * @param sm
	     */
	    public void sendToAllExClient(int commandNo, Object addData, String imei) ;
	     
	    /**
	     * 发送ipmsg数据到指定客户端
	     * @param commandNo 
	     * @param addData 
	     * @param connectionID
	     * @param sm
	     */
	   	public void sendToClient(int commandNo, Object addData,String imei) ;
	
	    /**
	     * 发送ipmsg数据到服务器
	     * @param commandNo
	     * @param addData
	     * @param sm
	     */
	    public void sendToServer(int commandNo, Object addData) ;
	  
	 	/**
	 	 * 停止网络服务	
	 	 */
	 	public void stopNet();


}