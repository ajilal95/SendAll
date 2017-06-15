package com.aj.sendall.db.util;

import android.content.Context;

import com.aj.sendall.db.converters.OnDemandConverterList;
import com.aj.sendall.db.dao.ConnectionsDao;
import com.aj.sendall.db.dao.DaoMaster;
import com.aj.sendall.db.dao.DaoSession;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.sharedprefs.SharedPrefConsts;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 9/5/17.
 */

@Singleton
public class DBUtil {
    private String DB_NAME = "sendall.db";
    private DaoSession daoSession = null;
    private Context context;

    @Inject
    public DBUtil(Context context){
        this.context = context;
        createDaoSession();
    }

    private void createDaoSession(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        Database db = helper.getWritableDb();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public List<ConnectionViewData> getAllConnectionViewData(){
        List<Connections> connections =  daoSession.getConnectionsDao().loadAll();
        return new OnDemandConverterList<Connections, ConnectionViewData>(connections, new OnDemandConverterList.EntityConverter<Connections, ConnectionViewData>() {
            @Override
            public ConnectionViewData convert(Connections fromEntity) {
                ConnectionViewData cvd = null;
                if(fromEntity != null){
                    cvd = new ConnectionViewData();
                    cvd.isSelected = false;
                    cvd.profileId = fromEntity.getConnectionId();
                    cvd.profileName = fromEntity.getConnectionName();
                    cvd.uniqueId = fromEntity.getSSID();
                }
                return cvd;
            }
        });
    }

    public boolean isConnectedSendAllDevice(String deviceName){
        ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
        List<Connections> matchedConnection = connectionsDao.queryBuilder()
                .where(ConnectionsDao.Properties.SSID.eq(deviceName))
                .list();
        return (matchedConnection != null && !matchedConnection.isEmpty());
    }
}
