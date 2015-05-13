package com.drawguess.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drawguess.R;
import com.drawguess.activity.BtDrawGuessActivity;
import com.drawguess.base.BaseObjectListAdapter;
import com.drawguess.base.Constant;
import com.drawguess.msgbean.Entity;
import com.drawguess.msgbean.DataGuess;
import com.drawguess.msgbean.User;
import com.drawguess.net.NetManage;
import com.drawguess.util.ImageUtils;
import com.drawguess.util.SessionUtils;
import com.squareup.picasso.Picasso;

public class MsgsAdapter extends BaseObjectListAdapter {
    public static final int LEFT_TEXT = 0;
    public static final int RIGHT_TEXT = 1;
   
    private int mAvatarId;

    public MsgsAdapter(Context context, List<? extends Entity> datas) {
        super(context, datas);

    }

    public void setData(List<? extends Entity> datas) {
        super.setData(datas);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DataGuess msg = (DataGuess) getItem(position);
        
        if (SessionUtils.isLocalUser(msg.getSenderIMEI())) {
            String avatarFileName = User.AVATAR + SessionUtils.getAvatar();
            mAvatarId = ImageUtils.getImageID(avatarFileName);
        }
        else {
        	User users = null;
        	if(Constant.CONNECT_WAY == true)
        		users = NetManage.getLocalUserMap().get(msg.getSenderIMEI());
        	else
        		users = BtDrawGuessActivity.getLocalMap().get(msg.getSenderIMEI());
    		String avatarFileName = User.AVATAR + users.getAvatar();
    		mAvatarId = ImageUtils.getImageID(avatarFileName);
        }
        int messageType = getItemViewType(position);

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            switch (messageType) {
                case LEFT_TEXT:
                    convertView = mInflater.inflate(R.layout.listitem_receive_msg, null);
                    holder.mHtvTimeStampTime = (TextView) convertView.findViewById(R.id.message_timestamp_htv_time);
                    holder.mIvLeftAvatar = (ImageView) convertView.findViewById(R.id.left_message_iv_userphoto);
                    holder.mTvTextContent = (TextView) convertView.findViewById(R.id.message_tv_msgtext);
                    break;

              
                case RIGHT_TEXT:
                    convertView = mInflater.inflate(R.layout.listitem_send_msg, null);
                    holder.mHtvTimeStampTime = (TextView) convertView.findViewById(R.id.message_timestamp_htv_time);
                    holder.mIvRightAvatar = (ImageView) convertView.findViewById(R.id.right_message_iv_userphoto);
                    holder.mTvTextContent = (TextView) convertView.findViewById(R.id.message_tv_msgtext);
                    break;
               
            }
            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (messageType) {
            case LEFT_TEXT:
                holder.mHtvTimeStampTime.setText(msg.getSendTime());
                holder.mTvTextContent.setText(msg.getMsgContent());
                Picasso.with(mContext).load(mAvatarId).into(holder.mIvLeftAvatar);
                break;

            case RIGHT_TEXT:
                holder.mHtvTimeStampTime.setText(msg.getSendTime());
                holder.mTvTextContent.setText(msg.getMsgContent());
                Picasso.with(mContext).load(mAvatarId).into(holder.mIvRightAvatar);
                break;

        }

        return convertView;
    }

    /**
     * 根据数据源的position返回需要显示的的layout的type
     * 
     * */
    @Override
    public int getItemViewType(int position) {

        DataGuess msg = (DataGuess) getItem(position);
        int type = -1;
        if (SessionUtils.isLocalUser(msg.getSenderIMEI())) {
            type = 1;
        }
        else {
            type = 0;
        }
        return type;
    }

    static class ViewHolder {

        private TextView mHtvTimeStampTime; // 时间
        private TextView mTvTextContent; // 文本内容
        private ImageView mIvLeftAvatar; // 左边的头像
        private ImageView mIvRightAvatar; // 右边的头像

    }

}
