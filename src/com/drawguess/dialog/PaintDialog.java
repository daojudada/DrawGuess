package com.drawguess.dialog;

import com.drawguess.R;
import com.drawguess.interfaces.PaintChangedListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.SeekBar;

/**
 * 笔刷
 * @author GuoJun
 *
 */
public class PaintDialog extends Dialog {
	
	Context context;
	SeekBar seekPaint,seekWidsh;
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
        

        seekPaint=(SeekBar)findViewById(R.id.);
		seekWidth=(SeekBar)findViewById(R.id.seekbar2);
        
    }
	
    
}

