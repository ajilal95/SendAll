package com.aj.sendall.ui.businessservices;

import android.content.Context;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 24/4/17.
 */

@Singleton
public final class ConnectionsActivityService {
    private DBUtil dbUtil;

    @Inject
    public ConnectionsActivityService(DBUtil dbUtil){
        this.dbUtil = dbUtil;
    }
    public List<ConnectionViewData> getAllConnections(){
        ConnectionViewData dummy = new ConnectionViewData();
        dummy.uniqueId = "anda";
        dummy.isSelected = false;
        dummy.profileId = 0;
        dummy.profileName = "Dummy";
        List<ConnectionViewData> list = new ArrayList<>();
        list.add(dummy);
        return /*dbUtil.getAllConnectionViewData();*/list;
    }
}
