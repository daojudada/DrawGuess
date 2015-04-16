package com.drawguess.activity;

import com.drawguess.R;
import com.drawguess.adapter.AvatarAdapter;
import com.drawguess.base.BaseActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
/**
 * 选择头像
 * @author GuoJun
 *
 */
public class ChooseAvatarActivity extends BaseActivity implements OnItemClickListener {
    // 图片ID数组
    private final static int[] images = new int[] { R.drawable.avatar1, R.drawable.avatar2,
            R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
            R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10,
            R.drawable.avatar11, R.drawable.avatar12, };
    AvatarAdapter adapter;

    private GridView gridView;

    private void initData() {
        setTitle(getString(R.string.choose_avatar));
        adapter = new AvatarAdapter(images, this);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void initEvents() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        gridView.setOnItemClickListener(this);
    }

    @Override
    protected void initViews() {
        gridView = (GridView) findViewById(R.id.gridview);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setavater);
        initViews();
        initData();
        initEvents();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        intent.putExtra("result", position);
        setResult(RESULT_OK, intent);
        finish();
    }

    // actionBar的监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
