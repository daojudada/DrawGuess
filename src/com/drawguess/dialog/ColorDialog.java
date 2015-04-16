package com.drawguess.dialog;

import com.drawguess.base.Constant;
import com.drawguess.interfaces.ColorChangedListener;

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
import android.view.Window;
/**
 * 调色板
 * @author GuoJun
 *
 */
public class ColorDialog extends Dialog {
	
	Context context;
	private String title;
	private int mInitialColor;
    private ColorChangedListener mListener;

	/**
     * 构造
     * @param context
     * @param title
     * @param listener 
     */
    public ColorDialog(Context context, String title, 
    		ColorChangedListener listener) {
    	this(context, Color.BLACK, title, listener);
    }
    
    /**
     * @param context
     * @param initialColor 
     * @param title 
     * @param listener 
     */
    public ColorDialog(Context context, int initialColor, 
    		String title, ColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
        this.title = title;
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window dialogWindow = this.getWindow();          
        dialogWindow.setGravity(Gravity.CENTER);       
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        ColorPickerView mview = new ColorPickerView(context);
		//ColorPickerViewGroup viewGroup = new ColorPickerViewGroup(context);
        setContentView(mview);
        
    }
    
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getmInitialColor() {
		return mInitialColor;
	}

	public void setmInitialColor(int mInitialColor) {
		this.mInitialColor = mInitialColor;
	}

	public ColorChangedListener getmListener() {
		return mListener;
	}

	public void setmListener(ColorChangedListener mListener) {
		this.mListener = mListener;
	}
	
	
	
	
    private class ColorPickerView extends View {
    	private Paint mPaint;
    	private Paint mCenterPaint;
    	private Paint mLinePaint;
    	private Paint mRectPaint;
    	
    	private Shader rectShader;
    	private float rectLeft;
    	private float rectTop;
    	private float rectRight;
    	private float rectBottom;
        
    	private final int[] mCircleColors;
    	private final int[] mRectColors;
    	
    	private int mHeight;
    	private int mWidth;
    	private float r;
    	private float centerRadius;
    	
    	private boolean downInCircle = true;
    	private boolean downInRect;
    	private boolean highlightCenter;
    	private boolean highlightCenterLittle;
    	
    	private RectF rf1,rf2;
    	
    	
		public ColorPickerView(Context context) {
			super(context);
			this.mHeight = (int) (Constant.WIN_Y * 0.6f);
			this.mWidth = (int) (Constant.WIN_X * 0.7f);
			setMinimumHeight(mHeight);
			setMinimumWidth(mWidth);
			
	    	mCircleColors = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 
	    			0xFF00FFFF, 0xFF00FF00,0xFFFFFF00, 0xFFFF0000};
	    	Shader s = new SweepGradient(0, 0, mCircleColors, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(100);
            r = mWidth / 2 * 0.8f - mPaint.getStrokeWidth() * 0.5f;
            
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(mInitialColor);
            mCenterPaint.setStrokeWidth(5);
            centerRadius = (r - mPaint.getStrokeWidth() / 2 ) * 0.7f;
            
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setColor(Color.parseColor("#72A1D1"));
            mLinePaint.setStrokeWidth(4);
            
            mRectColors = new int[]{0xFF000000, mCenterPaint.getColor(), 0xFFFFFFFF};
            mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mRectPaint.setStrokeWidth(5);
            rectLeft = -r - mPaint.getStrokeWidth() * 0.5f;
            rectTop = r + mPaint.getStrokeWidth() * 0.5f + 
            		mLinePaint.getStrokeMiter() * 0.5f + 100;
            rectRight = r + mPaint.getStrokeWidth() * 0.5f;
            rectBottom = rectTop + 100;
            
            rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors, null, Shader.TileMode.MIRROR);
			rf1 = new RectF(-centerRadius, -centerRadius, centerRadius, centerRadius);
			rf2 = new RectF(-r, -r, r, r);
			
			
		}


