package com.drawguess.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Iterator;

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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.drawguess.drawop.*;

/**
 * 画板View，实现绘图的基本功能
 * @author GuoJun
 *
 */
public class DrawView extends SurfaceView implements  SurfaceHolder.Callback
{
	private int wx,hy;//图像大小
	private int win_x,win_y;//屏幕大小
	private int mode=0;//触摸点数
	private int paintWidth;
	private int EraseWidth;
	
	private boolean isMove;
	private float px,py;
	private float moveX,moveY,mX,mY;
	private float l=0,ls=0;//两点的初始距�?
	private float suol=1,suols=1;//缩放比例
	private float l1=1,l2=1;//图元模式的缩放比
	private float q1,q2;////图元模式的旋转角
	
	private long startTime;
	
	private Path path;
	private Paint paint;
	private Paint bmpPaint;
	private Bitmap cacheBitmap,backBitmap;
	private Canvas cacheCanvas;
	private Shape shape;
	private OpTrans opTrans;
	private SurfaceHolder holder;
	private OperationManage opManage;
	private PaintFlagsDrawFilter pfd;  
	
	public enum DrawState{Draw,Canvas,Path};
	private DrawState ds;
	
	public DrawView(Context context,AttributeSet attrs) 
	{
		super(context,attrs);
		
		ds = DrawState.Draw;
		paintWidth = 5;
		EraseWidth = paintWidth+20; 
		isMove = false;
		
	      

		holder = this.getHolder();  
        holder.addCallback(this);
        
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

	}
	
	public void initBitmap()
	{
		cacheBitmap= Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
		backBitmap=Bitmap.createBitmap(wx, hy, Config.RGB_565);
		
		
		cacheCanvas.setBitmap(backBitmap);//
		cacheCanvas.drawRGB(255, 255, 255);
		cacheCanvas.setBitmap(cacheBitmap);
		
		Operation.setPro(cacheCanvas, cacheBitmap, opManage);
	}
	
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
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		win_x = widthMeasureSpec;
		win_y = heightMeasureSpec;
		
		wx=win_x;
		hy=win_y;
		
