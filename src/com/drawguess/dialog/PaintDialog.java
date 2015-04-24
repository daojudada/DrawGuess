package com.drawguess.dialog;

import com.drawguess.R;
import com.drawguess.interfaces.PaintChangedListener;
import com.drawguess.view.ShowPaintView;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 笔刷
 * @author GuoJun
 *
 */
public class PaintDialog extends Dialog {
	
	private ShowPaintView showPaintView;
	private SeekBar seekAlpha,seekWidth;
	private TextView textAlpha,textWidth;
	private RadioGroup paintStyleGroup;
    private RadioButton mPencil,mBrush,mCrany,mRelief;
	private int paintWidth;
	private int paintAlpha;
	private int paintStyle;
	private int paintColor;
	private PaintChangedListener mListener;

	/**
	 * 构造对话框
	 * @param context 上下文
	 * @param paintW 笔刷宽度
	 * @param paintA 笔刷透明度
	 * @param paintS 笔刷样式
	 * @param listener 监听
	 */
    public PaintDialog(Context context, int paintW, int paintA, int paintS, int paintC, PaintChangedListener listener) {
        super(context);
        paintWidth = paintW;
        paintAlpha = paintA;
        paintStyle = paintS;
        paintColor = paintC;
        this.mListener = listener;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);      
        
		setContentView(R.layout.dialog_paint);
        
		paintStyleGroup = (RadioGroup)findViewById(R.id.paint_style_group);
		mPencil = (RadioButton)findViewById(R.id.paint_style_pencil);
		mBrush = (RadioButton)findViewById(R.id.paint_style_brush);
		mCrany = (RadioButton)findViewById(R.id.paint_style_crany);
		mRelief = (RadioButton)findViewById(R.id.paint_style_relief);
		
		switch (paintStyle) {
        case 0:
        	paintStyleGroup.check(mPencil.getId());
			break;
        case 2:
        	paintStyleGroup.check(mBrush.getId());
			break;
        case 3:
        	paintStyleGroup.check(mCrany.getId());
			break;
        case 1:
        	paintStyleGroup.check(mRelief.getId());
			break;
        default:
        	break;
        }
		
		paintStyleGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				/**
				 * 选的和ID对不上，找不到原因，FUCK the BUG
				 */
		        switch (checkedId) {
	            case R.id.paint_style_pencil:
	            	paintStyle = 0;
	            	break;
	            case R.id.paint_style_brush:
	            	paintStyle = 2;
	            	break;
	            case R.id.paint_style_crany:
	            	paintStyle = 3;
	            	break;
	            case R.id.paint_style_relief:
	            	paintStyle = 1;
					break;
	            default:
	            	break;
		        }
            	mListener.paintChanged(paintWidth, paintAlpha, paintStyle);
				showPaintView.setPaint(paintWidth, paintAlpha, paintStyle, paintColor);	
			}
			
		});

        showPaintView = (ShowPaintView)findViewById(R.id.showpaintview);
        showPaintView.init();
		showPaintView.setPaint(paintWidth, paintAlpha, paintStyle, paintColor);
		
        
        seekAlpha = (SeekBar)findViewById(R.id.seekalpha);
		seekWidth = (SeekBar)findViewById(R.id.seekwidth);

		seekAlpha.setProgress(paintAlpha);
		seekWidth.setProgress(paintWidth);
		textAlpha = (TextView)findViewById(R.id.paint_alpha);
		textWidth = (TextView)findViewById(R.id.paint_width);
		textAlpha.setText(""+paintAlpha);
		textWidth.setText(""+paintWidth);

		
		seekAlpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textAlpha.setText(""+progress);
				paintAlpha = progress;
				mListener.paintChanged(paintWidth, paintAlpha, paintStyle);
				showPaintView.setPaint(paintWidth, paintAlpha, paintStyle, paintColor);
			}
		});
		
		seekWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textWidth.setText(""+progress);
				paintWidth = progress;
				mListener.paintChanged(paintWidth, paintAlpha, paintStyle);
				showPaintView.setPaint(paintWidth, paintAlpha, paintStyle, paintColor);
			}
		});
        
    }
	
    
}

