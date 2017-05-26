package com.aj.sendall.db.util;

import android.content.Context;

import com.aj.sendall.db.converters.OnDemandConverterList;
import com.aj.sendall.db.dao.DaoMaster;
import com.aj.sendall.db.dao.DaoSession;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.model.Connections;

import org.greenrobot.greendao.database.Database;

import java.util.List;

/**
 * Created by ajilal on 9/5/17.
 */

public class DBUtil {
    public static String DB_NAME = "sendall.db";
    private static DaoSession daoSession = null;

    public static DaoSession getDaoSession(Context context){
        if(daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
            Database db = helper.getWritableDb();
            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public static List<ConnectionViewData> getAllConnectionViewData(Context context){
        List<Connections> connections =  getDaoSession(context).getConnectionsDao().loadAll();
        return new OnDemandConverterList<Connections, ConnectionViewData>(connections, new OnDemandConverterList.EntityConverter<Connections, ConnectionViewData>() {
            @Override
            public ConnectionViewData convert(Connections fromEntity) {
                ConnectionViewData cvd = null;
                if(fromEntity != null){
                    cvd = new ConnectionViewData();
                    cvd.isSelected = false;
                    cvd.profileId = fromEntity.getConnectionId();
                    cvd.profileName = fromEntity.getConnectionName();
                }
                return cvd;
            }
        });
    }
}
