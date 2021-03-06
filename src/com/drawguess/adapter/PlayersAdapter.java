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
import com.drawguess.msgbean.User;
import com.drawguess.util.DataUtils;
import com.drawguess.util.ImageUtils;
import com.squareup.picasso.Picasso;

/**
 * 玩家适配器
 * @author GuoJun
 *
 */
public class PlayersAdapter extends BaseObjectListAdapter {
    class ViewHolder {
        TextView mHtvDevice;
        TextView mHtvLastMsg;
        TextView mHtvName;
        TextView mHtvTime;
        TextView mHtvOrder;
        ImageView mIvAvatar;
        ImageView mIvGender;
        ImageView mIvReady;
        LinearLayout mLayoutGender;
    }

    public PlayersAdapter(Context context, List<? extends Entity> datas) {
        super(context, datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_players, null, false);
            holder = new ViewHolder();

            holder.mIvAvatar = (ImageView) convertView.findViewById(R.id.user_item_iv_avatar);
            holder.mHtvDevice = (TextView) convertView.findViewById(R.id.user_item_tv_device);
            holder.mHtvOrder = (TextView) convertView.findViewById(R.id.uesr_item_htv_order);
            holder.mLayoutGender = (LinearLayout) convertView.findViewById(R.id.user_item_layout_gender);
            holder.mIvGender = (ImageView) convertView.findViewById(R.id.user_item_iv_gender);
            holder.mHtvTime = (TextView) convertView.findViewById(R.id.user_item_htv_time);
            holder.mIvReady = (ImageView) convertView.findViewById(R.id.user_item_iv_isready);
            holder.mHtvName = (TextView) convertView.findViewById(R.id.user_item_htv_name);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        User people = (User) getItem(position);
        int avatarId = ImageUtils.getImageID(User.AVATAR + people.getAvatar());
        Picasso.with(mContext).load(avatarId).into(holder.mIvAvatar);
        holder.mHtvName.setText(people.getNickname());
        holder.mLayoutGender.setBackgroundResource(people.getGenderBgId());
        holder.mIvGender.setImageResource(people.getGenderId());
        holder.mHtvTime.setText(DataUtils.getBetweentime(people.getLogintime()));
        holder.mHtvDevice.setText(people.getDevice());
        if(people.getOrder()>0){
        	holder.mIvReady.setImageResource(R.drawable.btn_ready);
        	holder.mHtvOrder.setText("顺序"+people.getOrder());
        }
        else{
        	holder.mIvReady.setImageResource(R.drawable.btn_unready);
        	holder.mHtvOrder.setText("");
        }
        	
        return convertView;
    }

    @Override
	public void setData(List<? extends Entity> datas) {
        super.setData(datas);
    }
}