		initBitmap();
		new DrawThread().start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
	}
	
	class DrawThread extends Thread{
		@Override
		public void run() {
			super.run();
			Draw();
		}
	}
	
	protected void Draw() 
	{
		Canvas canvas = null;
		try
        {
            synchronized (holder)
            {
				canvas=holder.lockCanvas();
				canvas.setDrawFilter(pfd);
				if(opManage.getMode() == DrawMode.RE)
				{
					opManage.setMode(DrawMode.ADD);
					cacheBitmap=Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
					cacheCanvas.setDrawFilter(pfd);
					cacheCanvas.setBitmap(cacheBitmap);
					Operation.setPro(cacheCanvas, cacheBitmap, opManage);
					
					Iterator<Operation> i = opManage.getDrawIterator();
					while(i.hasNext())
					{
						Operation op= i.next();
						drawOp(op);
						
					}
				}
				else if(opManage.getMode() == DrawMode.ADD)
				{
					cacheCanvas.setDrawFilter(pfd);
					canvas.drawBitmap(cacheBitmap,moveX,moveY,bmpPaint);
					Operation.setPro(cacheCanvas, cacheBitmap, opManage);
					Operation op= opManage.getDrawLast();
					if(op!=null)
					{
						drawOp(op);
					}
				}
				else if(opManage.getMode() == DrawMode.FILL)
				{
					opManage.setMode(DrawMode.ADD);
					cacheBitmap=Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
					cacheCanvas.setBitmap(cacheBitmap);
					Operation.setPro(cacheCanvas, cacheBitmap, opManage);
					
					Iterator<Operation> i = opManage.getDrawIterator();
					while(i.hasNext())
					{
						Operation op= i.next();
						drawOp(op);
						
					}
					
					Operation.setPro(cacheCanvas, cacheBitmap, opManage);
					cacheBitmap=Bitmap.createBitmap(wx, hy, Config.ARGB_8888);
					cacheCanvas.setBitmap(cacheBitmap);
					Operation.setPro(cacheCanvas, cacheBitmap, opManage);
					
					Iterator<Operation> it = opManage.getDrawIterator();
					while(it.hasNext())
					{
						Operation op= it.next();
						drawOp(op);
						
					}
				}
				
				canvas.scale(suol, suol);
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);//清屏
				canvas.drawColor(Color.rgb(132, 132, 132));
				canvas.drawBitmap(backBitmap, moveX,moveY,bmpPaint);
				canvas.drawBitmap(cacheBitmap,moveX,moveY,bmpPaint);
            }
        }
		catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            if(canvas!= null)
            {
            	holder.unlockCanvasAndPost(canvas);
            	holder.lockCanvas(new Rect(0, 0, 0, 0));
            	holder.unlockCanvasAndPost(canvas);
            }
        }
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
	
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) 
	{
		float x=event.getX(0)/suol;//画图时的坐标转换
		float y=event.getY(0)/suol;//???
		
		switch (ds) 
		{
		case Draw://绘图模式
			OpDraw opDraw = null;	
			switch (event.getAction()) 
			{
			case MotionEvent.ACTION_DOWN:
				//删掉列表中当前项后面的数据……
				
				
				
				switch (shape) 
				{
				case FILL:
					OpFill opFill = new OpFill((int)(x-moveX),(int)(y-moveY),getPaintColor());
					opFill.Redo();
					opManage.pushOp(opFill);
					break;
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

				new DrawThread().start();
				break;
			case MotionEvent.ACTION_MOVE:
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
					opManage.setMode(DrawMode.RE);
					break;
				case RECT://矩形
					path.reset();
					path.moveTo(px, py);
					RectF rectf1=new RectF(px,py,x-moveX,y-moveY);
					path.addRect(rectf1, Direction.CW);
					opManage.setMode(DrawMode.RE);
					break;
				case OVAL://圆形
					path.reset();
					path.moveTo(px, py);
					RectF rectf2=new RectF(px,py,x-moveX,y-moveY);
					path.addOval(rectf2, Direction.CW);
					opManage.setMode(DrawMode.RE);
					break;
				
				default:
					break;
				}

				new DrawThread().start();
				break;
			case MotionEvent.ACTION_UP:
				
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
			
		case Canvas://画布操作模式
			switch (event.getAction() & MotionEvent.ACTION_MASK) 
			{
			case MotionEvent.ACTION_DOWN:
				startTime = System.nanoTime();  //開始時間
				 
				mX=x;
				mY=y;
				mode+=1;
				
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//设置为双点模式
				mode+=1;
				
				l=(float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
					
					
				
				break;
			case MotionEvent.ACTION_MOVE:
				if(mode==2)
				{
					ls=(float) Math.sqrt((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)));
					suol=ls/l*suols;		
				}

				isMove = true;
				moveX+=(x-mX);
				moveY+=(y-mY);
				mX=x;
				mY=y;	
				

				new DrawThread().start();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				//设置为单点模式
				mode-=1;
				suols*=ls/l;
				
				break;
			case MotionEvent.ACTION_UP:
				long endTime = System.nanoTime();
				long diffsuTime = endTime - startTime; //消耗時間
				if(diffsuTime > 5e8 &&!isMove)
				{
					moveX = 0;
					moveY = 0;
					suols = 1;
					suol = 1;
					new DrawThread().start();
				}
				isMove = false;
				mode=0;
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
				opManage.setMode(DrawMode.RE);

				new DrawThread().start();
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
		}

		return true;
	}
	
	
	
	
/**..................................................................................................................................................**/	
	
	/**
	 * 清空画布
	 */
	public void setClear()
	{
		opManage.clear();
		initBitmap();
		new DrawThread().start();
	}
	
	
	/**
	 * 复制路径
	 */
	public void setCopy()
	{
		if(opManage.getNowDraw()!=null){
			OpCopy opCopy = new OpCopy();
			opManage.pushOp(opCopy);
			opCopy.Redo();
			new DrawThread().start();
		}
	}
	
	/**
	 * 删除路径
	 */
	public void setDelete()
	{
		if(opManage.getNowDraw()!=null){
			opManage.setMode(DrawMode.RE);
			OpDelete opDelete = new OpDelete();
			opManage.pushOp(opDelete);
			opDelete.Redo();
			new DrawThread().start();
		}
	}
	
	/**
	 * 设置redo
	 */
	public void setRedo()
	{
		opManage.setMode(DrawMode.RE);
		opManage.redo();
		new DrawThread().start();
	}
	
	/**
	 * 设置undo
	 */
	public void setUndo()
	{
		opManage.setMode(DrawMode.RE);
		opManage.undo();
		new DrawThread().start();
	}
	
	/**
	 * 设置形状类型
	 */
	public void setShape(Shape shape)
	{
		this.shape=shape;
	}
	
	/**
	 * 设置颜色
	 */
	public void setColor(int color)
	{
		paint.setColor(color);
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
	 * 设置画笔透明度
	 */
	public void setPaintAlpha(int alpha)
	{
		paint.setAlpha(alpha);
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
	
	/**
	 * 返回笔刷透明度
	 * @return paintAlpha
	 */
	public int getPaintAlpha()
	{
		return paint.getAlpha();
	}

	
}
