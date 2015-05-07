package com.drawguess.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.drawguess.R;
import com.drawguess.adapter.MsgsAdapter;
import com.drawguess.adapter.ScoresAdapter;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.Constant;
import com.drawguess.bluetooth.BluetoothService;
import com.drawguess.dialog.ColorDialog;
import com.drawguess.dialog.PaintDialog;
import com.drawguess.dialog.ShapeDialog;
import com.drawguess.drawop.OpDraw.Shape;
import com.drawguess.interfaces.OnColorChangedListener;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.interfaces.OnPaintChangedListener;
import com.drawguess.interfaces.OnShapeChangedListener;
import com.drawguess.msgbean.DataDraw;
import com.drawguess.msgbean.DataGuess;
import com.drawguess.msgbean.DataDraw.OP_TYPE;
import com.drawguess.msgbean.DataDraw.TOUCH_TYPE;
import com.drawguess.msgbean.User;
import com.drawguess.net.MSGConst;
import com.drawguess.net.MSGProtocol;
import com.drawguess.net.NetManage;
import com.drawguess.sql.DBOperate;
import com.drawguess.sql.WordInfo;
import com.drawguess.util.DataUtils;
import com.drawguess.util.EncryptUtils;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TimerUtils;
import com.drawguess.util.TypeUtils;
import com.drawguess.view.DrawView;

public class BtDrawGuessActivity extends BaseActivity implements OnClickListener{
	
	private static final String TAG = "DrawTabActivity";
	
	/**
	 * 对方的蓝牙地址
	 */
	private String DEVICE_ADDRESS;
	
	/*
	 * 蓝牙连接服务类对象
	 */
	private BluetoothService mBtService;
	
	/*
	 * 蓝牙适配器对象
	 */
	private BluetoothAdapter mBtAdapter;
	
	/*
	 * 
	 */
	private boolean isMeDraw;
	
	/**
     * 延时退出时间变量
     */
    private long ExitTime; 
    /**
     * 消息传递类
     */
    public MyHandler handler = new MyHandler();
    /**
     * 绘图状态
     */
    private Boolean isPack,isEraser,isTrans;
	private int[] colorSource = new int[]{
    		R.drawable.btn_black1,R.drawable.btn_red1,R.drawable.btn_blue1,R.drawable.btn_green1,
    		R.drawable.btn_yellow1,R.drawable.btn_purple1,R.drawable.btn_ching1,
    		R.drawable.btn_black2,R.drawable.btn_red2,R.drawable.btn_blue2,R.drawable.btn_green2,
    		R.drawable.btn_yellow2,R.drawable.btn_purple2,R.drawable.btn_ching2
    };
    /**
     * 绘图显示类
     */
    private DrawView mDrawView;
    private ImageButton[] mBtColors = new ImageButton[7];
    private LinearLayout mDrawTab,mChatTab,mResultTab;
    private ImageButton mIbErase,mIbColor,mIbPaint,mIbFill,mIbShape,mIbRedo,mIbUndo,mIbMenu,mIbEdit,mIbDelete,mIbCopy;
    private LinearLayout mLayoutChat,mLayoutResult;
    private LinearLayout mLayoutDraw;
    private LinearLayout mLayoutChatEdit;
    private LinearLayout mLayoutColorBar,mLayoutBtnBar;
    private LinearLayout mLayoutTime;
    private View mVDraw,mVChat,mVResult;
    private ImageView mIvArrow;
    private Button mBtSend;
    private Button mBtTip;
    private EditText mEtEdit;
    private TextView mTvTime;
    private TextView mTvDraw;
    private ListView mLvScore;
    private ListView mLvMsg;
    
    public TextView mDebug;
    public static int logNum = 0;
    private String saveStr="";
    private ScoresAdapter scoresAdapter;
    private MsgsAdapter msgsAdapter;
    private TimerUtils mLocalTimerCheck;
    
    
    /**
     * 本地保存的玩家表
     */
    private static HashMap<String,User> mLocalPlayersMap;
    /**
     * 本地保存的消息表
     */
    private ArrayList<DataGuess> mLocalMsgsList;
    /**
     * 本地保存的分数临时表
     */
    private ArrayList<User> mLocalScoresList;
    
