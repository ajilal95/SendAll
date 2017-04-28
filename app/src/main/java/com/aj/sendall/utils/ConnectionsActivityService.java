package com.aj.sendall.utils;

import com.aj.sendall.dto.ConnectionViewData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajilal on 24/4/17.
 */

public final class ConnectionsActivityService {

    public static List<ConnectionViewData> getAllConnections(){
        //for testing
        List<ConnectionViewData> connectionViewDataList = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            ConnectionViewData data = new ConnectionViewData();
            data.profileName = "Connection " + (i + 1);
            connectionViewDataList.add(data);
        }
        return connectionViewDataList;
    }

    public static List<ConnectionViewData> getRecentConnections(){
        //for testing
        List<ConnectionViewData> connectionViewDataList = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            ConnectionViewData data = new ConnectionViewData();
            data.profileName = "Connection " + (i + 1);
            connectionViewDataList.add(data);
        }
        return connectionViewDataList;
    }
}
