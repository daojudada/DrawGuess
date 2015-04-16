package com.drawguess.interfaces;



/**
 * 笔刷设置回调接口
 */
public interface PaintChangedListener {
	/**
	 * 笔刷设置回调函数
	 * @param shape
	 */
    void paintChanged(int width,int alpha,int style);
}
