package com.drawguess.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.drawguess.R;
import com.drawguess.adapter.MsgsAdapter;
import com.drawguess.adapter.ScoresAdapter;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.Constant;
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
import com.drawguess.msgbean.DataGuess;
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

public class DrawGuessActivity extends BaseActivity implements OnClickListener{
	private static final String TAG = "DrawGuessActivity";
    /**
     * 延时退出时间变量
     */
    private long ExitTime; 
    private NetManage netManage;
    /**
     * 消息传递类
     */
    public MyHandler handler = new MyHandler();
    /**
     * 绘图状态
     */
    private Boolean isPack,isEraser,isTrans;
    private Boolean isLoad;
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
    
    private ScoresAdapter scoresAdapter;
    private MsgsAdapter msgsAdapter;
    private TimerUtils mServerTimerCheck,mLocalTimerCheck;
    
    
    /**
     * 猜对人数
     */
    private int guessNum;
    /**
     * 顺序链表
     */
    private ArrayList<String> mServerOrderList;
    /**
     * 服务器保存的玩家表
     */
    private HashMap<String,User> mServerPlayersMap;
    
    /**
     * 本地保存的玩家表
     */
    private HashMap<String,User> mLocalPlayersMap;
    /**
     * 本地保存的消息表
     */
    private ArrayList<DataGuess> mLocalMsgsList;
    /**
     * 本地保存的分数临时表
     */
    private ArrayList<User> mLocalScoresList;
    /**
     * 当前绘图用户
     */
    private User mLocalDrawUser;
    private String mLocalWord,mLocalWord1,mLocalWord2;
    private String mLocalKind,mLocalKind1,mLocalKind2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawtabs);
        
        showLoadingDialog("载入游戏中。。。");
        
        isPack = false;
		isEraser = false;
		isTrans = false;
		isLoad = false;
		
        initViews();
        initEvents();

        //获得网络层单例
		netManage = NetManage.getInstance(this);
		mDrawView.setNetManage(netManage);
		//得到网络层状态
		if(NetManage.getState() == 2){
			initServer();
		}
		else{
			initClient();
		}

    }
    

	@Override
    protected void onDestroy() {
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
					netManage.sendToServer(MSGConst.SEND_GUESS_WORD, dg);
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
	public void onClick(View v) {
		int m;
		DataDraw data;
		switch (v.getId()) {
		case R.id.drawtab_bt_tip:
			netManage.sendToServer(MSGConst.SEND_TIP, mLocalKind);
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
				netManage.sendToServer(MSGConst.SEND_GUESS_WORD, dg);
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
    
    /**
     * 设置各玩家游戏顺序
     */
    private void setUsersOrder(List<String> order){
		HashMap<String,User> mLocalUserMap = NetManage.getLocalUserMap();
	    mLocalPlayersMap = new HashMap<String,User>();
		int i = 0;
		for(String imei :order){
			i++;
			if(imei.equals(SessionUtils.getIMEI())){
				SessionUtils.setOrder(i);
				User user = SessionUtils.getLocalUserInfo();
				user.setOrder(i);
				mLocalPlayersMap.put(imei, user);
			}
			else{
				User user = mLocalUserMap.get(imei);
				user.setOrder(i);
				mLocalPlayersMap.put(imei, user);
			}
		}
		mLocalDrawUser = mLocalPlayersMap.get(order.get(0));
    }
    
    /**
     * 是否所有人都猜对
     * @return
     */
	private boolean isAllGuessTrue(){
		guessNum++;
		if(guessNum == mServerOrderList.size() - 1)
			return true;
		else
			return false;
	}
	
	/**
	 * 初始化服务器
	 */
	private void initServer(){
		guessNum = 0;
		//服务器获得游戏顺序
		mServerPlayersMap = new HashMap<String,User>();
	    mServerOrderList = this.getIntent().getExtras().getStringArrayList("order");
		HashMap<String,User> mServerUserMap = NetManage.getServerUserMap();
		for(String imei :mServerOrderList){
			if(mServerUserMap.containsKey(imei)){
				User user = mServerUserMap.get(imei);
				mServerPlayersMap.put(imei, user);
			}
		}

		//创建服务器消息处理回调
		createServerListener();
		//创建客户端消息处理回调
		createClientListener();
		netManage.sendToAllClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList));
	}
	
	/**
	 * 初始化客户端
	 */
	private void initClient(){
		//创建客户端消息处理回调
		createClientListener();
		netManage.sendToServer(MSGConst.SEND_ONLINE, null);
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
		        case MSGConst.ANS_PLAYERS:{
		        	//同步数据
		        	List<String> order = new ArrayList<String>();
		        	order = TypeUtils.cStringToList(pMsg.getAddStr());
		        	//设置游戏顺序
		        	setUsersOrder(order);
		        	//排序积分榜
					sortScoresList();
					//如果是绘图者，从数据库中随机取词
	        		if(SessionUtils.getOrder() == 1){
	        			DBOperate db = new DBOperate(DrawGuessActivity.this);
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
		        case MSGConst.ANS_GAME_OVER:{
		        	mLocalPlayersMap.clear();
		        	netManage.stop();
		        	NetManage.setState(0);
		        	break;
		        }
		        case MSGConst.ANS_OFFLINE:{
		        	String name = mLocalPlayersMap.get(pMsg.getAddStr()).getNickname();
		            mLocalPlayersMap.remove(pMsg.getAddStr());
		        	b.putString("offline", name);
		        	break;
		        }
		        case MSGConst.ANS_GUESS_TRUE:{
	            	
	            	//猜词加2分
	            	User guessUser =  mLocalPlayersMap.get(pMsg.getAddStr());
	            	int score = guessUser.getScore();
            		score+=2;
	            	guessUser.setScore(score);
		            	
	            	//绘图加1分
	            	score = mLocalDrawUser.getScore();
	            	mLocalDrawUser.setScore(++score);


	            	if(SessionUtils.getOrder() == 1){
	            		b.putString("toast", guessUser.getNickname()+"猜对了，你加1分");
	    		        android.os.Message tMsg = new android.os.Message();
	    		        tMsg.what = MSGConst.SHOW_TOAST;
	    		        tMsg.setData(b);
	    				handler.sendMessage(tMsg);
	            		DataGuess dg = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), "好厉害，这也能猜出来");
	            		mLocalMsgsList.add(dg);
	            	}
	            	else if(SessionUtils.getIMEI().equals(guessUser.getIMEI())){
	            		b.putString("toast", "恭喜你猜对了，积分加2");
	    		        android.os.Message tMsg = new android.os.Message();
	    		        tMsg.what = MSGConst.SHOW_TOAST;
	    		        tMsg.setData(b);
	    				handler.sendMessage(tMsg);
	            	}
	            	
		        	//排序积分榜
	            	sortScoresList();
	            	
		        	break;
		        }
		        case MSGConst.ANS_GUESS_FALSE:{
	            	User guessUser =  mLocalPlayersMap.get(pMsg.getAddStr());
	            	if(SessionUtils.getOrder() == 1){
	            		b.putString("toast", guessUser.getNickname()+"猜错了");
	    		        android.os.Message tMsg = new android.os.Message();
	    		        tMsg.what = MSGConst.SHOW_TOAST;
	    		        tMsg.setData(b);
	    				handler.sendMessage(tMsg);
	            		DataGuess dg = new DataGuess(SessionUtils.getIMEI(), DataUtils.getNowtime(), 
	            				guessUser.getNickname()+",你个逗逼猜错了");
	            		mLocalMsgsList.add(dg);
	            	}
	            	else{
	            		b.putString("toast", "很遗憾你猜错了");
	    		        android.os.Message tMsg = new android.os.Message();
	    		        tMsg.what = MSGConst.SHOW_TOAST;
	    		        tMsg.setData(b);
	    				handler.sendMessage(tMsg);
	            	}
		        	break;
		        }
				case MSGConst.ANS_GUESS_WORD:{
					//验证正确性
					boolean guessResult = true;
					DataGuess dg = (DataGuess)pMsg.getAddObject();
					String result = dg.getMsgContent();

	            	if(SessionUtils.getOrder() == 1){
	            		DataGuess s = dg.clone();
	            		s.setMsgContent("我猜是"+s.getMsgContent());
	            		mLocalMsgsList.add(s);
	            	}
	            	
					guessResult = result.equals(mLocalWord) ? true : false;
					
					if(guessResult){
						netManage.sendToServer(MSGConst.SEND_GUESS_TRUE, dg.getSenderIMEI());
					}
					else{
						netManage.sendToServer(MSGConst.SEND_GUESS_FALSE, dg.getSenderIMEI());
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
		        	case CLEAR:
		        		if(SessionUtils.getOrder()!=1)
		        			doClickEvent(op,data1,data2,data3,data4);
		        		break;
		            default:
		                break;
		        	}
		        	
	        		if(SessionUtils.getOrder()!=1){
						logNum++;
	        		}
        			handler.sendEmptyMessage(MSGConst.DEBUG_MSG);
		        	break;
		        }
		        case MSGConst.ANS_TIP:{
            		b.putString("toast", "对方很好心的提示你:"+ pMsg.getAddStr());
    		        android.os.Message tMsg = new android.os.Message();
    		        tMsg.what = MSGConst.SHOW_TOAST;
    		        tMsg.setData(b);
    				handler.sendMessage(tMsg);
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
				case MSGConst.SEND_ONLINE:
					netManage.sendToClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList), pMsg.getSenderIMEI());
					break;
				case MSGConst.SEND_OFFLINE:
					String imei = pMsg.getSenderIMEI();
					if(mServerOrderList.get(0).equals(imei)){
					    mServerOrderList.remove(imei);
					    mServerPlayersMap.remove(imei);
					    netManage.sendToAllClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList));
					}
					else{
					    mServerOrderList.remove(imei);
					    mServerPlayersMap.remove(imei);
					}
					netManage.sendToAllExClient(MSGConst.ANS_OFFLINE, imei, imei);
					break;
				case MSGConst.SEND_DRAW:
					netManage.sendToAllClient(MSGConst.ANS_DRAW, (DataDraw)pMsg.getAddObject());
					
					break;
				case MSGConst.SEND_GUESS_WORD:
					netManage.sendToClient(MSGConst.ANS_GUESS_WORD, (DataGuess)pMsg.getAddObject(), mServerOrderList.get(0));
					break;
				case MSGConst.SEND_GUESS_TRUE:
					//猜词者和画图者加分
					User guessUser = mServerPlayersMap.get(pMsg.getSenderIMEI());
        			int score = guessUser.getScore();
        			score+=2;
        			guessUser.setScore(score);
					User drawUser = mServerPlayersMap.get(mServerOrderList.get(0));
        			score = drawUser.getScore();
        			drawUser.setScore(++score);
	            	//发送猜对消息
					netManage.sendToAllClient(MSGConst.ANS_GUESS_TRUE, pMsg.getAddStr());
					if(isAllGuessTrue()){
						guessNum = 0;
						//重新分配游戏顺序
						mServerOrderList.add(mServerOrderList.get(0));
						mServerOrderList.remove(0);
						netManage.sendToAllClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList));
					}
					break;
				case MSGConst.SEND_GUESS_FALSE:
					netManage.sendToAllClient(MSGConst.ANS_GUESS_FALSE, pMsg.getAddStr());
					break;
				case MSGConst.SEND_CHOOSED:
					netManage.sendToAllExClient(MSGConst.ANS_CHOOSED, null, pMsg.getSenderIMEI());
					break;
				case MSGConst.SEND_TIP:
					netManage.sendToAllExClient(MSGConst.ANS_TIP, pMsg.getAddStr(), pMsg.getSenderIMEI());
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
    private void refreshScoreAdapter() {
    	//刷新积分列表
        scoresAdapter.setData(mLocalScoresList); 
        scoresAdapter.notifyDataSetChanged();
    }

    /** 刷新用户消息列表UI **/
    private void refreshMsgAdapter() {
    	//刷新消息列表
    	if(SessionUtils.getOrder() == 1){
	        msgsAdapter.setData(mLocalMsgsList);
	        msgsAdapter.notifyDataSetChanged();
	        mLvMsg.setSelection(mLocalMsgsList.size());
    	}
    }
    
	/**
	 * 主线程处理UI变化
	 *
	 */
    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSGConst.ANS_CHOOSED:{
            	dismissLoadingDialog();
            	break;
            }
            case MSGConst.ANS_PLAYERS:{
                //载入完成
            	if(!isLoad){
            		dismissLoadingDialog();
            		isLoad = true;
            	}
            	//如果自己是链表第一个
        		if(SessionUtils.getOrder() == 1){
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
							netManage.sendToServer(MSGConst.SEND_CHOOSED, null);
						}
					};
            		DialogInterface.OnClickListener secondListener = new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mLocalWord = mLocalWord2;
							mLocalKind = mLocalKind2;
							handler.sendEmptyMessage(MSGConst.SET_TEXT);
							netManage.sendToServer(MSGConst.SEND_CHOOSED, null);
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
        		
        		//创建计时进程
        		if(NetManage.getState() == 2){
        			//退出上个计时进程
        			if(mServerTimerCheck != null)
        				mServerTimerCheck.exit();
        			//设定游戏时间
        			mServerTimerCheck = new TimerUtils() {
        	            @Override
        	            public void doTimeOutWork() {
        	            	//时间结束下一轮游戏
        					guessNum = 0;
        					//重新分配游戏顺序
        					mServerOrderList.add(mServerOrderList.get(0));
        					mServerOrderList.remove(0);
        					netManage.sendToAllClient(MSGConst.ANS_PLAYERS, TypeUtils.cListToString(mServerOrderList));
        	            }

        	            @Override
        	            public void doTimerCheckWork() {
        	            }
        	        };
        	        mServerTimerCheck.start(Constant.GAME_TIME, 1000);
        		}
    			//退出上个计时进程
        		if(mLocalTimerCheck != null)
        			mLocalTimerCheck.exit();
    			//设定游戏时间
    			mLocalTimerCheck = new TimerUtils() {
    	            @Override
    	            public void doTimeOutWork() {
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
        		msgsAdapter = new MsgsAdapter(DrawGuessActivity.this, mLocalMsgsList);
        		mLvMsg.setAdapter(msgsAdapter);
            	//设置积分表格
        		scoresAdapter = new ScoresAdapter(DrawGuessActivity.this, mLocalScoresList);
        		mLvScore.setAdapter(scoresAdapter);
                scoresAdapter.notifyDataSetChanged();
            	break;
            }
            case MSGConst.ANS_GAME_OVER:{
            	showCustomToast("房主退出了游戏，断开连接");
	        	finish();
            	break;
            }
            case MSGConst.ANS_OFFLINE:{
            	String name = msg.getData().getString("offline");
            	showCustomToast(name + "离开了游戏");
            	break;
            }
            case MSGConst.ANS_GUESS_TRUE:{
            	//如果是自己猜对的，隐藏猜词框
            	if(SessionUtils.getOrder() == 1){
        			mLayoutChatEdit.setVisibility(View.GONE);
            	}
            	refreshScoreAdapter();
            	refreshMsgAdapter();
            	break;
            }
            case MSGConst.ANS_GUESS_FALSE:{
            	refreshMsgAdapter();
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
