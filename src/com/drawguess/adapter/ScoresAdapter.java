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
public class ScoresAdapter extends BaseObjectListAdapter {
    class ViewHolder {
        TextView mTvName;
        TextView mTvScore;
        ImageView mIvAvatar;
    }

    public ScoresAdapter(Context context, List<? extends Entity> datas) {
        super(context, datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_scores, null, false);
            holder = new ViewHolder();

            holder.mIvAvatar = (ImageView) convertView.findViewById(R.id.score_item_iv_avatar);
            holder.mTvName = (TextView) convertView.findViewById(R.id.score_item_tv_name);
            holder.mTvScore = (TextView) convertView.findViewById(R.id.score_item_tv_score);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        User people = (User) getItem(position);
        int avatarId = ImageUtils.getImageID(User.AVATAR + people.getAvatar());
        Picasso.with(mContext).load(avatarId).into(holder.mIvAvatar);
        holder.mTvName.setText(people.getNickname());
        holder.mTvScore.setText(people.getScore()+"");
        
        	
        return convertView;
    }

    @Override
	public void setData(List<? extends Entity> datas) {
        super.setData(datas);
    }
}
