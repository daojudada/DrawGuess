package com.drawguess.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Iterator;

import com.drawguess.activity.DrawGuessActivity;
import com.drawguess.base.Constant;
import com.drawguess.bluetooth.BluetoothService;
import com.drawguess.drawop.OpDraw.Shape;
import com.drawguess.drawop.OperationManage.DrawMode;
import com.drawguess.drawop.*;
import com.drawguess.msgbean.DataDraw;
import com.drawguess.msgbean.DataDraw.OP_TYPE;
import com.drawguess.msgbean.DataDraw.TOUCH_TYPE;
import com.drawguess.net.MSGConst;
import com.drawguess.net.NetManage;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Bitmap.Config;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * 画板View，实现绘图的基本功能
 * @author GuoJun
 *
 */
public class DrawView extends View {
	public enum DrawState{Draw,Trans}
	private final static String TAG = "DrawView";
	private NetManage netManage;
	private BluetoothService btService;
	private Paint bmpPaint;
	private Bitmap cacheBitmap,earlyBitmap;
	private Canvas cacheCanvas;
	
	private DrawState ds;
	private int EraseWidth;
	private boolean isMove,isFirstMove;
	private float l=0,ls=0;//两点的初始距�?
	private float l1=1,l2=1;//图元模式的缩放比
	private int mode=0;//触摸点数
	private float moveX,moveY,mX,mY;
	
	private OperationManage opManage;

	private OpDraw opDraw = null;	
	private OpTrans opTrans = null;
	private Paint paint;
	private int paintColor;
	private int paintWidth;
	private int paintAlpha;
	/**
	 * 笔刷样式
	 * 0  铅笔
	 * 1  画笔
	 * 2  水彩
	 * 3  浮雕
	 */
	private int paintStyle;
	
	private Path path;
	private PaintFlagsDrawFilter pfd;
	private float px,py;
	private float q1,q2;////图元模式的旋转角
	private Shape shape;
	private long startTime;  
	
