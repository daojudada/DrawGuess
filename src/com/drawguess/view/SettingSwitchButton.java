package com.drawguess.view;

import com.drawguess.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;


/**
 * 按钮选择
 * @author copy from other
 *
 */
public class SettingSwitchButton extends CheckBox {
    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    private final class SwitchAnimation implements Runnable {

        @Override
        public void run() {
            if (!mAnimating) {
                return;
            }
            doAnimation();
            FrameAnimationController.requestAnimationFrame(this);
        }
    }

    private final int MAX_ALPHA = 255;
    
    private final float EXTENDED_OFFSET_Y = 15;

    private int mAlpha = MAX_ALPHA;

    private float mAnimatedVelocity;

    private boolean mAnimating;

    private float mAnimationPosition;


    private Bitmap mBottom;

    private boolean mBroadcasting;

    private float mBtnInitPos;

    private Bitmap mBtnNormal;

    private float mBtnOffPos; // 开关关闭的位置

    private float mBtnOnPos; // 开关打开的位置

    private float mBtnPos; // 按钮的位置

    private Bitmap mBtnPressed;

    private float mBtnWidth;

    private boolean mChecked = false;

    private int mClickTimeout;

    private Bitmap mCurBtnPic;

    private float mExtendOffsetY; // Y轴方向扩大的区域,增大点击区域

    private float mFirstDownX; // 首次按下的X

    private float mFirstDownY; // 首次按下的Y

    private Bitmap mFrame;

    private Bitmap mMask;

    private float mMaskHeight;

    private float mMaskWidth;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

    private Paint mPaint;

    private ViewParent mParent;

    private PerformClick mPerformClick;

    private float mRealPos; // 图片的绘制位置

    private RectF mSaveLayerRectF;

    private int mTouchSlop;

    private boolean mTurningOn;

    private float mVelocity;

    private PorterDuffXfermode mXfermode;

    private final float VELOCITY = 350;

    public SettingSwitchButton(Context context) {
        this(context, null);
    }

    public SettingSwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public SettingSwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        mParent = getParent();
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void doAnimation() {
        mAnimationPosition += mAnimatedVelocity * FrameAnimationController.ANIMATION_FRAME_DURATION
                / 1000;
        if (mAnimationPosition <= mBtnOnPos) {
            stopAnimation();
            mAnimationPosition = mBtnOnPos;
            setCheckedDelayed(true);
        }
        else if (mAnimationPosition >= mBtnOffPos) {
            stopAnimation();
            mAnimationPosition = mBtnOffPos;
            setCheckedDelayed(false);
        }
        moveView(mAnimationPosition);
    }

    /**
     * 将btnPos转换成RealPos
     * 
     * @param btnPos
     * @return
     */
    private float getRealPos(float btnPos) {
        return btnPos - mBtnWidth / 2;
    }

