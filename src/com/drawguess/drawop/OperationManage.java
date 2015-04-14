package com.drawguess.drawop;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import com.drawguess.drawop.Operation.Op;



/**
 * 操作管理类
 * @author GuoJun
 *
 */
public class OperationManage {
	
	/**
	 * operation list
	 */
	private LinkedList<Operation> listDraw;
	/**
	 * operation stack
	 */
	private Stack<Operation> stOperation;
	/**
	 * undo operation recycle stack
	 */
	private Stack<Operation> stRecycle;
	
	/**
	 * now draw stack
	 */
	private Stack<OpDraw> stNowDraw;

	
	public enum DrawMode{RE,ADD,FILL};
	/**
	 * 绘图模式
	 * RE：重绘画布
	 * ADD：增量绘制
	 * NOW：重绘当前路径
	 */
	private DrawMode mode;
	
	public OperationManage() {
		listDraw = new LinkedList<Operation>();
		stOperation = new Stack<Operation>();
		stRecycle = new Stack<Operation>();
		stNowDraw = new Stack<OpDraw>();
		mode = DrawMode.RE;
	}
	
	/**
	 * push in list
	 * @param op
	 */
	public void pushOp(Operation op)
	{
		stOperation.push(op);
	}
	
	/**
	 * pop out list
	 * @param op
	 * @return stOperation.Last
	 */
	public Operation popOp()
	{
		Operation op = stOperation.pop();
		return op;
	}
	
	/**
	 * push in listDraw
	 * @param listDraw
	 */
	public void pushDraw(OpDraw op)
	{
		listDraw.add(op);
		stNowDraw.push(op);
	}
	
	/**
	 * push in listDraw
	 * @param listDraw
	 */
	public void pushNowDraw(OpDraw op)
	{
		stNowDraw.push(op);
	}
	
	/**
	 * push in listDraw
	 * @param opFill
	 */
	public void pushFill(OpFill opFill)
	{
		listDraw.add(opFill);
	}
	
	/**
	 * pop out listDraw
	 * @param op
	 * @return stOperation.Last
	 */
	public Operation popDraw()
	{
		Operation op = listDraw.getLast();
		listDraw.removeLast();
		stNowDraw.pop();
		return op;
	}

	public Operation popNowDraw()
	{
		return stNowDraw.pop();
	}
	
	/**
	 * pop out opFill
	 * @param op
	 * @return stOperation.Last
	 */
	public Operation popFill()
	{
		Operation op = listDraw.getLast();
		listDraw.removeLast();
		return op;
	}

	
	
	public OpDraw getNowDraw()
	{
		if(stNowDraw.isEmpty())
			return null;
		else
			return stNowDraw.lastElement();
	}
	
	public Operation getDrawLast()
	{
		if(listDraw.isEmpty())
			return null;
		else
			return listDraw.getLast();
	}
	
	
	public Iterator<Operation> getDrawIterator()
	{
		return listDraw.iterator();
	}
	
	/**
	 * 绘图模式
	 */
	public void setMode(DrawMode m)
	{
		mode = m;
	}
	
	public DrawMode getMode()
	{
		return mode;
	}
	
	public void clear()
	{
		stOperation.clear();
		stRecycle.clear();
		stNowDraw.clear();
		listDraw.clear();
		
	}
	
	public int size()
	{
		return stOperation.size();
		
	}
	
	public void redo()
	{
		if(!stRecycle.isEmpty())
		{
			Operation op = stRecycle.pop();
			if(op.getType() == Op.TRANS)
				((OpTrans)op).setIsRedo(true);
			op.Redo();
			pushOp(op);
		}
	}
	
	public void undo()
	{
		if(!stOperation.isEmpty())
		{
			Operation op = popOp();
			op.Undo();
			stRecycle.push(op);
		}
	}

}
