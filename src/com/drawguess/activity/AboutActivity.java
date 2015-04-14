package com.drawguess.activity;

import com.drawguess.R;
import com.drawguess.base.BaseActivity;

import android.os.Bundle;
import android.view.MenuItem;

/**
 * 关于我们的类
 * @author GuoJun
 *
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initEvents();
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initEvents() {
        setTitle(getString(R.string.setting_text_sysconfig_aboutus));
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    // actionBar的监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    } 

}
