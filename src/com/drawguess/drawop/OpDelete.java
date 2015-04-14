package com.drawguess.drawop;

/**
 * 删除操作事务
 * @author GuoJun
 *
 */
public class OpDelete extends Operation{
	
	private OpDraw opDraw;
	
	public OpDelete() {
		type = Op.DELETE;
		this.opDraw = opManage.getNowDraw();
	}
	


	@Override
	public void Undo() {
		opDraw.setIsDraw(true);
		opManage.pushNowDraw(opDraw);
	}


	@Override
	public void Redo() {
		opDraw = opManage.getNowDraw();
		if(opDraw!=null)
			opDraw.setIsDraw(false);
		opManage.popNowDraw();
	}
}