    private String mLocalWord,mLocalWord1,mLocalWord2;
    private String mLocalKind,mLocalKind1,mLocalKind2;
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btdrawguess);
        
        
        isPack = false;
		isEraser = false;
		isTrans = false;
		
        initViews();
        initEvents();

        mBtService = BluetoothService.getInstance(handler);
        mDrawView.setBtService(mBtService);
        
        Bundle bundle = this.getIntent().getExtras();
        isMeDraw = bundle.getBoolean("isMeDraw");

        //如果自己是链表第一个
		if(isMeDraw){
			SessionUtils.setOrder(1);
			mLayoutChatEdit.setVisibility(View.GONE);
			mChatTab.setVisibility(View.VISIBLE);
	    	mLayoutColorBar.setVisibility(View.VISIBLE);
	    	mLayoutBtnBar.setVisibility(View.VISIBLE);
	    	
			DBOperate db = new DBOperate(this);
			WordInfo w1 = db.getRandomWord(System.currentTimeMillis());
			String word1 = new String(EncryptUtils.decrypt(w1.getWord(), Constant.PASSWORD));
			String kind1 = new String(EncryptUtils.decrypt(w1.getKind(), Constant.PASSWORD));
			WordInfo w2 = db.getRandomWord(System.currentTimeMillis());
			String word2 = new String(EncryptUtils.decrypt(w2.getWord(), Constant.PASSWORD));
			String kind2 = new String(EncryptUtils.decrypt(w2.getKind(), Constant.PASSWORD));
			mLocalWord1 = word1;
			mLocalWord2 = word2;
			mLocalKind1 = kind1;
			mLocalKind2 = kind2;
			db.close();
			//显示选词对话框
    		DialogInterface.OnClickListener firstListener = new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mLocalWord = mLocalWord1;
					mLocalKind = mLocalKind1;
					handler.sendEmptyMessage(MSGConst.SET_TEXT);
					
					mBtService.sendMessage(MSGConst.SEND_CHOOSED, null);
				}
			};
    		DialogInterface.OnClickListener secondListener = new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mLocalWord = mLocalWord2;
					mLocalKind = mLocalKind2;
					handler.sendEmptyMessage(MSGConst.SET_TEXT);
					
					mBtService.sendMessage(MSGConst.SEND_CHOOSED, null);
				}
			};
    		showAlertDialog(
    				"选择词汇","请从下面两个词语中选择一个你要画的词汇吧",
    				mLocalWord1, firstListener,
    				mLocalWord2, secondListener);
		}
		else{
			SessionUtils.setOrder(2);
			mLayoutChatEdit.setVisibility(View.VISIBLE);
			mChatTab.setVisibility(View.GONE);
	    	mLayoutColorBar.setVisibility(View.GONE);
	    	mLayoutBtnBar.setVisibility(View.GONE);
	    	showLoadingDialog("等待绘图者选词噢");
		}

		//退出上个计时进程
		if(mLocalTimerCheck != null)
			mLocalTimerCheck.exit();
		//设定游戏时间
		mLocalTimerCheck = new TimerUtils() {
            @Override
            public void doTimeOutWork() {
            	mBtService.sendMessage(MSGConst.SEND_START, null);
            }

            @Override
            public void doTimerCheckWork() {
            	handler.sendEmptyMessage(MSGConst.TIME_CHECK);
            }
        };
        mLocalTimerCheck.start(Constant.GAME_TIME, 1000);
		//清空画布
		doClickEvent(OP_TYPE.CLEAR, -1, -1, -1, -1);
		

		//设置消息表
		mLocalMsgsList = new ArrayList<DataGuess>();
		msgsAdapter = new MsgsAdapter(BtDrawGuessActivity.this, mLocalMsgsList);
		mLvMsg.setAdapter(msgsAdapter);
    	//设置积分表格
		scoresAdapter = new ScoresAdapter(BtDrawGuessActivity.this, mLocalScoresList);
		mLvScore.setAdapter(scoresAdapter);
        scoresAdapter.notifyDataSetChanged();
        
		
		mLocalPlayersMap.put(SessionUtils.getIMEI(), SessionUtils.getLocalUserInfo());
		mBtService.sendMessage(MSGConst.SEND_ONLINE, SessionUtils.getLocalUserInfo());
    }
    

	@Override
    protected void onDestroy() {
		mBtService.stop();
        handler = null;
        super.onDestroy();

    }
	
	@Override
	protected void initEvents() {
		for(int i=0;i<7;i++){
			mBtColors[i].setOnClickListener(this);
		}
		mLvMsg.setStackFromBottom(true);
		mLvMsg.setFastScrollEnabled(true);
        
		mDrawTab.setOnClickListener(this);
		mChatTab.setOnClickListener(this);
		mResultTab.setOnClickListener(this);
		mIbErase.setOnClickListener(this);
		mIbColor.setOnClickListener(this);
		mIbPaint.setOnClickListener(this);
		mIbFill.setOnClickListener(this);
		mIbShape.setOnClickListener(this);
		mIbRedo.setOnClickListener(this);
		mIbUndo.setOnClickListener(this);
		mIbMenu.setOnClickListener(this);
		mIbEdit.setOnClickListener(this);
		mIbDelete.setOnClickListener(this);
		mIbCopy.setOnClickListener(this);
		mBtSend.setOnClickListener(this);
		mBtTip.setOnClickListener(this);

		mLayoutTime.setOnClickListener(this);
		mIvArrow.setOnClickListener(this);
		
		mEtEdit.setOnEditorActionListener(new OnEditorActionListener(){
			@Override       
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {  
				if (arg1 == EditorInfo.IME_ACTION_SEND) {      
					DataGuess dg = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), mEtEdit.getText().toString());
					mBtService.sendMessage(MSGConst.SEND_GUESS_WORD, dg);
				}         
				return false;       
			}  
		});
		
		
		mLocalPlayersMap = new HashMap<String, User>();
		
		// Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
		// If BT is not on, request that it be enabled.  
        // setupChat() will then be called during onActivityResult  
        if (!mBtAdapter.isEnabled()) {  
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);  
        }
        
        EditText v = (EditText)findViewById(R.id.drawtab_chat_editer);
        v.setText(DEVICE_ADDRESS);
	}
	
	@Override
    protected void initViews() {
		mDebug = (TextView)findViewById(R.id.debug_test);
		
		mDrawView = (DrawView)findViewById(R.id.drawview);
    	mLayoutDraw = (LinearLayout)findViewById(R.id.drawlayout);
    	mLayoutChat = (LinearLayout)findViewById(R.id.chatlayout);
    	mLayoutResult = (LinearLayout)findViewById(R.id.resultlayout);
    	
    	mLayoutChat = (LinearLayout)findViewById(R.id.chatlayout);
    	mLayoutResult = (LinearLayout)findViewById(R.id.resultlayout);
    	
    	mLayoutColorBar = (LinearLayout)findViewById(R.id.drawtab_colorbar);
    	mLayoutBtnBar = (LinearLayout)findViewById(R.id.drawtab_btnbar);
    	
    	mLayoutChatEdit = (LinearLayout)findViewById(R.id.edit_text_layout);
    	
    	mLvScore = (ListView)findViewById(R.id.scores_list);
    	mLvMsg = (ListView)findViewById(R.id.chat_list);
    	
		mVDraw = findViewById(R.id.drawtabview);
		mVChat = findViewById(R.id.chattabview);
		mVResult = findViewById(R.id.resulttabview);
		
		mDrawTab = (LinearLayout)findViewById(R.id.drawtab);
		mChatTab = (LinearLayout)findViewById(R.id.chattab);
		mResultTab = (LinearLayout)findViewById(R.id.resulttab);

	    mLayoutTime = (LinearLayout)findViewById(R.id.drawtab_layout_time);
	    mIvArrow = (ImageView)findViewById(R.id.drawtab_iv_arrow);
	    mTvTime = (TextView)findViewById(R.id.drawtab_tv_time);
	    mTvDraw = (TextView)findViewById(R.id.drawtab_tv_draw);
	    
		mBtColors[0] = (ImageButton)findViewById(R.id.black);
		mBtColors[1] = (ImageButton)findViewById(R.id.red);
		mBtColors[2] = (ImageButton)findViewById(R.id.blue);
		mBtColors[3] = (ImageButton)findViewById(R.id.green);
		mBtColors[4] = (ImageButton)findViewById(R.id.yellow);
		mBtColors[5] = (ImageButton)findViewById(R.id.purple);
		mBtColors[6] = (ImageButton)findViewById(R.id.ching);
		
		mIbErase = (ImageButton)findViewById(R.id.erase);
		mIbColor = (ImageButton)findViewById(R.id.colorpick);
		mIbPaint = (ImageButton)findViewById(R.id.paint);
		mIbFill  = (ImageButton)findViewById(R.id.fill);
		mIbShape = (ImageButton)findViewById(R.id.shape);
		mIbRedo  = (ImageButton)findViewById(R.id.redo);
		mIbUndo  = (ImageButton)findViewById(R.id.undo);
		mIbMenu  = (ImageButton)findViewById(R.id.menu);
		mIbEdit  = (ImageButton)findViewById(R.id.edit);
		mIbDelete= (ImageButton)findViewById(R.id.delete);
		mIbCopy  = (ImageButton)findViewById(R.id.copy);

		mBtTip = (Button)findViewById(R.id.drawtab_bt_tip);
	    mBtSend = (Button)findViewById(R.id.drawtab_chat_send);
	    mEtEdit = (EditText)findViewById(R.id.drawtab_chat_editer);


		if(isMeDraw){
			mLayoutChatEdit.setVisibility(View.GONE);
			mChatTab.setVisibility(View.VISIBLE);
	    	mLayoutColorBar.setVisibility(View.VISIBLE);
	    	mLayoutBtnBar.setVisibility(View.VISIBLE);
		}
		else{
			mLayoutChatEdit.setVisibility(View.VISIBLE);
			mChatTab.setVisibility(View.GONE);
	    	mLayoutColorBar.setVisibility(View.GONE);
	    	mLayoutBtnBar.setVisibility(View.GONE);
		}
		
    }
	
	@Override
	public void onClick(View v) {
		int m;
		DataDraw data;
		switch (v.getId()) {
		case R.id.drawtab_bt_tip:
			mBtService.sendMessage(MSGConst.SEND_TIP, mLocalKind);
			break;
		case R.id.drawtab_chat_send:
			if(!mEtEdit.getText().toString().equals("")){
				DataGuess dg = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), mEtEdit.getText().toString());
				mEtEdit.setText("");
				View view = getWindow().peekDecorView();
			    if (view != null) {
			        InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			        inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
			    }
			    mBtService.sendMessage(MSGConst.SEND_GUESS_WORD, dg);
			}
			break;
		case R.id.drawtab:
	    	mLayoutDraw.setVisibility(View.VISIBLE);
	    	mLayoutChat.setVisibility(View.GONE);
	    	mLayoutResult.setVisibility(View.GONE);
			mVDraw.setVisibility(View.VISIBLE);
			mVChat.setVisibility(View.INVISIBLE);
			mVResult.setVisibility(View.INVISIBLE);
			break;
		case R.id.chattab:
	    	mLayoutDraw.setVisibility(View.GONE);
	    	mLayoutChat.setVisibility(View.VISIBLE);
	    	mLayoutResult.setVisibility(View.GONE);
			mVDraw.setVisibility(View.INVISIBLE);
			mVChat.setVisibility(View.VISIBLE);
			mVResult.setVisibility(View.INVISIBLE);
			break;
		case R.id.resulttab:
	    	mLayoutDraw.setVisibility(View.GONE);
	    	mLayoutChat.setVisibility(View.GONE);
	    	mLayoutResult.setVisibility(View.VISIBLE);
			mVDraw.setVisibility(View.INVISIBLE);
			mVChat.setVisibility(View.INVISIBLE);
			mVResult.setVisibility(View.VISIBLE);
			break;
		case R.id.black:
			m=0;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF000000);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF000000);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.red:
			m=1;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFF0000);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFF0000);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.blue:
			m=2;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF0000FF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF0000FF);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.green:
			m=3;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF00FF00);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF00FF00);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.yellow:
			m=4;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFFFF00);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFFFF00);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.purple:
			m=5;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFF00FF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFF00FF);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.ching:
			m=6;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF00FFFF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF00FFFF);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.shape:
			//形状选择,点击后对话框
			new ShapeDialog(this, 
				new OnShapeChangedListener() {
					@Override
					public void shapeChanged(int shape) {
						doClickEvent(OP_TYPE.SHAPE, shape, -1, -1, -1);
						DataDraw data  = new DataDraw(OP_TYPE.SHAPE, TOUCH_TYPE.DEFAULT, shape, -1, -1, -1);
						mBtService.sendMessage(MSGConst.SEND_DRAW, data);
					}
				}
			);
			break;
		case R.id.fill:
			doClickEvent(OP_TYPE.PACK, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.PACK, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.erase:
			doClickEvent(OP_TYPE.ERASE, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.ERASE, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.copy:
			doClickEvent(OP_TYPE.COPY, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.COPY, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.delete:
			doClickEvent(OP_TYPE.DELETE, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.DELETE, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.colorpick:
			//颜色设置，对话框
			new ColorDialog(this, mDrawView.getPaintColor(), "color", 
				new OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						doClickEvent(OP_TYPE.PAINT, -1, -1, -1, color);
						DataDraw data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, color);
						mBtService.sendMessage(MSGConst.SEND_DRAW, data);
						setColorBack(-1);
					}
				}
			).show();
			break;
		case R.id.edit:
			doClickEvent(OP_TYPE.EDIT, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.EDIT, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.paint:
			//画笔设置，对话框
			new PaintDialog(this, mDrawView.getPaintWidth(), mDrawView.getPaintAlpha(), mDrawView.getPaintStyle(),mDrawView.getPaintColor(),
					new OnPaintChangedListener(){
						@Override
						public void paintChanged(int width, int alpha, int style) {
							doClickEvent(OP_TYPE.PAINT, width, alpha, style, Float.MAX_VALUE);
							DataDraw data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, width, alpha, style, Float.MAX_VALUE);
							mBtService.sendMessage(MSGConst.SEND_DRAW, data);
						}
				}
			).show();
			break;
		case R.id.undo:
			doClickEvent(OP_TYPE.UNDO, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.UNDO, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.menu:
			//菜单
			openOptionsMenu();
			break;
		case R.id.redo:
			doClickEvent(OP_TYPE.REDO, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.REDO, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case R.id.drawtab_iv_arrow:
			if(mLayoutTime.getVisibility() == View.INVISIBLE){
				mLayoutTime.setVisibility(View.VISIBLE);
				mIvArrow.setBackgroundResource(R.drawable.bg_arrow_in);
			}
			else{
				mLayoutTime.setVisibility(View.INVISIBLE);
				mIvArrow.setBackgroundResource(R.drawable.bg_arrow_out);
			}
			break;
		case R.id.drawtab_layout_time:
			mLayoutTime.setVisibility(View.INVISIBLE);
			mIvArrow.setBackgroundResource(R.drawable.bg_arrow_out);
			break;
		default:
			break;
		}
		
		
	}
	
	public void doClickEvent(OP_TYPE opType, float data1, float data2, float data3, float data4){
    	switch (opType){
    	case SHAPE:
    		switch((int)data1)
			{
			case 0:
				mDrawView.setShape(Shape.FREE);
				mIbShape.setBackgroundResource(R.drawable.btn_free);
				break;
			case 1:
				mDrawView.setShape(Shape.LINE);
				mIbShape.setBackgroundResource(R.drawable.btn_line1);
				break;
			case 2:
				mDrawView.setShape(Shape.RECT);
				mIbShape.setBackgroundResource(R.drawable.btn_rect1);
				break;
			case 3:
				mDrawView.setShape(Shape.OVAL);
				mIbShape.setBackgroundResource(R.drawable.btn_oval1);
				break;
			}
    		break;
    	case PACK:
			mDrawView.setPack();
    		if(isPack){
				isPack=false;
				mIbFill.setBackgroundResource(R.drawable.btn_fill1);
			}
			else{
				isPack=true;
				mIbFill.setBackgroundResource(R.drawable.btn_fill2);
				if(isEraser){
					isEraser=false;
					mDrawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				}
				if(isTrans){
					isTrans=mDrawView.setTrans();
					mIbEdit.setBackgroundResource(R.drawable.btn_edit1);
					mIbDelete.setVisibility(View.GONE);
					mIbCopy.setVisibility(View.GONE);
					mIbErase.setVisibility(View.VISIBLE);
					mIbColor.setVisibility(View.VISIBLE);
					for(int i=0;i<7;i++){
						mBtColors[i].setVisibility(View.VISIBLE);
					}
				}
			}
    		break;
    	case EDIT:
    		//几何变换
			isTrans=mDrawView.setTrans();
			if(isTrans){
				mIbEdit.setBackgroundResource(R.drawable.btn_edit2);
				if(isEraser){
					isEraser=false;
					mDrawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				}
				if(isPack){
					mDrawView.setPack();
					isPack=false;
					mIbFill.setBackgroundResource(R.drawable.btn_fill1);
				}
				
				mIbErase.setVisibility(View.GONE);
				mIbColor.setVisibility(View.GONE);
				mIbDelete.setVisibility(View.VISIBLE);
				mIbCopy.setVisibility(View.VISIBLE);
				for(int i=0;i<7;i++){
					mBtColors[i].setVisibility(View.INVISIBLE);
				}
			}
			else{
				mIbEdit.setBackgroundResource(R.drawable.btn_edit1);
				mIbDelete.setVisibility(View.GONE);
				mIbCopy.setVisibility(View.GONE);
				mIbErase.setVisibility(View.VISIBLE);
				mIbColor.setVisibility(View.VISIBLE);
				for(int i=0;i<7;i++){
					mBtColors[i].setVisibility(View.VISIBLE);
				}
			}
    		break;
    	case ERASE:
			//橡皮擦
			if(isEraser){	
				isEraser=false;
				mDrawView.setEraser(isEraser);
				mIbErase.setBackgroundResource(R.drawable.btn_erase1);
			}
			else{
				isEraser=true;
				mDrawView.setEraser(isEraser);
				mIbErase.setBackgroundResource(R.drawable.btn_erase2);
				
				if(isPack){
					mDrawView.setPack();
					isPack=false;
					mIbFill.setBackgroundResource(R.drawable.btn_fill1);
				}
			}
    		break;
    	case COPY:
    		//复制
			mDrawView.setCopy();
    		break;
    	case DELETE:
    		//删除
			mDrawView.setDelete();
    		break;
    	case CLEAR:
    		//清空
    		logNum = 0;
			mDrawView.setClear();
    		break;
    	case REDO:
			//取消撤销
			mDrawView.setRedo();
    		break;
    	case UNDO:
			//撤销
			mDrawView.setUndo();
    		break;
    	case PAINT:
    		if(data1>-1)
    			mDrawView.setPaintWidth((int)data1);
    		if(data2>-1)
    			mDrawView.setPaintAlpha((int)data2);
    		if(data3>-1)
    			mDrawView.setPaintStyle((int)data3);
    		if(data4!=Float.MAX_VALUE)
    			mDrawView.setPaintColor((int)data4);
    		break;
        default:
            break;
    	}
    }

    //创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, 1, "调试");
		menu.add(Menu.NONE, 2, 2, "清屏");
		menu.add(Menu.NONE, 3, 3, "保存");
		menu.add(Menu.NONE, 4, 4, "退出");
		return true;
	}
	
	public static HashMap<String,User> getLocalMap(){
		return mLocalPlayersMap;
	}
	
    @Override
    //按2下返回退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                Toast.makeText(this, getString(R.string.maintab_toast_logout), Toast.LENGTH_SHORT)
                        .show();
                ExitTime = System.currentTimeMillis();
            }
            else {
                ActivitiesManager.finishAllActivities();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			break;
		case 2:
			doClickEvent(OP_TYPE.CLEAR, -1, -1, -1, -1);
			DataDraw data  = new DataDraw(OP_TYPE.CLEAR, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			mBtService.sendMessage(MSGConst.SEND_DRAW, data);
			break;
		case 3:
			mDrawView.setSave();
			break;
		case 4:
            ActivitiesManager.finishAllActivities();
			break;
		default:
			break;
		}
		return true;
	}
    
    private void setColorBack(int m){
		for(int i=0;i<7;i++){
			if(i!=m)
				mBtColors[i].setBackgroundResource(colorSource[i]);
		}
	}

    /** 刷新用户分数列表UI **/
    private void refreshScoreAdapter() {
    	//刷新积分列表
        scoresAdapter.setData(mLocalScoresList); 
        scoresAdapter.notifyDataSetChanged();
    }

    /** 刷新用户消息列表UI **/
    private void refreshMsgAdapter() {
    	//刷新消息列表
    	if(isMeDraw){
	        msgsAdapter.setData(mLocalMsgsList);
	        msgsAdapter.notifyDataSetChanged();
	        mLvMsg.setSelection(mLocalMsgsList.size());
    	}
    }
    
    /**
     * 将usersMap按score降序排列成list
     */
    private void sortScoresList(){
    	mLocalScoresList = new ArrayList<User>(mLocalPlayersMap.size());
        for (Map.Entry<String, User> entry : mLocalPlayersMap.entrySet()) {
    		int i = 0;
			User nUser = entry.getValue();
			if(mLocalScoresList.isEmpty())
    			mLocalScoresList.add(nUser);
			else
	        	for(User mUser:mLocalScoresList){
	        		if(nUser.getScore()>=mUser.getScore()){
	        			mLocalScoresList.add(i, nUser);
	        			break;
	        		}
	        		else if(i == mLocalScoresList.size()-1)
	        			mLocalScoresList.add(nUser);
	        		i++;
	        	}
        }
    }
    
    private void handleMsg(MSGProtocol pMsg){
    	Message msg = new Message();
    	Bundle b = new Bundle();
    	switch(pMsg.getCommandNo()){
	    	case MSGConst.SEND_DRAW:{
	        	DataDraw data = (DataDraw) pMsg.getAddObject();
	        	OP_TYPE op = data.getOpType();
	        	TOUCH_TYPE touch = data.getTouchType();
	        	float data1 = data.getData1();
	        	float data2 = data.getData2();
	        	float data3 = data.getData3();
	        	float data4 = data.getData4();
	        	b.putInt("opType", op.ordinal());
	        	b.putInt("touchType", touch.ordinal());
	        	b.putFloat("data1", data1);
	        	b.putFloat("data2", data2);
	        	b.putFloat("data3", data3);
	        	b.putFloat("data4", data4);
	        	break;
	    	}
	    	case MSGConst.SEND_START: {
	    		if(isMeDraw){
	            	mBtService.sendMessage(MSGConst.SEND_START, null);
		    		isMeDraw = false;
		    		SessionUtils.setOrder(2);
	    		}
	    		else{

	    			isMeDraw = true;
	    			SessionUtils.setOrder(1);

        			DBOperate db = new DBOperate(this);
        			WordInfo w1 = db.getRandomWord(System.currentTimeMillis());
        			String word1 = new String(EncryptUtils.decrypt(w1.getWord(), Constant.PASSWORD));
        			String kind1 = new String(EncryptUtils.decrypt(w1.getKind(), Constant.PASSWORD));
        			WordInfo w2 = db.getRandomWord(System.currentTimeMillis());
        			String word2 = new String(EncryptUtils.decrypt(w2.getWord(), Constant.PASSWORD));
        			String kind2 = new String(EncryptUtils.decrypt(w2.getKind(), Constant.PASSWORD));
        			mLocalWord1 = word1;
        			mLocalWord2 = word2;
        			mLocalKind1 = kind1;
        			mLocalKind2 = kind2;
        			db.close();
	    			
        			
	    		}
	    		break;
	    	}
	    	case MSGConst.SEND_ONLINE: {
	    		User user = (User) pMsg.getAddObject();
	    		if(!mLocalPlayersMap.containsKey(user.getIMEI())){
	    			mLocalPlayersMap.put(user.getIMEI(), user);
	    			mBtService.sendMessage(MSGConst.SEND_ONLINE, SessionUtils.getLocalUserInfo());
	    		}
	    		break;
	    	}
	        case MSGConst.SEND_TIP:{
        		b.putString("toast", "对方很好心的提示你:"+ pMsg.getAddStr());
		        android.os.Message tMsg = new android.os.Message();
		        tMsg.what = MSGConst.SHOW_TOAST;
		        tMsg.setData(b);
				handler.sendMessage(tMsg);
	        	break;
	        }
	    	case MSGConst.SEND_GUESS_TRUE:{
            	
            	//猜词加2分
            	User guessUser =  mLocalPlayersMap.get(SessionUtils.getIMEI());
            	int score = guessUser.getScore();
        		score+=2;
            	guessUser.setScore(score);
	            	
            	//绘图加1分
            	User drawUser =  mLocalPlayersMap.get(pMsg.getSenderIMEI());
            	score = drawUser.getScore();
            	drawUser.setScore(++score);

        		b.putString("toast", "恭喜你猜对了，积分加2");
		        android.os.Message tMsg = new android.os.Message();
		        tMsg.what = MSGConst.SHOW_TOAST;
		        tMsg.setData(b);
				handler.sendMessage(tMsg);
	        	//排序积分榜
            	sortScoresList();
            	
	        	break;
	        }
	        case MSGConst.SEND_GUESS_FALSE:{
        		b.putString("toast", "很遗憾你猜错了");
		        android.os.Message tMsg = new android.os.Message();
		        tMsg.what = MSGConst.SHOW_TOAST;
		        tMsg.setData(b);
				handler.sendMessage(tMsg);
	        	break;
	        }
	    	case MSGConst.SEND_GUESS_WORD:{
				//验证正确性
				boolean guessResult = true;
				DataGuess dg = (DataGuess)pMsg.getAddObject();
				String result = dg.getMsgContent();

            	if(isMeDraw){
            		DataGuess s = dg.clone();
            		s.setMsgContent("我猜是"+s.getMsgContent());
            		mLocalMsgsList.add(s);
            	}
            	
				guessResult = result.equals(mLocalWord) ? true : false;
				
				if(guessResult){
	            	//猜词加2分
	            	User guessUser =  mLocalPlayersMap.get(pMsg.getSenderIMEI());
	            	int score = guessUser.getScore();
	        		score+=2;
	            	guessUser.setScore(score);
		            	
	            	//绘图加1分
	            	User drawUser =  mLocalPlayersMap.get(SessionUtils.getIMEI());
	            	score = drawUser.getScore();
	            	drawUser.setScore(++score);
	            	
            		b.putString("toast", SessionUtils.getNickname()+"猜对了，你加1分");
    		        android.os.Message tMsg = new android.os.Message();
    		        tMsg.what = MSGConst.SHOW_TOAST;
    		        tMsg.setData(b);
    				handler.sendMessage(tMsg);
            		DataGuess dg2 = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), "好厉害，这也能猜出来");
            		mLocalMsgsList.add(dg2);
					mBtService.sendMessage(MSGConst.SEND_GUESS_TRUE, null);
				}
				else{
	            	User guessUser =  mLocalPlayersMap.get(pMsg.getSenderIMEI());
            		b.putString("toast", guessUser.getNickname()+"猜错了");
    		        android.os.Message tMsg = new android.os.Message();
    		        tMsg.what = MSGConst.SHOW_TOAST;
    		        tMsg.setData(b);
    				handler.sendMessage(tMsg);
            		DataGuess dg2 = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), 
            				guessUser.getNickname()+",你个逗逼猜错了");
            		mLocalMsgsList.add(dg2);
					mBtService.sendMessage(MSGConst.SEND_GUESS_FALSE, null);
				}
				break;
			}
    	}
    	msg.what = pMsg.getCommandNo();
    	msg.setData(b);
    	handler.sendMessage(msg);
    }
    
    /**
	 * 主线程处理UI变化
	 *
	 */
    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Constant.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                readMessage+=" ";
                String[] strArray = null; 
    	    	strArray = readMessage.split("@sp"); 
        		for(int i = 0; i<strArray.length ; i++){
        			String sendMsg = strArray[i];
        			if(i == strArray.length -1 ){
    					saveStr = sendMsg.trim();
        			}
        			else{
        				MSGProtocol pMsg;
        				try {
        					pMsg = new MSGProtocol(sendMsg);
                            LogUtils.i("Read", pMsg.getCommandNo()+"");
                            handleMsg(pMsg);
                            
        				} catch (JSONException e) {
                            LogUtils.e("json", "json wrong");
        				}
        			}
        		}
        		
        		
                break;
            case MSGConst.SEND_START:{
            	//如果自己是链表第一个
        		if(isMeDraw){
        			mLayoutChatEdit.setVisibility(View.GONE);
        			mChatTab.setVisibility(View.VISIBLE);
        	    	mLayoutColorBar.setVisibility(View.VISIBLE);
        	    	mLayoutBtnBar.setVisibility(View.VISIBLE);
        	    	
            		//显示选词对话框
            		DialogInterface.OnClickListener firstListener = new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mLocalWord = mLocalWord1;
							mLocalKind = mLocalKind1;
							handler.sendEmptyMessage(MSGConst.SET_TEXT);
							mBtService.sendMessage(MSGConst.SEND_CHOOSED, null);
						}
					};
            		DialogInterface.OnClickListener secondListener = new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mLocalWord = mLocalWord2;
							mLocalKind = mLocalKind2;
							handler.sendEmptyMessage(MSGConst.SET_TEXT);
							mBtService.sendMessage(MSGConst.SEND_CHOOSED, null);
						}
					};
            		showAlertDialog(
            				"选择词汇","请从下面两个词语中选择一个你要画的词汇吧",
            				mLocalWord1, firstListener,
            				mLocalWord2, secondListener);
            		
        		}
        		else{
        			mLayoutChatEdit.setVisibility(View.VISIBLE);
        			mChatTab.setVisibility(View.GONE);
        	    	mLayoutColorBar.setVisibility(View.GONE);
        	    	mLayoutBtnBar.setVisibility(View.GONE);
        	    	showLoadingDialog("等待绘图者选词噢");
        		}
        		
    			//退出上个计时进程
        		if(mLocalTimerCheck != null)
        			mLocalTimerCheck.exit();
    			//设定游戏时间
    			mLocalTimerCheck = new TimerUtils() {
    	            @Override
    	            public void doTimeOutWork() {
    	            	mBtService.sendMessage(MSGConst.SEND_START, null);
    	            }

    	            @Override
    	            public void doTimerCheckWork() {
    	            	handler.sendEmptyMessage(MSGConst.TIME_CHECK);
    	            }
    	        };
    	        mLocalTimerCheck.start(Constant.GAME_TIME, 1000);
        		//清空画布
        		doClickEvent(OP_TYPE.CLEAR, -1, -1, -1, -1);
    			

        		//设置消息表
        		mLocalMsgsList = new ArrayList<DataGuess>();
        		msgsAdapter = new MsgsAdapter(BtDrawGuessActivity.this, mLocalMsgsList);
        		mLvMsg.setAdapter(msgsAdapter);
            	//设置积分表格
        		scoresAdapter = new ScoresAdapter(BtDrawGuessActivity.this, mLocalScoresList);
        		mLvScore.setAdapter(scoresAdapter);
                scoresAdapter.notifyDataSetChanged();
            	break;
            }
            case MSGConst.SEND_ONLINE:{
            	sortScoresList();
            	refreshScoreAdapter();
            	break;
            }
            case MSGConst.SEND_CHOOSED:{
            	if(!isMeDraw)
            		dismissLoadingDialog();
            	break;
            }
            case MSGConst.SEND_OFFLINE:{
            	showCustomToast("对方退出了游戏，断开连接");
	        	finish();
            	break;
            }
            case MSGConst.SEND_GUESS_TRUE:{
            	//如果是自己猜对的，隐藏猜词框
            	refreshScoreAdapter();
            	refreshMsgAdapter();
            	mBtService.sendMessage(MSGConst.SEND_START, null);
            	break;
            }
            case MSGConst.SEND_GUESS_FALSE:{
            	refreshMsgAdapter();
            	break;
            }
            case MSGConst.SEND_DRAW:{
	        	OP_TYPE op = OP_TYPE.values()[msg.getData().getInt("opType")];
	        	TOUCH_TYPE touch = TOUCH_TYPE.values()[msg.getData().getInt("touchType")];
	        	float data1 =  msg.getData().getFloat("data1");
	        	float data2 =  msg.getData().getFloat("data2");
	        	float data3 =  msg.getData().getFloat("data3");
	        	float data4 =  msg.getData().getFloat("data4");
	        	switch (op){
	        	case DRAW:
	        	case FILL:
	        	case TRANS:
	        		if(!isMeDraw){
	        			mDrawView.doOperation(touch, 
	        					data1 * mDrawView.getWX(), data2 * mDrawView.getHY(), 
	        					data3 * mDrawView.getWX(), data4 * mDrawView.getHY());
	        		}
	        		break;
	        	case ERASE:
	        	case PAINT:
	        	case SHAPE:
	        	case PACK:
	        	case REDO:
	        	case UNDO:
	        	case EDIT:
	        	case COPY:
	        	case DELETE:
	        	case CLEAR:
	        		if(!isMeDraw)
	        			doClickEvent(op,data1,data2,data3,data4);
	        		break;
	            default:
	                break;
	        	}
	        	break;
	        }
            case MSGConst.TIME_CHECK:
            	mTvTime.setText(mLocalTimerCheck.getCount() + "秒");
            	break;
            case MSGConst.SHOW_TOAST:
            	String s = msg.getData().getString("toast");
            	showCustomToast(s);
            	break;
            case MSGConst.SET_TEXT:
            	mTvDraw.setText("你要画的词是：" + mLocalWord);
            	break;
        	case MSGConst.DEBUG_MSG:
				mDebug.setText(logNum + " ");
        		break;
            default:
                break;
            }
        }
    }

    
}