	private float suol=1,suols=1;//缩放比例;
	private int wx,hy;//图像大小
	
	
	public DrawView(Context context,AttributeSet attrs){
		super(context,attrs);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		
		ds = DrawState.Draw;
		paintWidth = 5;
		paintAlpha = 255;
		paintStyle = 0;
		paintColor = Color.BLACK;
		EraseWidth = paintWidth+20; 
		isMove = false;

		wx = Constant.WIN_X ;
		hy = (int) ( Constant.WIN_Y - 40 *  Constant.DENSITY);
		
		path=new Path();
		cacheCanvas=new Canvas();
		pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
		
		opManage=new OperationManage();
		shape = Shape.FREE;
		
		moveX=0;
		moveY=0;
		suol=1;suols=1;//缩放比例
		
		initPaint();
		initBitmap();
		
		
	}
	

	  
	@Override
	public boolean dispatchTouchEvent(MotionEvent event){
		//如果当前绘图顺序处于首位
		if(SessionUtils.getOrder() == 1){
			float x=event.getX(0);//画图时的坐标转换
			float y=event.getY(0);
			DataDraw data;
			
			switch (ds) {
			case Draw://绘图模式

				//test
    			
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					//发送给服务器
					data = new DataDraw(OP_TYPE.DRAW,TOUCH_TYPE.DOWN1,x/wx,y/hy,-1,-1);
					DrawGuessActivity.logNum++;
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					//当前设备绘制
					x/=suol;
					y/=suol;
					doDraw(TOUCH_TYPE.DOWN1,x,y,-1,-1);
					
					startTime = System.nanoTime();  //開始時間
					
					break;
	
				case MotionEvent.ACTION_POINTER_DOWN:

					//发送给服务器
					data = new DataDraw(OP_TYPE.DRAW,TOUCH_TYPE.DOWN2,-1,-1,-1,-1);
					DrawGuessActivity.logNum++;
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					//当前设备绘制
					x/=suol;
					y/=suol;
					doDraw(TOUCH_TYPE.DOWN2,-1,-1,-1,-1);
					
					l=(float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
					mX=x;
					mY=y;
					
					break;
				case MotionEvent.ACTION_MOVE:
					isMove = true;
					if(mode == 1){
						//发送给服务器
						data = new DataDraw(OP_TYPE.DRAW,TOUCH_TYPE.MOVE,x/wx,y/hy,-1,-1);
						DrawGuessActivity.logNum++;
						if(Constant.CONNECT_WAY)
							netManage.sendToServer(MSGConst.SEND_DRAW, data);
						else
							btService.sendMessage(MSGConst.SEND_DRAW, data);
						//当前设备绘制
						x/=suol;
						y/=suol;
						doDraw(TOUCH_TYPE.MOVE,x,y,-1,-1);
					}
					else{
						x/=suol;
						y/=suol;
						ls=(float) Math.sqrt(
								(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+
								((event.getY(0)-event.getY(1)))*(event.getY(0)-event.getY(1)));
						suol=ls/l*suols;	

						moveX+=(x-mX);
						moveY+=(y-mY);
						mX=x;
						mY=y;
						refreshCanvas();
					}
					
					break;
				case MotionEvent.ACTION_POINTER_UP:
					//发送给服务器
					data = new DataDraw(OP_TYPE.DRAW,TOUCH_TYPE.UP2,-1,-1,-1,-1);
					DrawGuessActivity.logNum++;
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					//当前设备绘制
					x/=suol;
					y/=suol;
					doDraw(TOUCH_TYPE.UP2,-1,-1,-1,-1);
					
					suols*=ls/l;
					
					break;
				case MotionEvent.ACTION_UP:
					//发送给服务器
					data = new DataDraw(OP_TYPE.DRAW,TOUCH_TYPE.UP1,-1,-1,-1,-1);
					DrawGuessActivity.logNum++;
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					//当前设备绘制
					x/=suol;
					y/=suol;
					doDraw(TOUCH_TYPE.UP1,-1,-1,-1,-1);
					
					long endTime = System.nanoTime();
					long diffsuTime = endTime - startTime; //消耗時間
					if(diffsuTime > 5e8 &&!isMove)
					{
						moveX = 0;
						moveY = 0;
						suols = 1;
						suol = 1;
						refreshCanvas();
					}
					isMove = false;
					
					break;
					
				}
				break;
				
			case Trans://图元操作模式,更改列表数据
				float x2,y2;
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.DOWN1,x,y,-1,-1);
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					x/=suol;
					y/=suol;
					doTrans(TOUCH_TYPE.DOWN1, x , y, -1, -1);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					x2 = event.getX(1);
					y2 = event.getY(1);
					data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.DOWN2,x,y,x2,y2);
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					x/=suol;
					y/=suol;
					x2/=suol;
					y2/=suol;
					doTrans(TOUCH_TYPE.DOWN2, x, y, x2, y2);
					break;
				case MotionEvent.ACTION_MOVE:
					if(mode ==1){
						data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.MOVE,x,y,-1,-1);
						if(Constant.CONNECT_WAY)
							netManage.sendToServer(MSGConst.SEND_DRAW, data);
						else
							btService.sendMessage(MSGConst.SEND_DRAW, data);
						x/=suol;
						y/=suol;
						doTrans(TOUCH_TYPE.MOVE, x, y, -1 , -1);
					}
					else if(mode ==2){
						x2 = event.getX(1);
						y2 = event.getY(1);
						data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.MOVE,x,y,x2,y2);
						if(Constant.CONNECT_WAY)
							netManage.sendToServer(MSGConst.SEND_DRAW, data);
						else
							btService.sendMessage(MSGConst.SEND_DRAW, data);
						x/=suol;
						y/=suol;
						x2/=suol;
						y2/=suol;
						doTrans(TOUCH_TYPE.MOVE, x, y, x2, y2);
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.UP2,-1,-1,-1,-1);
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					doTrans(TOUCH_TYPE.UP2, -1,-1,-1,-1);
					break;
				case MotionEvent.ACTION_UP:
					data = new DataDraw(OP_TYPE.TRANS,TOUCH_TYPE.UP1,-1,-1,-1,-1);
					if(Constant.CONNECT_WAY)
						netManage.sendToServer(MSGConst.SEND_DRAW, data);
					else
						btService.sendMessage(MSGConst.SEND_DRAW, data);
					doTrans(TOUCH_TYPE.UP1, -1,-1,-1,-1);
					break;
				
				}
				break;
				
			default:
				break;
			}
		}
		return true;
	}
	
	/**
	 * 在canvas上绘图
	 * @param 绘图基本事务
	 */
	private void drawOp(Operation op){
		switch (op.type) {
		case FILL://填充
			((OpFill)op).draw();
			break;
		case DRAW://画图
			((OpDraw)op).draw();
			break;
		default:
			Log.i("operation Type Wrong", "wrong");
			break;
		}
	}
	

	/**
	 * 初始化位图
	 */
	private void initBitmap(){
		cacheBitmap= Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
		cacheCanvas.setBitmap(cacheBitmap);
		cacheCanvas.drawColor(Color.WHITE);
		Operation.setPro(cacheCanvas, cacheBitmap, opManage);
		saveCacheBitmap();
	}
	
	/**
	 * 初始化笔刷
	 */
	private void initPaint(){
		bmpPaint = new Paint();
		
		paint=new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setStrokeWidth(paintWidth);
		paint.setAlpha(paintAlpha);
		paint.setAntiAlias(true); 
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);  
		paint.setStrokeCap(Paint.Cap.ROUND);  
	}
	

	private void saveCacheBitmap(){
		earlyBitmap = cacheBitmap.copy(Config.ARGB_8888, false);
	}
	
