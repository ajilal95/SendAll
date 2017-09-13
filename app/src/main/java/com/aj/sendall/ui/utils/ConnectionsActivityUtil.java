package com.aj.sendall.ui.utils;

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
public final class ConnectionsActivityUtil {
    private DBUtil dbUtil;

    @Inject
    public ConnectionsActivityUtil(DBUtil dbUtil){
        this.dbUtil = dbUtil;
    }

    public List<ConnectionViewData> getAllConnections(){
        return dbUtil.getAllConnectionViewData();
    }
}