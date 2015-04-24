package com.drawguess.view;


import com.drawguess.base.Constant;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * 笔画的演示
 * @author GuoJun
 *
 */
public class ShowPaintView extends View{

	private int mHeight;
	private int mWidth;
	private int hX;
	private int hY;
	private Path mPath;
	private Paint mPaint,rectPaint,linePaint;
	
	
	public ShowPaintView(Context context) {
		super(context);
		init();
	}



	public ShowPaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ShowPaintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void init(){
		this.mHeight = (int) (Constant.WIN_Y * 0.15f);
		this.mWidth = (int) (260 * Constant.DENSITY);
		hX = mWidth / 2;
		hY = mHeight / 2;
		setMinimumHeight(mHeight);
		setMinimumWidth(mWidth);
		
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);  
		mPaint.setStrokeCap(Paint.Cap.ROUND);  
		
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.WHITE);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(8);
        linePaint.setColor(0xff04a8e3);
        
		mPath = new Path();
		
		float dx = mWidth / 2 * 0.6f;
		float dy = mHeight /2 ;
		mPath.moveTo(-dx, 0);
		mPath.cubicTo( -dx/3, dy, dx/3,  -dy, dx, 0);
	}
	
	public void setPaint(int w, int a,int s, int c){
        mPaint.setStrokeWidth(w);
        mPaint.setColor(c);
        mPaint.setAlpha(a);
        MaskFilter maskFilter = null;
		switch (s) {
		case 0:
			maskFilter=null;
			mPaint.setMaskFilter(maskFilter);
			break;
		case 1:
			if(w<=1)
				maskFilter = null;
			else if(w<=8&&w>1)
				maskFilter = new BlurMaskFilter(w-1, BlurMaskFilter.Blur.SOLID);
			else
				maskFilter = new BlurMaskFilter((float) (Math.sqrt(w)*3), BlurMaskFilter.Blur.SOLID);
			mPaint.setMaskFilter(maskFilter);
			break;
		case 2:	
			if(w<=1)
				maskFilter = null;
			else if(w<=8&&w>1)
				maskFilter = new BlurMaskFilter(w-1, BlurMaskFilter.Blur.NORMAL);
			else
				maskFilter = new BlurMaskFilter((float) (Math.sqrt(w)*3), BlurMaskFilter.Blur.NORMAL);
			mPaint.setMaskFilter(maskFilter);
			break;
		case 3:
			if(w<=1)
				maskFilter = null;
			else if(w<=8&&w>1)
				maskFilter = new EmbossMaskFilter(new float[]{1.0f,1.0f,1.0f},0.4f,6,w-1);
			else
				maskFilter = new EmbossMaskFilter(new float[]{1.0f,1.0f,1.0f},0.4f,6,(float) (Math.sqrt(w)*3));
			mPaint.setMaskFilter(maskFilter);
			break;
		default:
			break;
		}
        this.invalidate();
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		
        canvas.translate(hX, hY);
        int w = (int) (linePaint.getStrokeWidth() / 2);
        canvas.drawRect(-hX * 0.8f, -hY * 0.8f, hX * 0.8f, hY * 0.8f, rectPaint);
        canvas.drawLine(-hX * 0.8f, -hY * 0.8f - w, -hX * 0.8f, hY * 0.8f + w, linePaint);
        canvas.drawLine(-hX * 0.8f - w,  hY * 0.8f,  hX * 0.8f + w, hY * 0.8f, linePaint);
        canvas.drawLine( hX * 0.8f,  hY * 0.8f + w,  hX * 0.8f,-hY * 0.8f - w, linePaint);
        canvas.drawLine( hX * 0.8f + w, -hY * 0.8f, -hX * 0.8f - w,-hY * 0.8f, linePaint);

        canvas.drawPath(mPath, mPaint);
        
		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(mWidth, mHeight);
	}
	
}