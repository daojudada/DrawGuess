package com.drawguess.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.drawguess.R;
import com.drawguess.adapter.ScoresAdapter;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.dialog.ColorDialog;
import com.drawguess.dialog.PaintDialog;
import com.drawguess.dialog.ShapeDialog;
import com.drawguess.drawop.OpDraw.Shape;
import com.drawguess.interfaces.OnColorChangedListener;
import com.drawguess.interfaces.OnPaintChangedListener;
import com.drawguess.interfaces.OnShapeChangedListener;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.msgbean.DataDraw;
import com.drawguess.msgbean.DataDraw.OP_TYPE;
import com.drawguess.msgbean.DataDraw.TOUCH_TYPE;
import com.drawguess.msgbean.Users;
import com.drawguess.net.MSGConst;
import com.drawguess.net.MSGProtocol;
import com.drawguess.net.NetManage;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TypeUtils;
import com.drawguess.view.DrawView;

public class DrawGuessActivity extends BaseActivity implements OnClickListener{
	private static final String TAG = "DrawGuessActivity";
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
    private View mVDraw,mVChat,mVResult;
    private Button mBtSend;
    private EditText mEtEdit;
    private ListView mLvScore,mLvMessage;
    
    private ScoresAdapter scoresAdapter;
    
    public TextView mDebug;
    public int logDown=0,logMove=0,logUp=0;
    
    
    /**
     * 顺序链表
     */
    private ArrayList<String> mServerOrderList;
    /**
     * 服务器保存的玩家表
     */
    private HashMap<String,Users> mServerPlayersMap;
    
