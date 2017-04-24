package com.aj.sendall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.dto.ConnectionViewData;

import java.util.List;

/**
 * Created by ajilal on 24/4/17.
 */

public class ConnectionListAdapter extends BaseAdapter {
    private List<ConnectionViewData> dataList;
    private Context context;

    public ConnectionListAdapter(List<ConnectionViewData> dataList, Context context){
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        if(dataList != null){
            return dataList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(dataList != null){
            return dataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return Integer.valueOf(position).longValue();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConnectionViewData profileData = null;
        if(dataList != null){
            profileData = dataList.get(position);
        }
        if(profileData != null){
            if(convertView == null){
                convertView = ((LayoutInflater)context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.contact_list_item, parent, false);
            }
            if (profileData.profilePicBitmap != null) {
                ImageView profilePic = (ImageView) convertView.findViewById(R.id.img_vw_profile_pic);
                profilePic.setImageBitmap(profileData.profilePicBitmap);
            }
            if(profileData.profileName == null){
                profileData.profileName = "";
            }
            TextView profileName = (TextView)convertView.findViewById(R.id.txt_vw_profile_name);
            profileName.setText(profileData.profileName);
            return convertView;
        }
        return null;
    }
}
