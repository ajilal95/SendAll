package com.aj.sendall.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.NewConnSelected;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.bumptech.glide.Glide;

import java.util.LinkedList;

public class ConnectorAdapter extends RecyclerView.Adapter {
    private LinkedList<ConnectionViewData> connectionViewDatas;
    private Activity containerActivity;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    public ConnectorAdapter(Activity containerActivity){
        this.containerActivity = containerActivity;
    }

    public void setData(LinkedList<ConnectionViewData> connectionViewDatas){
        this.connectionViewDatas = connectionViewDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((LayoutInflater)containerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.contact_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConnectionViewData connectionViewData = connectionViewDatas.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.profileName.setText(connectionViewData.profileName);
        Glide.with(containerActivity)
                .load(connectionViewData.profilePicPath)
                .centerCrop()
                .override(96,96)
                .error(R.mipmap.default_profile)
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

        ViewHolder(View view){
            super(view);

            profilePic = (ImageView) view.findViewById(R.id.img_vw_profile_pic);
            profileName = (TextView) view.findViewById(R.id.txt_vw_profile_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewConnSelected event = new NewConnSelected();
                    event.selectedConn = (ConnectionViewData) itemView.getTag();
                    eventRouter.broadcast(event);
                }
            });
        }
    }

}
