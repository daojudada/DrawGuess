package com.drawguess.dialog;

import com.drawguess.R;
import com.drawguess.base.Constant;
import com.drawguess.interfaces.ColorChangedListener;
import com.drawguess.interfaces.PaintChangedListener;

import android.R.color;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 笔刷设置
 * @author GuoJun
 *
 */
public class PaintDialog extends Dialog {
	
	Context context;
	private int paintWidth;
	private int paintAlpha;
	private int paintStyle;
	private PaintChangedListener mListener;

	/**
	 * 构造对话框
	 * @param context 上下文
	 * @param paintW 笔刷宽度
	 * @param paintA 笔刷透明度
	 * @param paintS 笔刷样式
	 * @param listener 监听
	 */
    public PaintDialog(Context context, int paintW, int paintA, int paintS, PaintChangedListener listener) {
        super(context);
        this.context = context;
        paintWidth = paintW;
        paintAlpha = paintA;
        paintStyle = paintS;
    }
    
	public PaintChangedListener getmListener() {
		return mListener;
	}
	
	public void setmListener(PaintChangedListener mListener) {
		this.mListener = mListener;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_paint);
        Window dialogWindow = this.getWindow();          
        dialogWindow.setGravity(Gravity.CENTER);       
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
	
    
}

