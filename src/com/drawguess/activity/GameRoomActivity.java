package com.drawguess.activity;


import java.util.ArrayList;
import java.util.Collections;
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
import com.drawguess.interfaces.MSGListener;
import com.drawguess.msgbean.Users;
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
    private ArrayList<String> mReadyList; //已准备的用户列表
    private ArrayList<String> mStartList; //已准备的用户列表
    private HashMap<String,Users> mUsersMap; // 在线用户列表
    private ArrayList<Users> mUsersList; // 临时在线用户列表，用于adapter初始化
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        
        mUsersMap = new HashMap<String,Users>();
        mReadyList =  new ArrayList<String>();
        mStartList = new ArrayList<String>();
        
        isMeReady = false;
        SessionUtils.setOrder(-1);
        
        haveFind = false;
        
        initViews();
        initEvents();
        
        //获取单例
        netManage = NetManage.getInstance(this);
        netManage.setSocketMode(SocketMode.TCP);
        //创建UDP广播线程
		netManage.createUDP();
		//寻找服务器
        findServerTask();
    }

    
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gameroom_create:
			createServer();
        	showShortToast("创建房间成功");
        	haveFind = true;
            LogUtils.i(TAG, "创建房间成功");
            break;
        case R.id.gameroom_ready:
        	if(!isMeReady)
        	{
        		//用户准备
        		mBtnReady.setBackgroundResource(R.drawable.btn_red_default);
        		mBtnReady.setText(R.string.gameroom_order);
        		if(NetManage.getState() == 2){
					addReady(SessionUtils.getIMEI());
					String lists = TypeUtils.cListToString(mReadyList);
        			netManage.sendToAllClient(MSGConst.ANS_READY, lists);
        		}
        		else if(NetManage.getState() == 1)
        			netManage.sendToServer(MSGConst.SEND_READY, null);
        		isMeReady = true;
        	}
        	else
        	{
        		//用户取消准备
        		mBtnReady.setBackgroundResource(R.drawable.btn_bottombar_normal);
        		mBtnReady.setText(R.string.gameroom_ready);
        		if(NetManage.getState() == 2){
            		removeReady(SessionUtils.getIMEI());
					String lists = TypeUtils.cListToString(mReadyList);
        			netManage.sendToAllClient(MSGConst.ANS_UNREADY, lists);
        		}
        		else if(NetManage.getState() == 1)
        			netManage.sendToServer(MSGConst.SEND_UNREADY, null);
        		isMeReady = false;
        	}
        	//刷新UI
        	refreshAdapter();
        	refreshPlayersNum();
            break;

        case R.id.gameroom_start:
        	mStartList.add(SessionUtils.getIMEI());
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
    	            createClient();
    	            haveFind = true;
                	showShortToast("连接房间成功");
                }
                else{
    	            LogUtils.i(TAG, "没有找到房间");
                	showShortToast("没有找到房间");
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
    	            createClient();
                	showShortToast("连接房间成功");
                }
                else{
    	            LogUtils.i(TAG, "没有找到房间");
                	showShortToast("没有找到房间");
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog("正在寻找房间");
            }
        });
    }
	
	
	private void createClient(){
        //显示按钮
		mBtnCreate.setVisibility(View.GONE);
		mBtnReady.setVisibility(View.VISIBLE);
        //创建客户端
    	netManage.createClient();
    	clientListener = new MSGListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
		        switch (command) {
				case MSGConst.ANS_ONLINE:{ //服务器向客户端通报其他在线用户
					Users user = (Users)pMsg.getAddObject();
					//添加用户列表
					mUsersMap.put(user.getIMEI(), user);
				}
					break;
		        case MSGConst.ANS_OFFLINE: {//服务器向客户端通报其他下线用户
					//删除用户列表
					mUsersMap.remove(pMsg.getSenderIMEI());
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
		        	netManage.sendToServer(MSGConst.SEND_START, null);
		        }
		        	break;
		        	
		        case MSGConst.ANS_RESTART:{
		    		netManage.removeClientListener(clientListener);
		        	startActivity(DrawGuessActivity.class);
		        }
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
		netManage.connectServer();
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
		serverListener = new MSGListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
				switch (command) {
				case MSGConst.SEND_ONLINE:{ // 用户上线
					Users user = (Users)pMsg.getAddObject();
					//添加用户
					addUser(user);
		            //向该客户端发送服务器信息
		            netManage.sendToClient(MSGConst.ANS_ONLINE, SessionUtils.getLocalUserInfo(), pMsg.getSenderIMEI());
		            //向除该client的客户端发送该客户端信息
		            netManage.sendToAllExClient(MSGConst.ANS_ONLINE, user, pMsg.getSenderIMEI());
				}   
		            break;
		        case MSGConst.SEND_OFFLINE:{ // 用户下线
					Users user = (Users)pMsg.getAddObject();
					//删除用户列表
					removeUser(user);
		            //向除该client的客户端发送该客户端信息
		            netManage.sendToAllExClient(MSGConst.ANS_OFFLINE, null, pMsg.getSenderIMEI());
		        }
		            break;
		        case MSGConst.SEND_READY:{//收到准备请求
		        	String imei = pMsg.getSenderIMEI();
					addReady(imei);
					String lists = TypeUtils.cListToString(mReadyList);
		            netManage.sendToAllClient(MSGConst.ANS_READY, lists);
		        }
		            break;
		        case MSGConst.SEND_UNREADY:{//用户取消准备
		        	String imei = pMsg.getSenderIMEI();
					removeReady(imei);
					String lists = TypeUtils.cListToString(mReadyList);
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
		        //发送给主线程
		        msg.what = command;
		        msg.setData(b);
				handler.sendMessage(msg);
			}
		};
		//添加服务器消息处理回调
		netManage.addServerListener(serverListener);
	}
	
	private synchronized void checkStart(String imei){
    	mStartList.add(imei);
    	if(mStartList.size() == mReadyList.size()){
    		//确定开始
    		netManage.sendToAllClient(MSGConst.ANS_RESTART, null);
    		//删除该类的监听回调
    		netManage.removeServerListener(serverListener);
    		//停止监听UDP寻找房间的广播
    		netManage.stopUdp();
    		Bundle b =new Bundle();
			b.putStringArrayList("order", mReadyList);
			startActivity(DrawGuessActivity.class, b);
			
    	}
    }
	

	private void addReady(String imei){
		//添加准备列表
		mReadyList.add(imei);
		setOrderList(mReadyList);
    }
    
	private void removeReady(String imei){
		//删除准备列表
		mReadyList.remove(imei);
		setOrderList(mReadyList);
    }
	
	private void setOrderList(List<String> lists){
		SessionUtils.setOrder(-1);
		Iterator<Entry<String, Users>> iter = mUsersMap.entrySet().iterator();
    	while (iter.hasNext()) {
    		Entry<String, Users> entry = (Entry<String, Users>) iter.next();
    		Users u = (Users) entry.getValue();
    		u.setOrder(-1);
		}
		int i = 0;
		if(!lists.isEmpty()){
			for(String s:lists){
				i++;
				if(s.equals(SessionUtils.getIMEI()))
					SessionUtils.setOrder(i);
				else{
					Users user = mUsersMap.get(s);
					user.setOrder(i);
				}
			}
		}
	}
	
	private synchronized void addUser(Users user){
		//添加用户列表
		mUsersMap.put(user.getIMEI(), user);
    }
    
	private synchronized void removeUser(Users user){
		mUsersMap.remove(user.getIMEI());
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
    	
    	Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + SessionUtils.getAvatar())).into(mIvAvatar);
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
        mAdapter = new PlayersAdapter(this, mUsersList);
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
        mUsersList = new ArrayList<Users>(mUsersMap.size());
        for (Map.Entry<String, Users> entry : mUsersMap.entrySet()) {
            mUsersList.add(entry.getValue());
        }
    }
    
    
    
    /**
	 * 主线程处理UI变化
	 */
    private MyHandler handler = new MyHandler();
	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
			case MSGConst.SEND_ONLINE:
	        case MSGConst.SEND_OFFLINE:
	        case MSGConst.SEND_READY:
	        case MSGConst.SEND_UNREADY:
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
       //Users people = mUsersList.get((int) id);
    }

   
    
    
    /** 刷新用户在线列表UI **/
    private void refreshAdapter() {
        mAdapter.setData(mUsersList); // Adapter加载List数据
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
    	if(mUsersList.isEmpty())
    		mPlayersNum.setText(R.string.gameroom_emptyplayer);
    	else
    	{
    		String sFormat = getResources().getString(R.string.gameroom_haveplayer); 
       		mPlayersNum.setText(sFormat + mUsersList.size());
    	}
    }


}
