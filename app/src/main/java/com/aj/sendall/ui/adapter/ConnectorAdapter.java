package com.aj.sendall.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.ui.activity.Connector;
import com.bumptech.glide.Glide;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by ajilal on 4/7/17.
 */

public class ConnectorAdapter extends RecyclerView.Adapter {
    private LinkedList<ConnectionViewData> connectionViewDatas;
    private Context context;
    private View.OnClickListener clickListenerForItem;

    public ConnectorAdapter(Context context, View.OnClickListener clickListenerForItem){
        this.context = context;
        this.clickListenerForItem = clickListenerForItem;
    }

    public void setData(LinkedList<ConnectionViewData> connectionViewDatas){
        this.connectionViewDatas = connectionViewDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.contact_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConnectionViewData connectionViewData = connectionViewDatas.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.profileName.setText(connectionViewData.profileName);
        Glide.with(context)
                .load(connectionViewData.profilePicPath)
                .centerCrop()
                .override(96,96)
                .into(viewHolder.profilePic);
        viewHolder.itemView.setTag(connectionViewData);
    }

    @Override
    public int getItemCount() {
        return connectionViewDatas == null ? 0 : connectionViewDatas.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePic;
        TextView profileName;

        public ViewHolder(View view){
            super(view);

            profilePic = (ImageView) view.findViewById(R.id.img_vw_profile_pic);
            profileName = (TextView) view.findViewById(R.id.txt_vw_profile_name);

            view.setOnClickListener(clickListenerForItem);
        }
    }

}
