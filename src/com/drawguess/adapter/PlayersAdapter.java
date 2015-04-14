package com.drawguess.adapter;


import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drawguess.R;
import com.drawguess.base.BaseObjectListAdapter;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.Users;
import com.drawguess.util.DateUtils;
import com.drawguess.util.ImageUtils;
import com.squareup.picasso.Picasso;

/**
 * 玩家适配器
 * @author GuoJun
 *
 */
public class PlayersAdapter extends BaseObjectListAdapter {
    public PlayersAdapter(Context context, List<? extends Entity> datas) {
        super(context, datas);
    }

    public void setData(List<? extends Entity> datas) {
        super.setData(datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_players, null, false);
            holder = new ViewHolder();

            holder.mIvAvatar = (ImageView) convertView.findViewById(R.id.user_item_iv_avatar);
            holder.mHtvDevice = (TextView) convertView.findViewById(R.id.user_item_tv_device);
            holder.mHtvName = (TextView) convertView.findViewById(R.id.user_item_htv_name);
            holder.mLayoutGender = (LinearLayout) convertView
                    .findViewById(R.id.user_item_layout_gender);
            holder.mIvGender = (ImageView) convertView.findViewById(R.id.user_item_iv_gender);
            holder.mHtvTime = (TextView) convertView.findViewById(R.id.user_item_htv_time);
            holder.mIvReady = (ImageView) convertView.findViewById(R.id.user_item_iv_isready);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        Users people = (Users) getItem(position);
        int avatarId = ImageUtils.getImageID(Users.AVATAR + people.getAvatar());
        Picasso.with(mContext).load(avatarId).into(holder.mIvAvatar);
        holder.mHtvName.setText(people.getNickname());
        holder.mLayoutGender.setBackgroundResource(people.getGenderBgId());
        holder.mIvGender.setImageResource(people.getGenderId());
        holder.mHtvTime.setText(DateUtils.getBetweentime(people.getLogintime()));
        holder.mHtvDevice.setText(people.getDevice());
        if(people.getOrder()>0)
        	holder.mIvReady.setImageResource(R.drawable.btn_ready);
        else
        	holder.mIvReady.setImageResource(R.drawable.btn_unready);
        	
        return convertView;
    }

    class ViewHolder {
        ImageView mIvAvatar;
        TextView mHtvDevice;
        TextView mHtvName;
        LinearLayout mLayoutGender;
        ImageView mIvGender;
        TextView mHtvTime;
        TextView mHtvLastMsg;
        ImageView mIvReady;
    }
}