    /**
     * 本地保存的玩家表
     */
    private HashMap<String,Users> mLocalPlayersMap;
    /**
     * 本地保存的分数临时表
     */
    private ArrayList<Users> mLocalScoresList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawtabs);
        isPack = false;
		isEraser = false;
		isTrans = false;
        initViews();
        initEvents();

		netManage = NetManage.getInstance(this);
		mDrawView.setNetManage(netManage);
		//得到游戏顺序链表
		if(NetManage.getState() == 2){
			//服务器获得游戏顺序
			mServerPlayersMap = new HashMap<String,Users>();
		    mLocalPlayersMap = new HashMap<String,Users>();
		    mLocalScoresList = new ArrayList<Users>();
		    mServerOrderList = this.getIntent().getExtras().getStringArrayList("order");
			HashMap<String,Users> mServerUserMap = NetManage.getServerUserMap();
			for(String imei :mServerOrderList){
				if(mServerUserMap.containsKey(imei)){
					Users user = mServerUserMap.get(imei);
					mServerPlayersMap.put(imei, user);
					mLocalScoresList.add(user);
				}
			}

			//创建服务器消息处理回调
			createServerListener();
			//创建客户端消息处理回调
			createClientListener();
			netManage.sendToAllClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList));
		}
		else{
		    mLocalPlayersMap = new HashMap<String,Users>();
		    mLocalScoresList = new ArrayList<Users>();
			//创建客户端消息处理回调
			createClientListener();
		}
		
		
		//如果自己是链表第一个
		if(SessionUtils.getOrder() == 1){
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
    protected void onDestroy() {
        super.onDestroy();

    }
	
	@Override
	protected void initEvents() {
		for(int i=0;i<7;i++){
			mBtColors[i].setOnClickListener(this);
		}
		
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
		mEtEdit.setOnEditorActionListener(new OnEditorActionListener(){
			@Override       
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {  
				if (arg1 == EditorInfo.IME_ACTION_SEND) {      
					netManage.sendToServer(MSGConst.SEND_GUESS_WORD, mEtEdit.getText());
				}         
				return false;       
			}  
		});
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
    	
		mVDraw = findViewById(R.id.drawtabview);
		mVChat = findViewById(R.id.chattabview);
		mVResult = findViewById(R.id.resulttabview);
		
		mDrawTab = (LinearLayout)findViewById(R.id.drawtab);
		mChatTab = (LinearLayout)findViewById(R.id.chattab);
		mResultTab = (LinearLayout)findViewById(R.id.resulttab);
		
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

	    mBtSend = (Button)findViewById(R.id.drawtab_chat_send);
	    mEtEdit = (EditText)findViewById(R.id.drawtab_chat_editer);

    }
	
	@Override
	public void onClick(View v) {
		int m;
		DataDraw data;
		switch (v.getId()) {
		case R.id.drawtab_chat_send:
			netManage.sendToServer(MSGConst.SEND_GUESS_WORD, mEtEdit.getText());
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
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.red:
			m=1;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFF0000);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFF0000);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.blue:
			m=2;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF0000FF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF0000FF);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.green:
			m=3;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF00FF00);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF00FF00);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.yellow:
			m=4;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFFFF00);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFFFF00);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.purple:
			m=5;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFFFF00FF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFFFF00FF);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.ching:
			m=6;
			doClickEvent(OP_TYPE.PAINT, -1, -1, -1, 0xFF00FFFF);
			data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, 0xFF00FFFF);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
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
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					}
				}
			);
			break;
		case R.id.fill:
			doClickEvent(OP_TYPE.PACK, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.PACK, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.erase:
			doClickEvent(OP_TYPE.ERASE, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.ERASE, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.copy:
			doClickEvent(OP_TYPE.COPY, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.COPY, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.delete:
			doClickEvent(OP_TYPE.DELETE, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.DELETE, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.colorpick:
			//颜色设置，对话框
			new ColorDialog(this, mDrawView.getPaintColor(), "color", 
				new OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						doClickEvent(OP_TYPE.PAINT, -1, -1, -1, color);
						DataDraw data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, -1, -1, -1, color);
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
						setColorBack(-1);
					}
				}
			).show();
			break;
		case R.id.edit:
			doClickEvent(OP_TYPE.EDIT, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.EDIT, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.paint:
			//画笔设置，对话框
			new PaintDialog(this, mDrawView.getPaintWidth(), mDrawView.getPaintAlpha(), mDrawView.getPaintStyle(),mDrawView.getPaintColor(),
					new OnPaintChangedListener(){
						@Override
						public void paintChanged(int width, int alpha, int style) {
							doClickEvent(OP_TYPE.PAINT, width, alpha, style, Float.MAX_VALUE);
							DataDraw data  = new DataDraw(OP_TYPE.PAINT, TOUCH_TYPE.DEFAULT, width, alpha, style, Float.MAX_VALUE);
							netManage.sendToServer(MSGConst.SEND_DRAW, data);
						}
				}
			).show();
			break;
		case R.id.undo:
			doClickEvent(OP_TYPE.UNDO, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.UNDO, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
			break;
		case R.id.menu:
			//菜单
			openOptionsMenu();
			break;
		case R.id.redo:
			doClickEvent(OP_TYPE.REDO, -1, -1, -1, -1);
			data  = new DataDraw(OP_TYPE.REDO, TOUCH_TYPE.DEFAULT, -1, -1, -1, -1);
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
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
			netManage.sendToServer(MSGConst.SEND_DRAW, data);
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
    
    /**
     * 创建客户端通信监听
     */
    private void createClientListener(){
    	clientListener = new OnMsgRecListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
		        switch (command) {
		        case MSGConst.ANS_PLAYERS:{//同步数据
		        	List<String> order = new ArrayList<String>();
		        	order = TypeUtils.cStringToList(pMsg.getAddStr());
					HashMap<String,Users> mLocalUserMap = NetManage.getLocalUserMap();
					for(String imei :order){
						if(mLocalUserMap.containsKey(imei)){
							Users user = mLocalUserMap.get(imei);
							mLocalPlayersMap.put(imei, user);
							mLocalScoresList.add(user);
						}
						else{
							mLocalScoresList.add(SessionUtils.getLocalUserInfo());
						}
					}
		        	break;
		        }
				case MSGConst.ANS_GUESS_WORD:{
					//验证正确性
					boolean guessResult = true;
					//....
					if(guessResult){
						netManage.sendToServer(MSGConst.SEND_GUESS_TRUE, pMsg.getSenderIMEI());
					}
					else{
						netManage.sendToServer(MSGConst.SEND_GUESS_FALSE, pMsg.getSenderIMEI());
					}
					break;
				}
		        case MSGConst.ANS_DRAW:{
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
		        	

	        		if(SessionUtils.getOrder()!=1){
		        		if(touch == TOUCH_TYPE.DOWN1){
		        			logDown++;
		        		}
		        		if(touch == TOUCH_TYPE.MOVE){
		        			logMove++;
		        		}
		        		if(touch == TOUCH_TYPE.UP1){
		        			logUp++;
		        			handler.sendEmptyMessage(MSGConst.DEBUG_MSG);
		        		}
	        		}
	        		
		        	break;
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
	}
	
    
    /**
     * 创建服务器端通信监听
     */
	private void createServerListener(){
		serverListener = new OnMsgRecListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
				int command = pMsg.getCommandNo();
				switch (command) {
				case MSGConst.SEND_DRAW:
		        	DataDraw data = (DataDraw) pMsg.getAddObject();
		        	TOUCH_TYPE touch = data.getTouchType();
		        	

	        		if(touch == TOUCH_TYPE.DOWN1){
	        			logDown++;
	        		}
	        		if(touch == TOUCH_TYPE.MOVE){
	        			logMove++;
	        		}
	        		if(touch == TOUCH_TYPE.UP1){
	        			logUp++;
	        			handler.sendEmptyMessage(MSGConst.DEBUG_MSG);
	        		}
					netManage.sendToAllClient(MSGConst.ANS_DRAW, (DataDraw)pMsg.getAddObject());
					break;
				case MSGConst.SEND_GUESS_WORD:
					netManage.sendToClient(MSGConst.ANS_GUESS_WORD, pMsg.getAddStr(), mServerOrderList.get(0));
					break;
				case MSGConst.SEND_GUESS_TRUE:
					netManage.sendToAllClient(MSGConst.ANS_GUESS_TRUE, pMsg.getAddStr());
					break;
				case MSGConst.SEND_GUESS_FALSE:
					netManage.sendToAllClient(MSGConst.ANS_GUESS_FALSE, pMsg.getAddStr());
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
	

    /** 刷新用户分数列表UI **/
    private void refreshScoresAdapter() {
        scoresAdapter.setData(mLocalScoresList); // Adapter加载List数据
        scoresAdapter.notifyDataSetChanged();
    }
    
	/**
	 * 主线程处理UI变化
	 *
	 */
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSGConst.ANS_PLAYERS:{
            	//设置积分表格
        		scoresAdapter = new ScoresAdapter(DrawGuessActivity.this, mLocalScoresList);
        		mLvScore.setAdapter(scoresAdapter);
            	break;
            }
            case MSGConst.ANS_GUESS_TRUE:{
            	refreshScoresAdapter();
            }
            case MSGConst.ANS_DRAW:{
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
	        		if(SessionUtils.getOrder()!=1){
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
	        		if(SessionUtils.getOrder()!=1)
	        			doClickEvent(op,data1,data2,data3,data4);
	        		break;
	        	case CLEAR:
	        		logDown = 0;
	        		logMove = 0;
	        		logUp = 0;
	        		if(SessionUtils.getOrder()!=1)
	        			doClickEvent(op,data1,data2,data3,data4);
	        		break;
	            default:
	                break;
	        	}
	        	
	        }
        	case MSGConst.DEBUG_MSG:
				mDebug.setText(logDown+" "+logMove+" "+logUp+" " +mDrawView.getOpSize());
        		break;
            default:
                break;
            }
        }
    }

}
