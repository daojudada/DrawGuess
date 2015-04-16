package com.drawguess.drawop;

import android.graphics.Paint;
import android.graphics.Path;


/**
 * 复制操作事务
 * @author GuoJun
 *
 */
public class OpCopy extends Operation{
	
	private OpDraw opDraw;
	
	public OpCopy() {
		type = Op.COPY;
		this.opDraw = opManage.getNowDraw();
	}
	
	
	public OpDraw getDraw()
	{
		return opDraw;
	}

	


	@Override
	public void Redo() {
		if(opDraw!=null)
		{
			Path p = new Path();
			Paint pa = new Paint(opDraw.getPaint());
			p.addPath(opDraw.getPath(),40,0);
			OpDraw newOpDraw= new OpDraw(p,pa);
			opDraw = newOpDraw;
			opManage.pushDraw(opDraw);
		}
	}


	@Override
	public void Undo() {
		opManage.popDraw();
	}
}