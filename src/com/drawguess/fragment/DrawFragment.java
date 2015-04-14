package com.drawguess.fragment;


import com.drawguess.R;
import com.drawguess.base.BaseFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
public class DrawFragment extends BaseFragment implements OnClickListener {

    public DrawFragment(Context context) {
        super(context);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_draw, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initEvents() {
    }

    @Override
    protected void init() {
        initViews();
        initEvents();
    }

  

	@Override
	public void onClick(View v) {
		
	}

}
