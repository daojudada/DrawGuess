package com.drawguess.activity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
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
import com.drawguess.msgbean.Users;
import com.drawguess.net.MSGConst;
import com.drawguess.net.MSGProtocol;
import com.drawguess.net.WifiNet;
import com.drawguess.net.WifiNet.RegisterIMEI;
import com.drawguess.net.WifiNet.SocketMode;
import com.drawguess.util.ImageUtils;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.view.MultiListView;
import com.drawguess.view.MultiListView.OnRefreshListener;
import com.esotericsoftware.kryonet.Connection;
import com.squareup.picasso.Picasso;

/**
 * 游戏房间，主要事件是寻找和建立服务器，准备以及开始
 * @author GuoJun
 *
 */
public class GameRoomActivity extends BaseActivity implements  OnItemClickListener, OnRefreshListener, OnClickListener {
	private static final String TAG = "GameRoomActivity";

    private boolean isMeReady;
    
    private PlayersAdapter mAdapter;
    private Button mBtnReady;
    
    private Button mBtnStart;
    private ImageView mIvAvatar;
    private ImageView mIvGender;
    private LinearLayout mLayoutExGender; // 性别根布局
    private MultiListView mListView;
    private TextView mName;
    private TextView mPlayersNum;
    private ArrayList<String> mReadyList; //已准备的用户列表
    private ArrayList<Users> mUsersList; // 在线用户列表，用于adapter
    private HashMap<String,Users> mUsersMap; // 在线用户列表
    
    private WifiNet net;

