package com.yk.lxr.robotapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yk.lxr.bean.ChatMessage;

import java.util.List;

/**
 * Created by A on 2017/2/26.
 */

public class MessageAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ChatMessage> mDatas;

    public MessageAdapter(Context context, List<ChatMessage> mDatas) {
        this.mInflater  = LayoutInflater.from(context);
        this.mDatas = mDatas;
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mDatas.get(position);
        if (chatMessage.getType() == ChatMessage.Type.INCOMING)
        {
            return 0;
        }
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = mDatas.get(position);
        ViewHolder viewHolder = null;
        if(null == convertView){
            if(0 == getItemViewType(position)){
                convertView = mInflater.inflate(R.layout.item_from_msg, parent,
                        false);
                viewHolder = new ViewHolder();

                viewHolder.mMsg = (TextView) convertView
                        .findViewById(R.id.id_from_msg_info);
                viewHolder.mMsg.setText(chatMessage.getMsg().toString());

            }else{
                convertView = mInflater.inflate(R.layout.item_to_msg, parent,
                        false);
                viewHolder = new ViewHolder();

                viewHolder.mMsg = (TextView) convertView
                        .findViewById(R.id.id_to_msg_info);

                viewHolder.mMsg.setText(chatMessage.getMsg().toString());
            }

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        return convertView;
    }

    private final class ViewHolder
    {
        TextView mMsg;
    }
}
