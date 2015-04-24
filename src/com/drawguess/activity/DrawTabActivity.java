package com.drawguess.activity;


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
import com.drawguess.interfaces.PaintChangedListener;
import com.drawguess.interfaces.ShapeChangedListener;
import com.drawguess.view.DrawView;

public class DrawTabActivity extends BaseActivity implements OnClickListener{
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
    /**
     * 是否是服务器
     */
    private Boolean isServer;
	private int[] colorSource = new int[]{
    		R.drawable.btn_black1,R.drawable.btn_red1,R.drawable.btn_blue1,R.drawable.btn_green1,
    		R.drawable.btn_yellow1,R.drawable.btn_purple1,R.drawable.btn_ching1,
    		R.drawable.btn_black2,R.drawable.btn_red2,R.drawable.btn_blue2,R.drawable.btn_green2,
    		R.drawable.btn_yellow2,R.drawable.btn_purple2,R.drawable.btn_ching2
    };
    /**
     * 绘图显示类
     */
    private DrawView drawView;
    private ImageButton[] mBtColors = new ImageButton[7];
    private LinearLayout mDrawTab,mChatTab,mResultTab;
    private ImageButton mIbErase,mIbColor,mIbPaint,mIbFill,mIbShape,mIbRedo,mIbUndo,mIbMenu,mIbEdit;
    private LinearLayout mLayoutChat,mLayoutResult;
    private FrameLayout mLayoutDraw;
    private LinearLayout mLayoutChatTab,mLayoutDrawButton;
    private View mVDraw,mVChat,mVResult;


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
		drawView = (DrawView)findViewById(R.id.drawview);
    	mLayoutDraw = (FrameLayout)findViewById(R.id.drawlayout);
    	mLayoutChat = (LinearLayout)findViewById(R.id.chatlayout);
    	mLayoutResult = (LinearLayout)findViewById(R.id.resultlayout);
    	mLayoutChatTab = (LinearLayout)findViewById(R.id.chattab);
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
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.red:
			m=1;
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.blue:
			m=2;
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.green:
			m=3;
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.yellow:
			m=4;
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.purple:
			m=5;
			drawView.setPaintColor(0xFF000000);
			mBtColors[m].setBackgroundResource(colorSource[m+7]);
			setColorBack(m);
			break;
		case R.id.ching:
			m=6;
			drawView.setPaintColor(0xFF000000);
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
						drawView.setShape(Shape.FREE);
						mIbShape.setBackgroundResource(R.drawable.btn_free);
						break;
					case 1:
						drawView.setShape(Shape.LINE);
						mIbShape.setBackgroundResource(R.drawable.btn_line1);
						break;
					case 2:
						drawView.setShape(Shape.RECT);
						mIbShape.setBackgroundResource(R.drawable.btn_rect1);
						break;
					case 3:
						drawView.setShape(Shape.OVAL);
						mIbShape.setBackgroundResource(R.drawable.btn_oval1);
						break;
					}
				}});
			break;
		case R.id.fill:
			drawView.setPack();
			if(isPack){
				isPack=false;
				mIbFill.setBackgroundResource(R.drawable.btn_fill1);
			}
			else{
				isPack=true;
				mIbFill.setBackgroundResource(R.drawable.btn_fill2);
				if(isEraser){
					isEraser=false;
					drawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				}
				if(isTrans){
					isTrans=drawView.setTrans();
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
				drawView.setCopy();
			}
			else{
				//橡皮擦
				if(isEraser){	
					isEraser=false;
					drawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				}
				else{
					isEraser=true;
					drawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase2);
					
					if(isPack){
						drawView.setPack();
						isPack=false;
						mIbFill.setBackgroundResource(R.drawable.btn_fill1);
					}
					if(isTrans){
						isTrans=drawView.setTrans();
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
			isTrans=drawView.setTrans();
			if(isTrans){
				mIbEdit.setBackgroundResource(R.drawable.btn_edit2);
				if(isEraser){
					isEraser=false;
					drawView.setEraser(isEraser);
					mIbErase.setBackgroundResource(R.drawable.btn_erase1);
				}
				if(isPack){
					drawView.setPack();
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
				drawView.setDelete();
			}
			//填充
			else{
				//颜色设置，对话框
				new ColorDialog(this, drawView.getPaintColor(), "color", new ColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						drawView.setPaintColor(color);
						setColorBack(-1);
					}}
				).show();
			}
			break;
			
		case R.id.paint:
			//画笔设置，对话框
			new PaintDialog(this, drawView.getPaintWidth(), drawView.getPaintAlpha(), drawView.getPaintStyle(),drawView.getPaintColor(),
					new PaintChangedListener(){
						@Override
						public void paintChanged(int width, int alpha, int style) {
							drawView.setPaintWidth(width);
							drawView.setPaintAlpha(alpha);
							drawView.setPaintStyle(style);
						}
				}
			).show();
			break;
		case R.id.undo:
			//撤销
			drawView.setUndo();
			break;
		case R.id.menu:
			//菜单
			openOptionsMenu();
			break;
		case R.id.redo:
			//取消撤销
			drawView.setRedo();
			break;
		default:
			break;
		}
		
	}
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawtabs);
        isServer = true;
        isPack = false;
		isEraser = false;
		isTrans = false;
        initViews();
        initEvents();
        
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
    protected void onDestroy() {
        super.onDestroy();

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
			if(isServer){
				isServer = false;
				mLayoutChatTab.setVisibility(View.GONE);
		    	mLayoutDrawButton.setVisibility(View.GONE);
			}
			else{
				isServer = true;
				mLayoutChatTab.setVisibility(View.VISIBLE);
		    	mLayoutDrawButton.setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			drawView.setClear();
			break;
		case 3:
			drawView.setSave();
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
    

    public static class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                

                default:
                    break;
            }
        }
    }


}
