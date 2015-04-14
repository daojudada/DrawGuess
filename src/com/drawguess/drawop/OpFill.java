package com.drawguess.drawop;


import java.util.LinkedList;
import java.util.Stack;
import android.graphics.Point;
/**
 * 填充操作事务
 * @author GuoJun
 *
 */
public class OpFill extends Operation{
	
	private int x;
	private int y;
	private int color;
	private LinkedList<int[]> fillArea;
	private boolean isFill,isFirst;
	

	public OpFill(int x,int y,int color) {
		type = Op.FILL;
		this.x=x;
		this.y=y;
		this.color=color;
		isFill = true;
		isFirst = true;
		fillArea = new LinkedList<int[]>();
	}
	
	public float[] getPosition()
	{
		return new float[]{x,y};
	}
	
	public int getColor()
	{
		return color;
	}
	
	
	
	/**
	 * 扫描线填充算法
	 * @param bitmap
	 */
	private void ScanFill()
	{
		int oldColor = bitmap.getPixel(x, y);
		if(oldColor != color)
		{
			int xl,xr;
			boolean spanNeedFill;
			Point pt = new Point();
			Stack<Point> stack=new Stack<Point>();
			pt.x=x;pt.y=y;
			stack.push(pt);
			while(!stack.isEmpty())
			{
				pt=stack.pop();
				x=pt.x;
				y=pt.y;
				while(bitmap.getPixel(x, y)==oldColor)
				{
					bitmap.setPixel(x, y, color);
					if(x<canvas.getWidth()-1)
						x++;
					else 
						break;
				}
				xr=x-1;
				x=pt.x-1;
				while(bitmap.getPixel(x,y)==oldColor)
				{
					bitmap.setPixel(x, y, color);
					if(x>0)
						x--;
					else 
						break;
				}
				xl=x+1;
				
				fillArea.add(new int[]{xl,xr,y});
				
				//扫描上
				x=xl;
				if(y<canvas.getHeight()-1)
				{
					y=y+1;
					while(x<=xr)
					{
						spanNeedFill=false;
						while(bitmap.getPixel(x, y)==oldColor)
						{
							spanNeedFill=true;

							if(x<canvas.getWidth()-1)
								x++;
							else 
								break;
						}
						if(spanNeedFill)
						{
							pt.x=x-1;
							pt.y=y;
							stack.push(new Point(pt.x,pt.y));
							spanNeedFill=false;
						}
						while(bitmap.getPixel(x, y)!=oldColor && x<=xr)
							if(x<canvas.getWidth()-1)
								x++;
							else 
								break;
					}
				}
				
				//扫描下一行
				x=xl;
				if(y>1)
				{
					y=y-2;
					while(x<=xr)
					{
						spanNeedFill=false;
						while(bitmap.getPixel(x, y)==oldColor)
						{
							spanNeedFill=true;
							if(x<canvas.getWidth()-1)
								x++;
							else 
								break;
						}
						if(spanNeedFill)
						{
							pt.x=x-1;
							pt.y=y;
							stack.push(new Point(pt.x,pt.y));
							spanNeedFill=false;
						}
						while(bitmap.getPixel(x, y)!=oldColor && x<=xr)
							if(x<canvas.getWidth()-1)
								x++;
							else 
								break;
					}
				}
			}
		}
	}

	private void redoFill()
	{
		for(int[] f:fillArea)
		{
			for(int sx = f[0]; sx <= f[1]; sx++)
			{
				bitmap.setPixel(sx, f[2], color);
			}
		}
	}
	
	public void draw()
	{
		if(isFill)
		{
			if(isFirst)
			{
				ScanFill();
				isFirst = false;
			}
			else
			{
				redoFill();
			}
		}
	}


	@Override
	public void Undo() {
		opManage.popFill();
	}

	@Override
	public void Redo() {
		opManage.pushFill(this);
	}
}
