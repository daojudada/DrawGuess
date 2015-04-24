package com.drawguess.dialog;

import com.drawguess.R;
import com.drawguess.base.BaseDialog;
import com.drawguess.view.FlippingImageView;

import android.content.Context;
import android.widget.TextView;

/**
 * 载入中的提示对话框
 * @author GuoJun
 *
 */
public class LoadingDialog extends BaseDialog {

	private FlippingImageView mFivIcon;
	private TextView mHtvText;
	private String mText;

	public LoadingDialog(Context context, String text) {
		super(context);
		mText = text;
		init();
	}

	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}

	private void init() {
		setContentView(R.layout.dialog_loading);
		mFivIcon = (FlippingImageView) findViewById(R.id.loadingdialog_fiv_icon);
		mHtvText = (TextView) findViewById(R.id.loadingdialog_htv_text);
		mFivIcon.startAnimation();
		mHtvText.setText(mText);
	}

	public void setText(String text) {
		mText = text;
		mHtvText.setText(mText);
	}
}