/**.........................public...............................................................................**/
	
	
	@Override
	public void onDraw(Canvas canvas){
		try
        {
			canvas.setDrawFilter(pfd);
			canvas.scale(suol, suol);
			canvas.drawColor(Color.rgb(128, 128, 128));
			canvas.drawBitmap(cacheBitmap,moveX,moveY,bmpPaint);
        }
		catch (Exception e) {
            LogUtils.i(TAG, "onDraw wrong");
        }
	}
	
	/**
	 * 
	 * @return 操作栈的长度
	 */
	public int getOpSize(){
		return opManage.size();
	}
	
	//得到View的宽度
	public int getWX(){
		return wx;
	}
	
	//得到View的长度
	public int getHY(){
		return hy;
	}
	
	/**
	 * 返回笔刷颜色
	 * @return paintWidth
	 */
	public int getPaintColor(){
		return paintColor;
	}
	
	/**
	 * 返回笔刷宽度
	 * @return paintWidth
	 */
	public int getPaintWidth(){
		return paintWidth;
	}
	

	/**
	 * 返回笔刷样式
	 * @return paintStyle
	 */
	public int getPaintStyle(){
		return paintStyle;
	}
	

	/**
	 * 返回笔刷透明度
	 * @return paintAlpha
	 */
	public int getPaintAlpha(){
		return paintAlpha;
	}
	
	/**
	 * 设置网络管理器
	 */
	public void setNetManage(NetManage nm)
	{
		this.netManage = nm;
	}
	
	/**
	 * 设置网络管理器
	 */
	public void setBtService(BluetoothService nm)
	{
		this.btService = nm;
	}
	
	/**
	 * 清空画布c
	 */
	public void setClear()
	{

		suol=1;
		suols=1;
		moveX= 0;
		moveY = 0;
		opManage.clear();
		initBitmap();
		refreshCanvas();
	}
	
	/**
	 * 设置颜色
	 */
	public void setPaintColor(int color)
	{
		paintColor = color;
		paint.setColor(paintColor);
	}
	
	/**
	 * 复制路径
	 */
	public void setCopy()
	{
		//缓存位图
		saveCacheBitmap();
		if(opManage.getNowDraw()!=null){
			OpCopy opCopy = new OpCopy();
			opManage.pushOp(opCopy);
			opCopy.Redo();
			refreshCanvas();
		}
	}
	
	/**
	 * 删除路径
	 */
	public void setDelete()
	{
		if(opManage.getNowDraw()!=null){
			opManage.setMode(DrawMode.RE);
			initBitmap();
			OpDelete opDelete = new OpDelete();
			opManage.pushOp(opDelete);
			opDelete.Redo();
			refreshCanvas();
		}
	}
	
	/**
	 * 设置为橡皮擦
	 */
	public void setEraser(boolean isEraser)
	{
		//判断是否按下
		if(isEraser)
		{
			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(EraseWidth);
		}
		else 
		{
			paint.setColor(paintColor);
			paint.setStrokeWidth(paintWidth);
		}
	}

	
	/**
	 * 设置填充模式
	 */
	public void setPack(){
		if(shape != Shape.FILL)
			shape = Shape.FILL;
		else
			shape = Shape.FREE;
			
	}
	
	/**
	 * 设置画笔透明度
	 */
	public void setPaintAlpha(int alpha){
		paintAlpha = alpha;
		paint.setAlpha(paintAlpha);
	}
	
	/**
	 * 设置画笔宽度
	 */
	public void setPaintWidth(int width){
		paintWidth = width;
		EraseWidth = paintWidth + 20;
		paint.setStrokeWidth(width);
	}
	
	/**
	 * 设置画笔样式
	 * @param style
	 */
	public void setPaintStyle(int style){
		paintStyle = style;
		MaskFilter maskFilter = null;
		switch (style) {
		case 0:
			maskFilter=null;
			paint.setMaskFilter(maskFilter);
			break;
		case 1:
			if(paintWidth<=8&&paintWidth>1)
				maskFilter = new BlurMaskFilter(paintWidth-1, BlurMaskFilter.Blur.SOLID);
			else if(paintWidth<=1)
				maskFilter = null;
			else
				maskFilter = new BlurMaskFilter(8, BlurMaskFilter.Blur.SOLID);
			paint.setMaskFilter(maskFilter);
			break;
		case 2:	
			maskFilter = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
			paint.setMaskFilter(maskFilter);
			break;
		case 3:
			maskFilter =new EmbossMaskFilter(new float[]{1.0f,1.0f,1.0f},0.4f,6,5f);
			paint.setMaskFilter(maskFilter);

		default:
			break;
		}
	}
	
	/**
	 * 设置redo
	 */
	public void setRedo(){
		opManage.setMode(DrawMode.RE);
		initBitmap();
		opManage.redo();
		refreshCanvas();
	}
	
	/**
	 * 保存
	 */
	public void setSave(){
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DrawSomething", DateFormat.SECOND_FIELD+".png");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			cacheBitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * 设置形状类型
	 */
	public void setShape(Shape shape){
		this.shape=shape;
	}
	
	/**
	 * 几何变换
	 */
	public boolean setTrans(){
		if(ds != DrawState.Trans)
		{
			ds = DrawState.Trans;
			return true;
		}
		else 
		{
			ds = DrawState.Draw;
			return false;
		}
	}
	
	/**
	 * 设置undo
	 */
	public void setUndo(){
		opManage.setMode(DrawMode.RE);
		initBitmap();
		opManage.undo();
		refreshCanvas();
	}

	/**
	 * 客户端接受服务器端的数据进行操作
	 * @param touch type
	 * @param x1 附加数据
	 * @param y1 附加数据
	 * @param x2 附加数据
	 * @param y2 附加数据
	 */
	public synchronized  void doOperation(TOUCH_TYPE touch, float x1, float y1, float x2, float y2){
		if(ds == DrawState.Draw)
			doDraw(touch,x1,y1,x2,y2);
		else if(ds == DrawState.Trans)
			doTrans(touch,x1,y1,x2,y2);
	}
	
	private void doDraw(TOUCH_TYPE touch, float x1, float y1, float x2, float y2){
		switch(touch){
		case DOWN1:
			//缓存位图
			mode = 1;
			path = new Path();
			
			saveCacheBitmap();
			
			
			isFirstMove = true;
			if(shape == Shape.FILL){
				OpFill opFill = new OpFill((int)(x1-moveX),(int)(y1-moveY),getPaintColor());
				opFill.Redo();
				opManage.pushOp(opFill);
				refreshCanvas();
			}
			break;
		case DOWN2:
			//设置为双点模式
			mode = 2;
			//取消Draw操作
			if(!isFirstMove){
				opManage.popOp();
				opManage.popDraw();
				refreshCanvas();
			}
			break;
		case MOVE:
			if(mode == 1)
			{
				if(isFirstMove){
					switch (shape) 
					{
					case FREE:
						path.moveTo(x1-moveX, y1-moveY);
						px=x1-moveX;
						py=y1-moveY;
						opDraw = new OpDraw(path, paint);
						opDraw.Redo();
						opManage.pushOp(opDraw);
						break;
					case LINE:
						path.moveTo(x1-moveX, y1-moveY);
						px=x1-moveX;
						py=y1-moveY;
						opDraw = new OpDraw(path, paint);
						opDraw.Redo();
						opManage.pushOp(opDraw);
						break;
					case RECT:
						path.moveTo(x1-moveX, y1-moveY);
						px=x1-moveX;
						py=y1-moveY;
						opDraw = new OpDraw(path, paint);
						opDraw.Redo();
						opManage.pushOp(opDraw);
						break;
					case OVAL:
						path.moveTo(x1-moveX, y1-moveY);
						px=x1-moveX;
						py=y1-moveY;
						opDraw = new OpDraw(path, paint);
						opDraw.Redo();
						opManage.pushOp(opDraw);
						break;
					default:
						break;
					}
					isFirstMove = false;
				}
				else{
					switch (shape) 
					{
					case FREE:
						path.quadTo(px,py,((x1-moveX)+px)/2, ((y1-moveY)+py)/2);
						px=x1-moveX;
						py=y1-moveY;
						break;
					case LINE:
						path.reset();
						path.moveTo(px, py);
						path.lineTo(x1-moveX, y1-moveY);
						break;
					case RECT://矩形
						path.reset();
						path.moveTo(px, py);
						RectF rectf1=new RectF(px,py,x1-moveX,y1-moveY);
						path.addRect(rectf1, Direction.CW);
						break;
					case OVAL://圆形
						path.reset();
						path.moveTo(px, py);
						RectF rectf2=new RectF(px,py,x1-moveX,y1-moveY);
						path.addOval(rectf2, Direction.CW);
						break;
					
					default:
						break;
					}
				}
			}
			else if(mode == 2){	
				
			}
			refreshCanvas();
			break;
		case UP2:
			mode = 1;
			break;
		case UP1:
			mode=0;
			break;
		default:
			break;
		}
		
	}
	
	private void doTrans(TOUCH_TYPE touch, float x1, float y1, float x2, float y2){
		switch(touch){
		case DOWN1:
			mX=x1;
			mY=y1;
			mode = 1;
			opTrans = new OpTrans();
			opManage.pushOp(opTrans);
			break;
		case DOWN2:
			//设置为双点模式
			mode = 2;
			l1= (float) Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
			q1=(float) Math.atan2(y2-y1,x2-x1);	
			break;
		case MOVE:
			if(mode==1)
				opTrans.doMove(x1-mX, y1-mY);//移动
			else if(mode==2)
			{
				l2= (float) Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
				q2=(float) Math.atan2(y2-y1,x2-x1);
				opTrans.doScale(l2/l1,(x1+x2)/2,(y1+y2)/2);//缩放l2/l1为缩放比
				opTrans.doRotate((q2-q1),(x1+x2)/2,(y1+y2)/2);//旋转q2-q1为旋转角
				
			}
			
			opTrans.Redo();
			mX=x1;
			mY=y1;

			
			break;
		case UP2:
			mode = 1;
			break;
		case UP1:
			mode=0;
			break;
		default:
			break;
		}
	}
	
	private void refreshCanvas(){
		cacheCanvas.setDrawFilter(pfd);
		if(opManage.getMode() == DrawMode.RE)
		{
			opManage.setMode(DrawMode.ADD);
			cacheCanvas.drawColor(Color.WHITE);
			Operation.setPro(cacheCanvas, cacheBitmap, opManage);
			
			Iterator<Operation> i = opManage.getDrawIterator();
			while(i.hasNext())
			{
				Operation op= i.next();
				if(op == opManage.getDrawLast())
					saveCacheBitmap();
				drawOp(op);
				
			}
		}
		else if(opManage.getMode() == DrawMode.ADD)
		{

			cacheCanvas.drawBitmap(earlyBitmap,0,0,bmpPaint);
			
			Operation op= opManage.getDrawLast();
			if(op!=null)
			{
				drawOp(op);
			}
			
		}
		if(SessionUtils.getOrder() == 1){
			this.invalidate();
		}
		else{
			this.postInvalidate();
		}
	}
}

