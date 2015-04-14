package com.drawguess.base;


import java.util.ArrayList;
import java.util.List;

import com.drawguess.R;
import com.drawguess.msgbean.Entity;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BaseObjectListAdapter extends BaseAdapter {

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<? extends Entity> mDatas = new ArrayList<Entity>();

    public BaseObjectListAdapter(Context context, List<? extends Entity> datas) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (datas != null) {
            mDatas = datas;
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public List<? extends Entity> getDatas() {
        return mDatas;
    }

    public void setData(List<? extends Entity> datas) {
        this.mDatas = datas;
    }

    protected void showCustomToast(String text) {
        View toastRoot = LayoutInflater.from(mContext).inflate(R.layout.toast_common, null, false);
        ( (TextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
        Toast toast = new Toast(mContext);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastRoot);
        toast.show();
    }
}