    private void connectServerTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
				return	net.connectServer();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
            	dismissLoadingDialog();
                if(result){
    	            LogUtils.i(TAG, "连接房间成功");
    	            //发送服务器告知上线
    	            RegisterIMEI ri = new RegisterIMEI();
    				ri.imei = SessionUtils.getIMEI();
    				net.sendToServer(ri,SocketMode.TCP);
    	            net.sendToServer(MSGConst.BR_ENTRY, SessionUtils.getLocalUserInfo(), SocketMode.TCP);
                	showShortToast("连接房间成功");
                }
                else{
                	showShortToast("连接房间失败");
    	            LogUtils.i(TAG, "连接房间失败");
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog("正在连接房间");
            }
        });
    }

    private void findServerTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
            	return net.findServer();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
            	dismissLoadingDialog();
                if(result){
    	            LogUtils.i(TAG, "找到房间");
    	            connectServerTask();
                }
                else{
    	            LogUtils.i(TAG, "没有找到房间");
                    showLoadingDialog("没找到房间，正在创建");
    	            try {
    					net.createServer();
    				} catch (IOException e) {
    					e.printStackTrace();
        	            LogUtils.i(TAG, "创建房间失败");
    				}
                	dismissLoadingDialog();
    	            LogUtils.i(TAG, "创建房间成功");
                	showShortToast("创建房间成功");
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog("正在寻找房间");
            }
        });
    }
    
    
    @Override
    protected void initEvents() {

        mBtnReady.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
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
    private void initMaptoList() {
        mUsersList = new ArrayList<Users>(mUsersMap.size());
        for (Map.Entry<String, Users> entry : mUsersMap.entrySet()) {
            mUsersList.add(entry.getValue());
        }
    }
    
    @Override
    protected void initViews() {
    	mIvAvatar = (ImageView) findViewById(R.id.gameroom_my_avatar);
        mName = (TextView) findViewById(R.id.gameroom_my_name);
        mPlayersNum = (TextView) findViewById(R.id.gameroom_tv_playersnumber);
        
        mIvGender = (ImageView) findViewById(R.id.gameroom_my_gender);
        mLayoutExGender = (LinearLayout) findViewById(R.id.gameroom_layout_gender);

        mBtnReady = (Button) findViewById(R.id.gameroom_ready);
        mBtnStart = (Button) findViewById(R.id.gameroom_start);
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
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.gameroom_ready:
        	if(!isMeReady)
        	{
        		mBtnReady.setBackgroundResource(R.drawable.btn_red_default);
        		mBtnReady.setText(R.string.gameroom_order);
        		isMeReady = true;
        	}
        	else
        	{
        		mBtnReady.setBackgroundResource(R.drawable.btn_bottombar_normal);
        		mBtnReady.setText(R.string.gameroom_ready);
        		isMeReady = false;
        	}
            break;

        case R.id.gameroom_start:
        	startActivity(DrawTabActivity.class);
        	finish();
            break;
		}
	}

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        mUsersList = new ArrayList<Users>();
        mUsersMap = new HashMap<String,Users>();
        mReadyList = new ArrayList<String>();
        
        isMeReady = false;
        initViews();
        initEvents();
        
        setNetListener();
        net.createClient();
        findServerTask();
    }


	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
       //Users people = mUsersList.get((int) id);
        
    }

    @Override
    public void onRefresh() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //mUDPListener.refreshUsers();
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mListView.onRefreshComplete();

            }
        });
    }
    
    
    /** 刷新用户在线列表UI **/
    private void refreshAdapter() {
        mAdapter.setData(mUsersList); // Adapter加载List数据
        mAdapter.notifyDataSetChanged();
    }
    
    /**
     * 刷新UI中玩家数量
     */
    private void refreshPlayersNum()
    {
    	if(mUsersList.isEmpty())
    		mPlayersNum.setText(R.string.gameroom_emptyplayer);
    	else
    	{
    		String sFormat = getResources().getString(R.string.gameroom_haveplayer); 
       		mPlayersNum.setText(sFormat + mUsersList.size());
    	}
    }


    /**
     * 重写Net中IPMSG消息类型的处理接口
     */
    private void setNetListener(){
    	net = new WifiNet(
			new com.drawguess.interfaces.MSGListener(){
				@Override
				public void handleIPMSG(Connection conneciton, MSGProtocol ipmsg) {
					switch (ipmsg.getCommandNo()) {
					case MSGConst.BR_ENTRY:{ // 用户上线
						Users user = (Users)ipmsg.getAddObject();
						//添加用户列表
						mUsersMap.put(user.getIMEI(), user);
						//刷新UI
						initMaptoList();
			            refreshAdapter();
			            refreshPlayersNum();
					}
						break;
			        case MSGConst.BR_EXIT: {// 用户下线
						Users user = (Users)ipmsg.getAddObject();
						//删除用户列表
						mUsersMap.remove(user.getIMEI());
						//刷新UI
			        	initMaptoList();
			            refreshAdapter();
			            refreshPlayersNum();
			        }
			            break;
			        case MSGConst.SENDREADY://收到准备请求
			        case MSGConst.ANSREADY:{//未准备请求
						Users user = (Users)ipmsg.getAddObject();
						String imei = user.getIMEI();
						mReadyList.add(imei);
						Users local = mUsersMap.get(imei);
						local.setOrder(user.getOrder());
						if(imei == SessionUtils.getIMEI())
							SessionUtils.setOrder(user.getOrder());
						//刷新UI
			        	initMaptoList();
			            refreshAdapter();
			        }
			            break;
			        case MSGConst.SENDSTART:{//收到开始请求
			        	
			        }
			        	break;
			        default:
			        	LogUtils.i(TAG, "wrong msg type");
			            break;
				    }
		    	}
			},
			new com.drawguess.interfaces.MSGListener(){
				@Override
				public void handleIPMSG(Connection conneciton, MSGProtocol ipmsg) {
					switch (ipmsg.getCommandNo()) {
					case MSGConst.BR_ENTRY:{ // 用户上线
						Users user = (Users)ipmsg.getAddObject();
						//添加用户列表
						mUsersMap.put(user.getIMEI(), user);
						//刷新UI
						initMaptoList();
			            refreshAdapter();
			            refreshPlayersNum();
			            //向除该client的客户端发送该客户端信息
			            net.sendToAllExClient(MSGConst.BR_ENTRY, user, conneciton.getID(), SocketMode.TCP);
			            //向该客户端发送服务器信息
			            net.sendToClient(MSGConst.BR_ENTRY, SessionUtils.getLocalUserInfo(), conneciton.getID(), SocketMode.TCP);
					}   
			            break;
			        case MSGConst.BR_EXIT:{ // 用户下线
						Users user = (Users)ipmsg.getAddObject();
						//删除用户列表
						mUsersMap.remove(user.getIMEI());
						//刷新UI
			        	initMaptoList();
			            refreshAdapter();
			            refreshPlayersNum();
			            //向除该client的客户端发送该客户端信息
			            net.sendToAllExClient(MSGConst.BR_EXIT, user, conneciton.getID(), SocketMode.TCP);
			        }
			            break;
			        case MSGConst.SENDREADY:{//收到准备请求
			        	String imei = ipmsg.getAddStr();
						Users user = mUsersMap.get(imei);
						mReadyList.add(imei);
						user.setOrder(mReadyList.size());
			            net.sendToAllClient(MSGConst.SENDREADY, user, SocketMode.TCP);
						//刷新UI
			        	initMaptoList();
			            refreshAdapter();
			        }
			            break;
			        case MSGConst.ANSREADY:{//收到未准备请求
			        	String imei = ipmsg.getAddStr();
						Users user = mUsersMap.get(imei);
						mReadyList.add(imei);
						user.setOrder(-1);
			            net.sendToAllClient(MSGConst.SENDREADY, user, SocketMode.TCP);
						//刷新UI
			        	initMaptoList();
			            refreshAdapter();
			        }
			            break;
			        case MSGConst.SENDSTART:{//发送开始请求
			        	
			        }
			        	break;
			        case MSGConst.ANSSTART://收到开始回应
			        	
			        	break;
			        default:
			        	LogUtils.i(TAG, "wrong msg type");
			            break;
				    }
		    	}
			}
		);
    }

}
