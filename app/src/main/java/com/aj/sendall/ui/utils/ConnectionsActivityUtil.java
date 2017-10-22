package com.aj.sendall.ui.utils;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.util.DBUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ConnectionsActivityUtil {
    private DBUtil dbUtil;

    @Inject
    ConnectionsActivityUtil(DBUtil dbUtil){
        this.dbUtil = dbUtil;
    }

    public List<ConnectionViewData> getAllConnections(){
        return dbUtil.getAllConnectionViewData();
    }
}