    private void initView(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        Resources resources = context.getResources();

        // get viewConfiguration
        mClickTimeout = ViewConfiguration.getPressedStateDuration()
                + ViewConfiguration.getTapTimeout();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        // get Bitmap
        mBottom = BitmapFactory.decodeResource(resources, R.drawable.bottom);
        mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.btn_pressed);
        mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.btn_unpressed);
        mFrame = BitmapFactory.decodeResource(resources, R.drawable.frame);
        mMask = BitmapFactory.decodeResource(resources, R.drawable.mask);
        mCurBtnPic = mBtnNormal;

        mBtnWidth = mBtnPressed.getWidth();
        mMaskWidth = mMask.getWidth();
        mMaskHeight = mMask.getHeight();

        mBtnOffPos = mBtnWidth / 2;
        mBtnOnPos = mMaskWidth - mBtnWidth / 2;

        mBtnPos = mChecked ? mBtnOnPos : mBtnOffPos;
        mRealPos = getRealPos(mBtnPos);

        final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY * density + 0.5f);
        mExtendOffsetY = (int) (EXTENDED_OFFSET_Y * density + 0.5f);

        mSaveLayerRectF = new RectF(0, mExtendOffsetY, mMask.getWidth(), mMask.getHeight()
                + mExtendOffsetY);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    public boolean isChecked() {
        return mChecked;
    }

    private void moveView(float position) {
        mBtnPos = position;
        mRealPos = getRealPos(mBtnPos);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(mSaveLayerRectF, mAlpha, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        // 绘制蒙板
        canvas.drawBitmap(mMask, 0, mExtendOffsetY, mPaint);
        mPaint.setXfermode(mXfermode);

        // 绘制底部图片
        canvas.drawBitmap(mBottom, mRealPos, mExtendOffsetY, mPaint);
        mPaint.setXfermode(null);
        // 绘制边框
        canvas.drawBitmap(mFrame, 0, mExtendOffsetY, mPaint);

        // 绘制按钮
        canvas.drawBitmap(mCurBtnPic, mRealPos, mExtendOffsetY, mPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mMaskWidth, (int) (mMaskHeight + 2 * mExtendOffsetY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float deltaX = Math.abs(x - mFirstDownX);
        float deltaY = Math.abs(y - mFirstDownY);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                attemptClaimDrag();
                mFirstDownX = x;
                mFirstDownY = y;
                mCurBtnPic = mBtnPressed;
                mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
                break;
            case MotionEvent.ACTION_MOVE:
                float time = event.getEventTime() - event.getDownTime();
                mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;
                if (mBtnPos >= mBtnOffPos) {
                    mBtnPos = mBtnOffPos;
                }
                if (mBtnPos <= mBtnOnPos) {
                    mBtnPos = mBtnOnPos;
                }
                mTurningOn = mBtnPos > (mBtnOffPos - mBtnOnPos) / 2 + mBtnOnPos;

                mRealPos = getRealPos(mBtnPos);
                break;
            case MotionEvent.ACTION_UP:
                mCurBtnPic = mBtnNormal;
                time = event.getEventTime() - event.getDownTime();
                if (deltaY < mTouchSlop && deltaX < mTouchSlop && time < mClickTimeout) {
                    if (mPerformClick == null) {
                        mPerformClick = new PerformClick();
                    }
                    if (!post(mPerformClick)) {
                        performClick();
                    }
                }
                else {
                    startAnimation(!mTurningOn);
                }
                break;
        }

        invalidate();
        return isEnabled();
    }

    @Override
    public boolean performClick() {
    	super.performClick();
        startAnimation(!mChecked);
        return true;
    }

    /**
     * <p>
     * Changes the checked state of this button.
     * </p>
     * 
     * @param checked
     *            true to check the button, false to uncheck it
     */
    public void setChecked(boolean checked) {

        if (mChecked != checked) {
            mChecked = checked;

            mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
            mRealPos = getRealPos(mBtnPos);
            invalidate();

            // Avoid infinite recursions if setChecked() is called from a
            // listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(SettingSwitchButton.this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(SettingSwitchButton.this, mChecked);
            }

            mBroadcasting = false;
        }
    }

    /**
     * 内部调用此方法设置checked状态，此方法会延迟执行各种回调函数，保证动画的流畅度
     * 
     * @param checked
     */
    private void setCheckedDelayed(final boolean checked) {
        this.postDelayed(new Runnable() {

            @Override
            public void run() {
                setChecked(checked);
            }
        }, 10);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA / 2;
        super.setEnabled(enabled);
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     * 
     * @param listener
     *            the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes. This callback is used for internal purpose only.
     * 
     * @param listener
     *            the callback to call on checked state change
     * @hide
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    private void startAnimation(boolean turnOn) {
        mAnimating = true;
        mAnimatedVelocity = turnOn ? -mVelocity : mVelocity;
        mAnimationPosition = mBtnPos;

        new SwitchAnimation().run();
    }

    private void stopAnimation() {
        mAnimating = false;
    }

    public void toggle() {
        setChecked(!mChecked);
    }
}
