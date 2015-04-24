package com.drawguess.adapter;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.drawguess.R;
import com.drawguess.msgbean.Avatar;
import com.squareup.picasso.Picasso;

/**
 * 头像适配器
 * @author GuoJun
 *
 */
public class AvatarAdapter extends BaseAdapter {

    private static class ViewHolder {
        public ImageView ivAvatar;
    }
    private Context mContext;
    private List<Avatar> mDatas;

    private LayoutInflater mInflater;

    public AvatarAdapter(int[] images, Context context) {
        super();
        mDatas = new ArrayList<Avatar>();
        mInflater = LayoutInflater.from(context);
        mContext = context;
        int mSize = images.length;
        for (int i = 0; i < mSize; i++) {
            Avatar avatar = new Avatar(images[i]);
            mDatas.add(avatar);
        }
    }

    @Override
    public int getCount() {
        if (null != mDatas) {
            return mDatas.size();
        }
        else {
            return 0;
        }
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_avatar, null);
            holder = new ViewHolder();
            holder.ivAvatar = (ImageView) convertView.findViewById(R.id.myprofile_avatarimage);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext).load(mDatas.get(position).getImageId())
                .into(holder.ivAvatar);
        return convertView;
    }
}
