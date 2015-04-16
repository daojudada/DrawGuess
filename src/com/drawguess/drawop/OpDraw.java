package com.drawguess.drawop;

import android.graphics.Paint;
import android.graphics.Path;
/**
 * 绘制路径基本事务
 * @author GuoJun
 *
 */
public class OpDraw extends Operation{
	
	public enum Shape{FILL,FREE,LINE,OVAL,RECT} 
	private boolean isDraw;
	private Paint paint;
	private Path path;
	
	public OpDraw(OpDraw opd) {
		type = Op.DRAW;
		this.path = opd.path;
		this.paint = opd.paint;
	}
	
	public OpDraw(Path path,Paint paint) {
		type = Op.DRAW;
		this.path = path;
		this.paint = new Paint(paint);
		isDraw = true;
	}

	public void draw(){
		if(isDraw)
			canvas.drawPath(path,paint);
	}
	
	public boolean getIsDraw()
	{
		 return isDraw;
	}
	
	public Paint getPaint()
	{
		return paint;
	}
	
	public Path getPath()
	{
		return path;
	}
	
	@Override
	public void Redo() {
		opManage.pushDraw(this);
	}
	
	public void setIsDraw(boolean b)
	{
		isDraw = b;
	}
	
	public void setPaint(Paint p)
	{
		paint = p;
	}


	public void setPath(Path p)
	{
		path=p;
	}

	@Override
	public void Undo() {
		opManage.popDraw();
	}
	
}
