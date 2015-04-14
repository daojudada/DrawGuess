package com.drawguess.drawop;

import android.graphics.Paint;
import android.graphics.Path;
/**
 * 绘制路径基本事务
 * @author GuoJun
 *
 */
public class OpDraw extends Operation{
	
	public enum Shape{FILL,FREE,LINE,RECT,OVAL} 
	private Path path;
	private Paint paint;
	private boolean isDraw;
	
	public OpDraw(Path path,Paint paint) {
		type = Op.DRAW;
		this.path = path;
		this.paint = new Paint(paint);
		isDraw = true;
	}
	
	public OpDraw(OpDraw opd) {
		type = Op.DRAW;
		this.path = opd.path;
		this.paint = opd.paint;
	}

	public Path getPath()
	{
		return path;
	}
	
	public Paint getPaint()
	{
		return paint;
	}
	
	public void setPath(Path p)
	{
		path=p;
	}
	
	public void setIsDraw(boolean b)
	{
		isDraw = b;
	}
	
	public boolean getIsDraw()
	{
		 return isDraw;
	}
	
	public void setPaint(Paint p)
	{
		paint = p;
	}
	
	public void draw(){
		if(isDraw)
			canvas.drawPath(path,paint);
	}


	@Override
	public void Undo() {
		opManage.popDraw();
	}

	@Override
	public void Redo() {
		opManage.pushDraw(this);
	}
	
}
