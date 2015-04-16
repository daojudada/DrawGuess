package com.drawguess.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Iterator;

import com.drawguess.base.Constant;
import com.drawguess.drawop.OpDraw.Shape;
import com.drawguess.drawop.OperationManage.DrawMode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.drawguess.drawop.*;
import com.drawguess.util.LogUtils;

/**
 * 画板View，实现绘图的基本功能
 * @author GuoJun
 *
 */
public class DrawView extends View
{
	public enum DrawState{Canvas,Draw,Path}
	private final static String TAG = "DrawView";
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
	
	private OpTrans opTrans;
	private Paint paint;
	private int paintWidth;
	private Path path;
	private PaintFlagsDrawFilter pfd;
	private float px,py;
	private float q1,q2;////图元模式的旋转角
	private Shape shape;
	private long startTime;  
	
	private float suol=1,suols=1;//缩放比例;
	private int wx,hy;//图像大小
	
	public DrawView(Context context,AttributeSet attrs) 
	{
		super(context,attrs);
		
		ds = DrawState.Draw;
		paintWidth = 5;
		EraseWidth = paintWidth+20; 
		isMove = false;
		

		wx = Constant.WIN_X ;
		hy = (int) ( Constant.WIN_Y - 40 *  Constant.DENSITY);
		
		path=new Path();
		cacheCanvas=new Canvas();
		pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
		
		opManage=new OperationManage();
		opTrans = null;
		shape = Shape.FREE;
		
		moveX=0;
		moveY=0;
		suol=1;suols=1;//缩放比例
		
		initPaint();
		initBitmap();
	}
	

	  
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) 
	{
		float x=event.getX(0)/suol;//画图时的坐标转换
		float y=event.getY(0)/suol;
		
		switch (ds) 
		{
		case Draw://绘图模式
			OpDraw opDraw = null;	
			switch (event.getAction() & MotionEvent.ACTION_MASK) 
			{
			case MotionEvent.ACTION_DOWN:
				//删掉列表中当前项后面的数据……
				mode = 1;

				//缓存位图
				saveCacheBitmap();
				
				isFirstMove = true;
				startTime = System.nanoTime();  //開始時間
				
				switch (shape) 
				{
				case FILL:
					OpFill opFill = new OpFill((int)(x-moveX),(int)(y-moveY),getPaintColor());
					opFill.Redo();
					opManage.pushOp(opFill);
					//this.invalidate();
					this.invalidate();
					break;
				default:
					break;
				}
				
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				//设置为双点模式
				mode = 2;
				
				l=(float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));

				mX=x;
				mY=y;
				
				//取消Draw操作
				if(!isFirstMove){
					opManage.popOp();
					opManage.popDraw();
					this.invalidate();
				}

				break;
			case MotionEvent.ACTION_MOVE:
				isMove = true;
				if(mode == 1)
				{
					if(isFirstMove){
						switch (shape) 
						{
						case FREE:
							path.moveTo(x-moveX, y-moveY);
							px=x-moveX;
							py=y-moveY;
							opDraw = new OpDraw(path, paint);
							opDraw.Redo();
							opManage.pushOp(opDraw);
							break;
						case LINE:
							path.moveTo(x-moveX, y-moveY);
							px=x-moveX;
							py=y-moveY;
							opDraw = new OpDraw(path, paint);
							opDraw.Redo();
							opManage.pushOp(opDraw);
							break;
						case RECT:
							path.moveTo(x-moveX, y-moveY);
							px=x-moveX;
							py=y-moveY;
							opDraw = new OpDraw(path, paint);
							opDraw.Redo();
							opManage.pushOp(opDraw);
							break;
						case OVAL:
							path.moveTo(x-moveX, y-moveY);
							px=x-moveX;
							py=y-moveY;
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
							path.quadTo(px,py,((x-moveX)+px)/2, ((y-moveY)+py)/2);
							px=x-moveX;
							py=y-moveY;
							break;
						case LINE:
							path.reset();
							path.moveTo(px, py);
							path.lineTo(x-moveX, y-moveY);
							break;
						case RECT://矩形
							path.reset();
							path.moveTo(px, py);
							RectF rectf1=new RectF(px,py,x-moveX,y-moveY);
							path.addRect(rectf1, Direction.CW);
							break;
						case OVAL://圆形
							path.reset();
							path.moveTo(px, py);
							RectF rectf2=new RectF(px,py,x-moveX,y-moveY);
							path.addOval(rectf2, Direction.CW);
							break;
						
						default:
							break;
						}
					}
				}
				else if(mode == 2){
					ls=(float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
					suol=ls/l*suols;	

					moveX+=(x-mX);
					moveY+=(y-mY);
					mX=x;
					mY=y;	
					
				}

				this.invalidate();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				//设置为单点模式
				mode = 1;
				suols*=ls/l;
				
				break;
			case MotionEvent.ACTION_UP:
				
				mode=0;
				
				
				long endTime = System.nanoTime();
				long diffsuTime = endTime - startTime; //消耗時間
				if(diffsuTime > 5e8 &&!isMove)
				{
					moveX = 0;
					moveY = 0;
					suols = 1;
					suol = 1;
					this.invalidate();
				}
				isMove = false;
				
				switch (shape) {
				case FREE:
					path=new Path();
					break;
				case LINE:
					path=new Path();
					break;
				case RECT://矩形
					path=new Path();
					break;
				case OVAL://圆形
					path=new Path();
					break;
				default:
					break;
				}
				break;
				
			}
			break;
			
		case Path://图元操作模式,更改列表数据
			switch (event.getAction() & MotionEvent.ACTION_MASK) 
			{
			case MotionEvent.ACTION_DOWN:
				mX=x;
				mY=y;
				mode+=1;
				if(opTrans == null)
				{
					opTrans = new OpTrans();
					opManage.pushOp(opTrans);
				}
				opTrans = new OpTrans();
				opManage.pushOp(opTrans);

				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//设置为双点模式
				mode+=1;
				
				l1= (float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
				q1=(float) Math.atan2((event.getY(1)-event.getY(0)),(event.getX(1)-event.getX(0)));	
					
				
				break;
			case MotionEvent.ACTION_MOVE:
				if(mode==1)
					opTrans.doMove(x-mX, y-mY);//移动
				else if(mode==2)
				{
					l2= (float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
					q2=(float) Math.atan2((event.getY(1)-event.getY(0)),(event.getX(1)-event.getX(0)));
					opTrans.doScale(l2/l1,(event.getX(0)+event.getX(1))/2,(event.getY(0)+event.getY(1))/2);//缩放l2/l1为缩放比
					opTrans.doRotate((q2-q1),(event.getX(0)+event.getX(1))/2,(event.getY(0)+event.getY(1))/2);//旋转q2-q1为旋转角
					
				}
				
				opTrans.Redo();
				mX=x;
				mY=y;

				this.invalidate();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				//设置为单点模式
				mode = -1;
				
				
				break;
			case MotionEvent.ACTION_UP:
				mode = 0;
				break;
			
			}
			break;
			
		default:
			break;
		}

		return true;
	}
	
	private void drawOp(Operation op)
	{
		switch (op.type) 
		{
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
	 * 返回笔刷透明度
	 * @return paintAlpha
	 */
	public int getPaintAlpha()
	{
		return paint.getAlpha();
	}


	
	/**
	 * 返回笔刷颜色
	 * @return paintWidth
	 */
	public int getPaintColor()
	{
		return paint.getColor();
	}
	
	/**
	 * 返回笔刷宽度
	 * @return paintWidth
	 */
	public int getPaintWidth()
	{
		return (int) paint.getStrokeWidth();
	}
	
	
	
	private void initBitmap()
	{
		cacheBitmap= Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
		cacheCanvas.setBitmap(cacheBitmap);
		Operation.setPro(cacheCanvas, cacheBitmap, opManage);
		saveCacheBitmap();
	}
	
	
	
	
/**..................................................................................................................................................**/	
	
	public void initPaint()
	{
		bmpPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		bmpPaint.setAntiAlias(true); 
		
		paint=new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setStrokeWidth(paintWidth);
		paint.setAlpha(255);
		paint.setAntiAlias(true); 
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);  
		paint.setStrokeCap(Paint.Cap.ROUND);  
	}
	
	
	@Override
	public void onDraw(Canvas canvas){
		try
        {
			canvas.setDrawFilter(pfd);
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
				
				canvas.scale(suol, suol);
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);//清屏
				canvas.drawColor(Color.rgb(132, 132, 132));
				canvas.drawBitmap(cacheBitmap,moveX,moveY,bmpPaint);
			}
			else if(opManage.getMode() == DrawMode.ADD)
			{
				cacheCanvas.drawColor(Color.WHITE);
				cacheCanvas.drawBitmap(earlyBitmap,0,0,bmpPaint);
				Operation.setPro(cacheCanvas, cacheBitmap, opManage);
				
				Operation op= opManage.getDrawLast();
				if(op!=null)
				{
					drawOp(op);
				}
				
				canvas.scale(suol, suol);
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);//清屏
				canvas.drawColor(Color.rgb(132, 132, 132));
				canvas.drawBitmap(cacheBitmap,moveX,moveY,bmpPaint);
				
			}
        }
		catch (Exception e) {
            e.printStackTrace();
            LogUtils.i(TAG, "onDraw wrong");
        }
	}
	
	private void saveCacheBitmap(){
		earlyBitmap = cacheBitmap.copy(Config.ARGB_8888, true);
	}
	
	/**
	 * 清空画布
	 */
	public void setClear()
	{
		opManage.clear();
		initBitmap();
		this.invalidate();
	}
	
	/**
	 * 设置颜色
	 */
	public void setColor(int color)
	{
		paint.setColor(color);
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
			this.invalidate();
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
			this.invalidate();
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
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
			paint.setStrokeWidth(EraseWidth);
		}
		else 
		{
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_OVER));
			paint.setStrokeWidth(paintWidth);
		}
	}
	
	/**
	 * 锁定画布
	 */
	public boolean setLock()
	{
		if(ds != DrawState.Canvas)
		{
			ds = DrawState.Canvas;
			return true;
		}
		else 
		{
			ds = DrawState.Draw;
			return false;
		}
	}
	
	/**
	 * 设置填充模式
	 */
	public void setPack()
	{
		if(shape != Shape.FILL)
			shape = Shape.FILL;
		else
			shape = Shape.FREE;
			
	}
	
	/**
	 * 设置画笔透明度
	 */
	public void setPaintAlpha(int alpha)
	{
		paint.setAlpha(alpha);
	}
	
	/**
	 * 设置画笔宽度
	 */
	public void setPaintWidth(int width)
	{
		paintWidth = width;
		EraseWidth = paintWidth + 20;
		paint.setStrokeWidth(width);
	}
	
	/**
	 * 设置redo
	 */
	public void setRedo()
	{
		opManage.setMode(DrawMode.RE);
		initBitmap();
		opManage.redo();
		this.invalidate();
	}
	
	/**
	 * 保存
	 */
	public void setSave()
	{
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置形状类型
	 */
	public void setShape(Shape shape)
	{
		this.shape=shape;
	}
	
	/**
	 * 几何变换
	 */
	public boolean setTrans()
	{
		if(ds != DrawState.Path)
		{
			ds = DrawState.Path;
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
	public void setUndo()
	{
		opManage.setMode(DrawMode.RE);
		initBitmap();
		opManage.undo();
		this.invalidate();
	}

}

