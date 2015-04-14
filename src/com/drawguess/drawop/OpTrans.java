package com.drawguess.drawop;


import android.graphics.Matrix;
import android.graphics.Path;
/**
 * 几何变换操作事务
 * @author GuoJun
 *
 */
public class OpTrans extends Operation{
	
	private OpDraw opDraw;
	private Path oldPath,newPath;
	private float angel;
	private float scale;
	private Matrix mM,rM,sM;
	private boolean isRedo;
	
	public OpTrans() {
		type = Op.TRANS;
		this.opDraw = opManage.getNowDraw();
		oldPath = new Path(opManage.getNowDraw().getPath());
		newPath = new Path();
		mM=new Matrix();
		rM=new Matrix();
		sM=new Matrix();
		isRedo = false;
		angel = 0;
		scale = 1;
	}
	
	/**
	 * 得到路径
	 * @return
	 */
	public Path getPath()
	{
		return oldPath;
	}
	
	public void init()
	{
		angel = 0;
		scale = 1;
	}
	
	public Operation getDraw()
	{
		return opDraw;
	}
	
	public void setIsRedo(boolean b)
	{
		isRedo = b;
	}
	/**
	 * 平移
	 * @param x
	 * @param y
	 */
	public void doMove(float x,float y)
	{
		mM.setTranslate(x, y);
	}
	
	/**
	 * 旋转
	 * @param angel
	 * @param px
	 * @param py
	 */
	public void doRotate(float angel,float px,float py)
	{
		rM.setRotate((float)((angel-this.angel)/Math.PI*180), px, py);
		this.angel=angel;
	}
	
	/**
	 * 缩放
	 * @param suo
	 * @param sx
	 * @param sy
	 */
	public void doScale(float suo,float sx,float sy)
	{
		sM.setScale(suo/scale, suo/scale,sx,sy);
		scale=suo;
	}
		
	


	@Override
	public void Undo() {
		opDraw.setPath(oldPath);
	}

	@Override
	public void Redo() {
		if(!isRedo)
		{
			Path mPath=new Path();
			mPath.addPath(opDraw.getPath(), mM);
			Path sPath=new Path();
			sPath.addPath(mPath, sM);
			Path rPath=new Path();
			rPath.addPath(sPath, rM);
			
			opDraw.setPath(rPath);
			
			newPath = rPath;
			
			mM=new Matrix();
			rM=new Matrix();
			sM=new Matrix();
		}
		else
			opDraw.setPath(newPath);
	}
}
