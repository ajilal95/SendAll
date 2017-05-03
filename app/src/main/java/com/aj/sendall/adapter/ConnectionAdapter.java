package com.aj.sendall.adapter;

import android.content.Context;
import android.graphics.Color;
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

public class ConnectionAdapter extends BaseAdapter {
    private List<ConnectionViewData> dataList;
    private Context context;

    public ConnectionAdapter(List<ConnectionViewData> dataList, Context context){
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
        if(convertView == null){
            convertView = ((LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.contact_list_item, parent, false);
        }

        convertView = getProfileDataView(position, convertView);
        return convertView;
    }

    private View getProfileDataView(int position, View convertView) {
        ConnectionViewData profileData = getProfileData(position);
        if (profileData != null) {
            ImageView profilePic = (ImageView) convertView.findViewById(R.id.img_vw_profile_pic);
            profilePic.setVisibility(View.VISIBLE);
            if (profileData.profilePicBitmap != null) {
                profilePic.setImageBitmap(profileData.profilePicBitmap);
            }
            if (profileData.profileName == null) {
                profileData.profileName = "";
            }
            TextView profileName = (TextView) convertView.findViewById(R.id.txt_vw_profile_name);
            profileName.setText(profileData.profileName);
            profileName.setTextColor(Color.BLACK);
            convertView.setTag(profileData);
        }
        return convertView;
    }

    private ConnectionViewData getProfileData(int position) {
        ConnectionViewData profileData = null;
        if (dataList != null) {
            profileData = dataList.get(position);
        }
        return profileData;
    }
}
