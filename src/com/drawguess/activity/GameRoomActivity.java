package com.drawguess.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.drawguess.R;
import com.drawguess.adapter.PlayersAdapter;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.BaseApplication;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.msgbean.User;
import com.drawguess.net.MSGConst;
import com.drawguess.net.MSGProtocol;
import com.drawguess.net.NetManage;
import com.drawguess.net.NetManage.SocketMode;
import com.drawguess.util.ImageUtils;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TypeUtils;
import com.drawguess.view.MultiListView;
import com.drawguess.view.MultiListView.OnRefreshListener;
import com.squareup.picasso.Picasso;

/**
 * 游戏房间，主要事件是寻找和建立服务器，准备以及开始
 * @author GuoJun
 *
 */
public class GameRoomActivity extends BaseActivity implements  OnItemClickListener, OnRefreshListener, OnClickListener {
	private static final String TAG = "GameRoomActivity";

    private boolean isMeReady;
    private boolean haveFind;

    private NetManage netManage;
    private PlayersAdapter mAdapter;
    private Button mBtnReady;
    private Button mBtnStart;
    private Button mBtnCreate;
    private ImageView mIvAvatar;
    private ImageView mIvGender;
    private LinearLayout mLayoutExGender; // 性别根布局
    private MultiListView mListView;
    private TextView mName;
    private TextView mPlayersNum;
    private TextView mOrder;
    
    /**
     * Local只允许在交互时与客户端收到消息时操作，Server只允许在服务器端收到消息时操作
     */
    private HashMap<String,User> mServerUsersMap; // 服务器在线用户列表
    private HashMap<String,User> mLocalUsersMap; // 客户端在线用户列表
    
    private ArrayList<String> mServerReadyList; //服务器已准备的用户列表
    private ArrayList<String> mServerStartList; //服务器已回应开始的用户列表
    
    private ArrayList<User> mLocalUsersList; // 客户端临时在线用户列表，用于adapter初始化
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        
        
        isMeReady = false;
        SessionUtils.setOrder(-1);
        
        haveFind = false;
        
        initViews();
        initEvents();
        
        //获取单例
        netManage = NetManage.getInstance(this);
        netManage.setSocketMode(SocketMode.TCP);
        mLocalUsersMap = NetManage.getLocalUserMap();
        mServerUsersMap = NetManage.getServerUserMap();
        
