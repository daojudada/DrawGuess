package com.drawguess.activity;


import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drawguess.R;
import com.drawguess.base.ActivitiesManager;
import com.drawguess.base.BaseActivity;
import com.drawguess.dialog.ColorDialog;
import com.drawguess.dialog.PaintDialog;
import com.drawguess.dialog.ShapeDialog;
import com.drawguess.drawop.OpDraw.Shape;
import com.drawguess.interfaces.ColorChangedListener;
import com.drawguess.interfaces.MSGListener;
import com.drawguess.interfaces.PaintChangedListener;
import com.drawguess.interfaces.ShapeChangedListener;
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
	private static final String TAG = "DrawTabActivity";
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
    private ImageButton mIbErase,mIbColor,mIbPaint,mIbFill,mIbShape,mIbRedo,mIbUndo,mIbMenu,mIbEdit;
    private LinearLayout mLayoutChat,mLayoutResult;
    private FrameLayout mLayoutDraw;
    private LinearLayout mLayoutChatTab,mLayoutDrawButton;
    private View mVDraw,mVChat,mVResult;

    private ArrayList<String> orderList;

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
		
        //得到游戏顺序链表
		if(NetManage.getState() == 2){
			//服务器获得游戏顺序
			orderList = this.getIntent().getExtras().getStringArrayList("order");
			mDrawView.setOrderList(orderList);
			//创建服务器消息处理回调
			createServerListener();
		}
		else{
			//创建客户端消息处理回调
			createClientListener();
		}
		
		
		//如果自己是链表第一个
		if(SessionUtils.getOrder() == 1){
			mLayoutChatTab.setVisibility(View.GONE);
	    	mLayoutDrawButton.setVisibility(View.VISIBLE);
		}
		else{
			mLayoutChatTab.setVisibility(View.VISIBLE);
	    	mLayoutDrawButton.setVisibility(View.GONE);
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
	}
	
	@Override
    protected void initViews() {
		mDrawView = (DrawView)findViewById(R.id.drawview);
    	mLayoutDraw = (FrameLayout)findViewById(R.id.drawlayout);
    	mLayoutChat = (LinearLayout)findViewById(R.id.chatlayout);
    	mLayoutResult = (LinearLayout)findViewById(R.id.resultlayout);
    	
    	mLayoutChatTab = (LinearLayout)findViewById(R.id.edit_text_layout);
    	
    	mLayoutDrawButton = (LinearLayout)findViewById(R.id.draw_button_layout);
    	
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
		

    }
	
	@Override
	public void onClick(View v) {
		int m;
		switch (v.getId()) {
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
			mDrawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.red:
			m=1;
			mDrawView.setPaintColor(0xFFFF0000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.blue:
			m=2;
			mDrawView.setPaintColor(0xFF0000FF);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.green:
			m=3;
			mDrawView.setPaintColor(0xFF00FF00);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.yellow:
			m=4;
			mDrawView.setPaintColor(0xFFFFFF00);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.purple:
			m=5;
			mDrawView.setPaintColor(0xFFFF00FF);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.ching:
			m=6;
			mDrawView.setPaintColor(0xFF00FFFF);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.shape:
			//形状选择,点击后对话框
			new ShapeDialog(this, new ShapeChangedListener() {
				@Override
				public void shapeChanged(int shape) {
					switch(shape)
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
				}});
			break;
		case R.id.fill:
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
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
					mIbColor.setBackgroundResource(R.drawable.btn_color);
					for(int i=0;i<7;i++){
						mBtColors[i].setVisibility(View.VISIBLE);
					}
				}
			}
			break;
		case R.id.erase:
			//复制
			if(isTrans){
				mDrawView.setCopy();
			}
			else{
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
					if(isTrans){
						isTrans=mDrawView.setTrans();
						mIbEdit.setBackgroundResource(R.drawable.btn_edit1);
						mIbErase.setBackgroundResource(R.drawable.btn_erase1);
						mIbColor.setBackgroundResource(R.drawable.btn_color);
						for(int i=0;i<7;i++){
							mBtColors[i].setVisibility(View.VISIBLE);
						}
					}
				}
			}
			break;

		case R.id.edit:
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
				
				mIbErase.setBackgroundResource(R.drawable.btn_copy);
				mIbColor.setBackgroundResource(R.drawable.btn_delete);
				for(int i=0;i<7;i++){
					mBtColors[i].setVisibility(View.INVISIBLE);
				}
			}
			else{
				mIbEdit.setBackgroundResource(R.drawable.btn_edit1);
				mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				mIbColor.setBackgroundResource(R.drawable.btn_color);
				for(int i=0;i<7;i++){
					mBtColors[i].setVisibility(View.VISIBLE);
				}
			}
			break;
		case R.id.colorpick:
			//删除
			if(isTrans){
				mDrawView.setDelete();
			}
			//填充
			else{
				//颜色设置，对话框
				new ColorDialog(this, mDrawView.getPaintColor(), "color", new ColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						mDrawView.setPaintColor(color);
						setColorBack(-1);
					}}
				).show();
			}
			break;
			
		case R.id.paint:
			//画笔设置，对话框
			new PaintDialog(this, mDrawView.getPaintWidth(), mDrawView.getPaintAlpha(), mDrawView.getPaintStyle(),mDrawView.getPaintColor(),
					new PaintChangedListener(){
						@Override
						public void paintChanged(int width, int alpha, int style) {
							mDrawView.setPaintWidth(width);
							mDrawView.setPaintAlpha(alpha);
							mDrawView.setPaintStyle(style);
						}
				}
			).show();
			break;
		case R.id.undo:
			//撤销
			mDrawView.setUndo();
			break;
		case R.id.menu:
			//菜单
			openOptionsMenu();
			break;
		case R.id.redo:
			//取消撤销
			mDrawView.setRedo();
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
			mDrawView.setClear();
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
    	clientListener = new MSGListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
		        switch (command) {
		        case MSGConst.ANS_DRAW:{
		        	DataDraw data = (DataDraw) pMsg.getAddObject();
		        	OP_TYPE op = data.getOpType();
		        	TOUCH_TYPE touch = data.getTouchType();
		        	int data1 = data.getData1();
		        	int data2 = data.getData2();
		        	int data3 = data.getData3();
		        	b.putInt("opType", op.ordinal());
		        	b.putInt("touchType", touch.ordinal());
		        	b.putInt("data1", data1);
		        	b.putInt("data2", data2);
		        	b.putInt("data3", data3);
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
		serverListener = new MSGListener(){
			@Override
			public void processMessage(MSGProtocol pMsg) {
		        android.os.Message msg = new android.os.Message();
		        Bundle b = new Bundle();
				int command = pMsg.getCommandNo();
				switch (command) {
				
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
	
	/**
	 * 主线程处理UI变化
	 *
	 */
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSGConst.ANS_DRAW:{
	        	OP_TYPE op = OP_TYPE.values()[msg.getData().getInt("opType")];
	        	TOUCH_TYPE touch = TOUCH_TYPE.values()[msg.getData().getInt("touchType")];
	        	int data1 =  msg.getData().getInt("data1");
	        	int data2 =  msg.getData().getInt("data2");
	        	int data3 =  msg.getData().getInt("data3");
	        	switch (op){
	        	case DRAW:
	        		
	        		break;
	        	case FILL:
	        		
	        		break;

	        	case TRANS:
	        		
	        		break;
	        	case PACK:
	        		break;
	        	case EDIT:
	        		mDrawView.setTrans();
	        		break;
	        	case ERASE:
					mDrawView.setEraser(data1 == 0 ? true : false);
	        		break;
	        	case COPY:
					mDrawView.setCopy();
	        		break;
	        	case DELETE:
					mDrawView.setDelete();
	        		break;
	        	case REDO:
	    			mDrawView.setRedo();
	        		break;
	        	case UNDO:
	        		mDrawView.setUndo();
	        		break;
	        	case PAINT:
	        		mDrawView.setPaintColor(data1);
	        		mDrawView.setPaintAlpha(data2);
	        		mDrawView.setPaintStyle(data3);
	        		break;
	        	}
	        }
                default:
                    break;
            }
        }
    }


}
