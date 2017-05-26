package com.aj.sendall.ui.businessservices;

import android.content.Context;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.util.DBUtil;

import java.util.List;

/**
 * Created by ajilal on 24/4/17.
 */

public final class ConnectionsActivityService {

    public static List<ConnectionViewData> getAllConnections(Context context){
        List<ConnectionViewData> connectionViewDataList = DBUtil.getAllConnectionViewData(context);
        return connectionViewDataList;
    }
}