        //创建UDP广播线程
		netManage.createUDP();
		//寻找服务器
        findServerTask();
    }

    
	@Override
    protected void onDestroy() {
		/*
		if(NetManage.getState() == 2){
			netManage.sendToAllExClient(MSGConst.ANS_GAME_OVER, null, SessionUtils.getIMEI());
			netManage.stop();
			netManage.stopUdp();
		}
		else{
			netManage.sendToServer(MSGConst.SEND_OFFLINE, null);
			netManage.stop();
			netManage.stopUdp();
		}
		handler = null;
		*/
        super.onDestroy();
    }
	

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gameroom_create:
			NetManage.setState(2);
	        mServerReadyList =  new ArrayList<String>();
	        mServerStartList = new ArrayList<String>();
			createServer();
            createClient(SessionUtils.getLocalIPaddress());
        	showCustomToast("创建房间成功");
        	haveFind = true;
            LogUtils.i(TAG, "创建房间成功");
            break;
        case R.id.gameroom_ready:
        	if(!isMeReady)
        	{
        		//用户准备
        		mBtnReady.setBackgroundResource(R.drawable.btn_red_default);
        		mBtnReady.setText(R.string.gameroom_order);
    			netManage.sendToServer(MSGConst.SEND_READY, null);
        		isMeReady = true;
        	}
        	else
        	{
        		//用户取消准备
        		mBtnReady.setBackgroundResource(R.drawable.btn_bottombar_normal);
        		mBtnReady.setText(R.string.gameroom_ready);
    			netManage.sendToServer(MSGConst.SEND_UNREADY, null);
        		isMeReady = false;
        	}
        	//刷新UI
        	refreshAdapter();
        	refreshPlayersNum();
            break;

        case R.id.gameroom_start:
        	netManage.sendToAllClient(MSGConst.ANS_START, null);
            break;
		}
	}

    @Override
    public void onRefresh() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
            	if(!haveFind)
            		return netManage.findServer(2000);
            	else return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
            	if(result){
    	            LogUtils.i(TAG, "找到房间");
    	            //显示按钮
    	    		mBtnCreate.setVisibility(View.GONE);
    	    		mBtnReady.setVisibility(View.VISIBLE);
    				NetManage.setState(1);
    	            createClient(netManage.getServerIp());
    	            haveFind = true;
                	showCustomToast("连接房间成功");
                }
                else{
    	            LogUtils.i(TAG, "没有找到房间");
                	showCustomToast("没有找到房间");
                }
                mListView.onRefreshComplete();

            }
        });
    }
    
	/**
	 * 后台寻找服务器
	 */
	private void findServerTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
        	/**
        	 * 寻找服务器
        	 * @param params
        	 * @return 是否找到服务器
        	 */
            @Override
            protected Boolean doInBackground(Void... params) {
            	return netManage.findServer(2000);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
            	dismissLoadingDialog();
                if(result){
    	            LogUtils.i(TAG, "找到房间");
    	            BaseApplication.playNotification();
    	            //显示按钮
    	    		mBtnCreate.setVisibility(View.GONE);
    	    		mBtnReady.setVisibility(View.VISIBLE);
    				NetManage.setState(1);
    	            createClient(netManage.getServerIp());
                	showCustomToast("连接房间成功");
                }
                else{
    	            LogUtils.i(TAG, "没有找到房间");
                	showCustomToast("没有找到房间");
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog("正在寻找房间");
            }
        });
    }
	
	
	private void createClient(String serverIp){
        //创建客户端
    	netManage.createClient();
    	clientListener = new OnMsgRecListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
		        switch (command) {
		        case MSGConst.ANS_GAME_OVER:{//服务器退出游戏
		    		//显示开始按钮
		    		mBtnCreate.setVisibility(View.VISIBLE);
		    		mBtnReady.setVisibility(View.GONE);
		    		mBtnStart.setVisibility(View.GONE);
		        	mLocalUsersMap.clear();
		        	mLocalUsersList.clear();
		        	netManage.stop();
		        	netManage.setClient(null);
		        	NetManage.setState(0);
		        }
				case MSGConst.ANS_ONLINE:{ //服务器向客户端通报其他在线用户
					User user = (User)pMsg.getAddObject();
					if(!user.getIMEI().equals(SessionUtils.getIMEI())){
						//添加用户列表
						mLocalUsersMap.put(user.getIMEI(), user);
					}
				}
					break;
		        case MSGConst.ANS_OFFLINE: {//服务器向客户端通报其他下线用户
					String imei = pMsg.getAddStr();
					if(!imei.equals(SessionUtils.getIMEI())){
						//删除用户列表
						mLocalUsersMap.remove(imei);
					}
		        }
		            break;
		        case MSGConst.ANS_READY:{//服务器向客户端通报其他准备用户
		        	String lists = pMsg.getAddStr();
	        		setOrderList(TypeUtils.cStringToList(lists));
		        }
		            break;
		        case MSGConst.ANS_UNREADY:{//服务器向客户端通报其他准备用户
		        	String lists = pMsg.getAddStr();
		    		setOrderList(TypeUtils.cStringToList(lists));
		        }
		            break;
		        case MSGConst.ANS_START:{//收到开始请求
		        	if(isMeReady){
			        	if(!pMsg.getSenderIMEI().equals(SessionUtils.getIMEI())){
				    		netManage.removeClientListener(clientListener);
		    	            BaseApplication.playNotification();
				        	startActivity(DrawGuessActivity.class);
			        	}
		        		netManage.sendToServer(MSGConst.SEND_START, null);
		        	}
		        }
		        	break;
		        	
		        default:
		        	LogUtils.i(TAG, "wrong msg type");
		            break;
			    }

		        msg.what = command;
		        msg.setData(b);
				handler.sendMessage(msg);
			}
		};
    	//添加客户端消息回调
		netManage.addClientListener(clientListener);
		//连接服务器
		netManage.connectServer(serverIp);
		//开启客户端线程
		netManage.startClient();
		//通报客户端上线
		netManage.sendToServer(MSGConst.SEND_ONLINE, SessionUtils.getLocalUserInfo());
	}
	

	private void createServer(){
		//显示开始按钮
		mBtnCreate.setVisibility(View.GONE);
		mBtnReady.setVisibility(View.VISIBLE);
		mBtnStart.setVisibility(View.VISIBLE);
		 //创建服务器
        netManage.createServer();
        //开启服务器线程
		netManage.startServer();
		serverListener = new OnMsgRecListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
				int command = pMsg.getCommandNo();
				switch (command) {
				case MSGConst.SEND_ONLINE:{ // 用户上线
					User user = (User)pMsg.getAddObject();
					//添加用户
					addUser(user);
		            //向该客户端发送本机信息
		            netManage.sendToClient(MSGConst.ANS_ONLINE, SessionUtils.getLocalUserInfo(), pMsg.getSenderIMEI());
		            //向除该client的客户端发送该客户端信息
		            netManage.sendToAllExClient(MSGConst.ANS_ONLINE, user, pMsg.getSenderIMEI());
		            //如果本机准备了 发送准备消息
		            if(isMeReady){
						String lists = TypeUtils.cListToString(mServerReadyList);
			            netManage.sendToClient(MSGConst.ANS_READY, lists, pMsg.getSenderIMEI());
		            }
		            	
				}   
		            break;
		        case MSGConst.SEND_OFFLINE:{ // 用户下线
					String imei = pMsg.getSenderIMEI();
					//删除用户列表
					removeUser(imei);
					removeReady(imei);
		            //向除该client的客户端发送该客户端信息
		            netManage.sendToAllExClient(MSGConst.ANS_OFFLINE, imei, imei);
		        }
		            break;
		        case MSGConst.SEND_READY:{//收到准备请求
		        	String imei = pMsg.getSenderIMEI();
					addReady(imei);
					String lists = TypeUtils.cListToString(mServerReadyList);
		            netManage.sendToAllClient(MSGConst.ANS_READY, lists);
		        }
		            break;
		        case MSGConst.SEND_UNREADY:{//用户取消准备
		        	String imei = pMsg.getSenderIMEI();
					removeReady(imei);
					String lists = TypeUtils.cListToString(mServerReadyList);
		            netManage.sendToAllClient(MSGConst.ANS_UNREADY, lists);
		        }
		            break;
		        case MSGConst.SEND_START:{//收到客户端开始回应
		        	checkStart(pMsg.getSenderIMEI());
		        }
		        	break;
		        default:
		        	LogUtils.i(TAG, "wrong msg type");
		            break;
			    }
			}
		};
		//添加服务器消息处理回调
		netManage.addServerListener(serverListener);
	}
	
	private synchronized void checkStart(String imei){
    	mServerStartList.add(imei);
    	if(mServerStartList.size() == mServerReadyList.size()){
    		//删除该类的监听回调
    		netManage.removeServerListener(serverListener);
    		netManage.removeClientListener(clientListener);
    		Bundle b =new Bundle();
			b.putStringArrayList("order", mServerReadyList);
            BaseApplication.playNotification();
			startActivity(DrawGuessActivity.class, b);
    	}
    }
	

	private synchronized void addUser(User user){
		//添加用户列表
		mServerUsersMap.put(user.getIMEI(), user);
    }
    
	private synchronized void removeUser(String imei){
		mServerUsersMap.remove(imei);
    }
	
	private synchronized void addReady(String imei){
		//添加准备列表
		mServerReadyList.add(imei);
    }
    
	private synchronized void removeReady(String imei){
		//删除准备列表
		if(mServerReadyList.contains(imei))
			mServerReadyList.remove(imei);
    }
	
	private void setOrderList(List<String> lists){
		SessionUtils.setOrder(-1);
		Iterator<Entry<String, User>> iter = mLocalUsersMap.entrySet().iterator();
    	while (iter.hasNext()) {
    		Entry<String, User> entry = (Entry<String, User>) iter.next();
    		User u = (User) entry.getValue();
    		u.setOrder(-1);
		}
		int i = 0;
		if(!lists.isEmpty()){
			for(String s:lists){
				i++;
				if(s.equals(SessionUtils.getIMEI()))
					SessionUtils.setOrder(i);
				else{
					User user = mLocalUsersMap.get(s);
					user.setOrder(i);
				}
			}
		}
	}
	
	@Override
    protected void initViews() {
    	mIvAvatar = (ImageView) findViewById(R.id.gameroom_my_avatar);
        mName = (TextView) findViewById(R.id.gameroom_my_name);
        mPlayersNum = (TextView) findViewById(R.id.gameroom_tv_playersnumber);
        mOrder = (TextView) findViewById(R.id.gameroom_my_order);
        
        mIvGender = (ImageView) findViewById(R.id.gameroom_my_gender);
        mLayoutExGender = (LinearLayout) findViewById(R.id.gameroom_layout_gender);

        mBtnReady = (Button) findViewById(R.id.gameroom_ready);
        mBtnStart = (Button) findViewById(R.id.gameroom_start);
        mBtnCreate = (Button) findViewById(R.id.gameroom_create);
        
    	mListView = (MultiListView) findViewById(R.id.friends_list);
    	
    	Picasso.with(mContext).load(ImageUtils.getImageID(User.AVATAR + SessionUtils.getAvatar())).into(mIvAvatar);
    	mName.setText(SessionUtils.getNickname());
		mPlayersNum.setText(R.string.gameroom_emptyplayer);
		
    	if ("女".equals(SessionUtils.getAvatar())) {
        mIvGender.setBackgroundResource(R.drawable.ic_user_famale);
        mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_famal);
	    }
	    else {
	        mIvGender.setBackgroundResource(R.drawable.ic_user_male);
	        mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_male);
	    }
	}
	 
    @Override
    protected void initEvents() {
        mBtnReady.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
        mBtnCreate.setOnClickListener(this);
        mAdapter = new PlayersAdapter(this, mLocalUsersList);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        
    }
    
    /**
     * 将用户表HashMap转成ArrayList
     * 
     * @param application
     */
    private void initMapToList() {
        mLocalUsersList = new ArrayList<User>(mLocalUsersMap.size());
        for (Map.Entry<String, User> entry : mLocalUsersMap.entrySet()) {
            mLocalUsersList.add(entry.getValue());
        }
    }
    
    
    
    /**
	 * 主线程处理UI变化
	 */
    private MyHandler handler = new MyHandler();
	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			//只有客户端才更新UI
			switch (msg.what){
			case MSGConst.ANS_GAME_OVER:
	        case MSGConst.ANS_ONLINE:
	        case MSGConst.ANS_OFFLINE: 
	        case MSGConst.ANS_READY:
	        case MSGConst.ANS_UNREADY:
				//刷新UI
				initMapToList();
	            refreshAdapter();
	            refreshPlayersNum();
	            break;
    	 	default:
    	 		break;
            }
		}
	};

	
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       //Users people = mLocalUsersList.get((int) id);
    }

   
    
    
    /** 刷新用户在线列表UI **/
    private void refreshAdapter() {
        mAdapter.setData(mLocalUsersList); // Adapter加载List数据
        mAdapter.notifyDataSetChanged();
    }
    
    /**
     * 刷新UI中玩家数量
     */
    private void refreshPlayersNum(){
    	if(SessionUtils.getOrder()>-1)
    		mOrder.setText("顺序：" + SessionUtils.getOrder());
    	else
    		mOrder.setText("");
    	if(mLocalUsersList.isEmpty())
    		mPlayersNum.setText(R.string.gameroom_emptyplayer);
    	else
    	{
    		String sFormat = getResources().getString(R.string.gameroom_haveplayer); 
       		mPlayersNum.setText(sFormat + mLocalUsersList.size());
    	}
    }


}
