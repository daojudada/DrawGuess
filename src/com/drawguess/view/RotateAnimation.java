package com.drawguess.view;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 动画旋转
 * @author copy from other
 *
 */
public class RotateAnimation extends Animation {

	public enum Mode {
		X, Y, Z;
	}
	private Camera mCamera;
	private float mCenterX;
	private float mCenterY;

	private Mode mMode;

	public RotateAnimation(float centerX, float centerY, Mode mode) {
		mCenterX = centerX;
		mCenterY = centerY;
		mMode = mode;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float deg = 0.0F + 360.0F * interpolatedTime;
		Matrix matrix = t.getMatrix();
		mCamera.save();
		if (mMode == Mode.X)
			mCamera.rotateX(deg);
		if (mMode == Mode.Y)
			mCamera.rotateY(deg);
		if (mMode == Mode.Z)
			mCamera.rotateZ(deg);

		mCamera.getMatrix(matrix);
		mCamera.restore();
		matrix.preTranslate(-mCenterX, -mCenterY);
		matrix.postTranslate(mCenterX, mCenterY);

	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}
}
