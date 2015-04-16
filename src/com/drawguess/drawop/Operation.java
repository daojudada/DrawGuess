package com.drawguess.drawop;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class Operation {
	public enum Op{COPY,DELETE,DRAW,FILL,TRANS}
	
	protected static Bitmap bitmap;
	protected static Canvas canvas;
	protected static OperationManage opManage;
	
	public static void setPro(Canvas c,Bitmap b,OperationManage o)
	{
		canvas = c;
		bitmap = b;
		opManage = o;
	}
	

	public Op type;
	
	/**
	 * 得到类型
	 * @return type
	 */
	public Op getType()
	{
		return type;
	}
	
	/**
	 * redo操作
	 */
	public abstract void Redo();
	/**
	 * undo操作
	 */
	public abstract void Undo();
	
}