		@Override
		protected void onDraw(Canvas canvas) {
            canvas.translate(mWidth / 2, mHeight / 2 - 150);
            canvas.drawCircle(0, 0, centerRadius,  mCenterPaint);
            if (highlightCenter || highlightCenterLittle) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                if(highlightCenter) {
                	mCenterPaint.setAlpha(0xFF);
                }else if(highlightCenterLittle) {
                	mCenterPaint.setAlpha(0x90);
                }
                canvas.drawCircle(0, 0, centerRadius + mCenterPaint.getStrokeWidth(),  mCenterPaint);
                
                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
            else
            {
            	int c = mCenterPaint.getColor();
            	mCenterPaint.setStyle(Paint.Style.STROKE);
            	mCenterPaint.setColor(Color.WHITE);
            	rf1.set(-centerRadius, -centerRadius, centerRadius, centerRadius);
            	canvas.drawOval(rf1,  mCenterPaint);
            	mCenterPaint.setStyle(Paint.Style.FILL);
            	mCenterPaint.setColor(c);
            }
            rf2.set(-r, -r, r, r);
            canvas.drawOval(rf2, mPaint);
            mRectPaint.setShader(rectShader);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint);
            float offset = mLinePaint.getStrokeWidth() / 2;
            canvas.drawLine(rectLeft - offset, rectTop - offset * 2, 
            		rectLeft - offset, rectBottom + offset * 2, mLinePaint);
            canvas.drawLine(rectLeft - offset * 2, rectTop - offset, 
            		rectRight + offset * 2, rectTop - offset, mLinePaint);
            canvas.drawLine(rectRight + offset, rectTop - offset * 2, 
            		rectRight + offset, rectBottom + offset * 2, mLinePaint);
            canvas.drawLine(rectLeft - offset * 2, rectBottom + offset, 
            		rectRight + offset * 2, rectBottom + offset, mLinePaint);
			super.onDraw(canvas);
		}
		
		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			float x = event.getX() - mWidth / 2;
            float y = event.getY() - mHeight / 2 + 150;
            boolean inCircle = inColorCircle(x, y, 
            		r + mPaint.getStrokeWidth() / 2, r - mPaint.getStrokeWidth() / 2);
            boolean inCenter = inCenter(x, y, centerRadius);
            boolean inRect = inRect(x, y);
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	downInCircle = inCircle;
                	downInRect = inRect;
                	highlightCenter = inCenter;
                case MotionEvent.ACTION_MOVE:
                	if(downInCircle && inCircle) {
                		float angle = (float) Math.atan2(y, x);
                        float unit = (float) (angle / (2 * Math.PI));
                        if (unit < 0) {
                            unit += 1;
                        }
	               		mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
                    	mRectColors[1] = mCenterPaint.getColor();
	                    rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors, null, Shader.TileMode.MIRROR);
                	}else if(downInRect && inRect) {
                		mCenterPaint.setColor(interpRectColor(mRectColors, x));
                	}
                	if((highlightCenter && inCenter) || (highlightCenterLittle && inCenter)) {
                		highlightCenter = true;
                		highlightCenterLittle = false;
                	} else if(highlightCenter || highlightCenterLittle) {
                		highlightCenter = false;
                		highlightCenterLittle = true;
                	} else {
                		highlightCenter = false;
                		highlightCenterLittle = false;
                	}
                   	invalidate();
                	break;
                case MotionEvent.ACTION_UP:
                	if(highlightCenter && inCenter) {
                		if(mListener != null) {
                			mListener.colorChanged(mCenterPaint.getColor());
                			ColorDialog.this.dismiss();
                		}
                	}
                	if(downInCircle) {
                		downInCircle = false;
                	}
                	if(downInRect) {
                		downInRect = false;
                	}
                	if(highlightCenter) {
                		highlightCenter = false;
                	}
                	if(highlightCenterLittle) {
                		highlightCenterLittle = false;
                	}
                	invalidate();
                    break;
            }
            return true;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(mWidth, mHeight);
		}

		/**
		 * @param x
		 * @param y
		 * @param outRadius 
		 * @param inRadius 
		 * @return is
		 */
		private boolean inColorCircle(float x, float y, float outRadius, float inRadius) {
			double outCircle = Math.PI * outRadius * outRadius;
			double inCircle = Math.PI * inRadius * inRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			if(fingerCircle < outCircle && fingerCircle > inCircle) {
				return true;
			}else {
				return false;
			}
		}
		
		/**
		 * @param x 
		 * @param y
		 * @param centerRadius 
		 * @return is
		 */
		private boolean inCenter(float x, float y, float centerRadius) {
			double centerCircle = Math.PI * centerRadius * centerRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			if(fingerCircle < centerCircle) {
				return true;
			}else {
				return false;
			}
		}
		
		/**
		 * @param x
		 * @param y
		 * @return is
		 */
		private boolean inRect(float x, float y) {
			if( x <= rectRight && x >=rectLeft && y <= rectBottom && y >=rectTop) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * @param colors
		 * @param unit
		 * @return color
		 */
		private int interpCircleColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            
            return Color.argb(a, r, g, b);
        }
		
		/**
		 * @param colors
		 * @param x
		 * @return color
		 */
		private int interpRectColor(int colors[], float x) {
			int a, r, g, b, c0, c1;
        	float p;
        	if (x < 0) {
        		c0 = colors[0]; 
        		c1 = colors[1];
        		p = (x + rectRight) / rectRight;
        	} else {
        		c0 = colors[1];
        		c1 = colors[2];
        		p = x / rectRight;
        	}
        	a = ave(Color.alpha(c0), Color.alpha(c1), p);
        	r = ave(Color.red(c0), Color.red(c1), p);
        	g = ave(Color.green(c0), Color.green(c1), p);
        	b = ave(Color.blue(c0), Color.blue(c1), p);
        	return Color.argb(a, r, g, b);
		}
		
		private int ave(int s, int d, float p) {
            return s + Math.round(p * (d - s));
        }
		
		
    }
    
 
    
    
}

