package com.aj.sendall.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.ArrayList;
import java.util.List;

public class ConnectionAdapter extends BaseAdapter {
    private List<ConnectionViewData> dataList;
    private List<ConnectionViewData> filteredItems;
    private Context context;
    private String filterString = null;

    public ConnectionAdapter(List<ConnectionViewData> dataList, Context context){
        this.dataList = dataList;
        this.context = context;
    }

    public void setDataList(List<ConnectionViewData> dataList){
        this.dataList = dataList;
        filter();
    }

    @Override
    public int getCount() {
        if(getFilteredItems() != null){
            return getFilteredItems().size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return getProfileData(position);
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

    public void setFilterString(String filterString){
        this.filterString = filterString;
        filter();
    }

    private void filter() {
        if(filterString == null || "".equals(filterString)){
            filteredItems = null;
        } else {
            filteredItems = new ArrayList<>();
            for(ConnectionViewData filteredItem : dataList){
                if(filteredItem.isSelected ||
                        filteredItem.profileName.toLowerCase().contains(filterString.toLowerCase())) {
                    filteredItems.add(filteredItem);
                }
            }
        }
    }

    private View getProfileDataView(int position, View convertView) {
        ConnectionViewData profileData = getProfileData(position);
        if (profileData != null) {
            ImageView profilePic = (ImageView) convertView.findViewById(R.id.img_vw_profile_pic);
            profilePic.setVisibility(View.VISIBLE);
            if (profileData.profileName == null) {
                profileData.profileName = "";
            }
            TextView profileName = (TextView) convertView.findViewById(R.id.txt_vw_profile_name);
            profileName.setText(profileData.profileName);
            profileName.setTextColor(Color.BLACK);
            convertView.setTag(profileData);
            CommonUiUtils.setViewSelectedAppearanceSimple(convertView, profileData.isSelected);
        }
        return convertView;
    }

    private ConnectionViewData getProfileData(int position) {
        ConnectionViewData profileData = null;
        if(getFilteredItems() != null){
            profileData = getFilteredItems().get(position);
        }
        return profileData;
    }

    private List<ConnectionViewData> getFilteredItems(){
        if (filteredItems != null) {
            return filteredItems;
        } else {
            return dataList;
        }
    }
}
