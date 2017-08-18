package com.aj.sendall.db.util;

import android.content.Context;

import com.aj.sendall.db.converters.OnDemandConverterList;
import com.aj.sendall.db.dao.ConnectionsDao;
import com.aj.sendall.db.dao.DaoMaster;
import com.aj.sendall.db.dao.DaoSession;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.model.Connections;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DBUtil {
    private DaoSession daoSession = null;
    private Context context;

    @Inject
    DBUtil(Context context){
        this.context = context;
        createDaoSession();
    }

    private void createDaoSession(){
        String DB_NAME = "sendall.db";
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        Database db = helper.getWritableDb();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    @SuppressWarnings({"unchecked"})
    public List<ConnectionViewData> getAllConnectionViewData(){
        List<Connections> connections =  daoSession.getConnectionsDao().queryBuilder().orderDesc(ConnectionsDao.Properties.LastContaced).list();
        return new OnDemandConverterList<>(connections, new OnDemandConverterList.EntityConverter<Connections, ConnectionViewData>() {
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

    /*public boolean isConnectedSendAllDevice(String deviceName){
        ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
        List<Connections> matchedConnection = connectionsDao.queryBuilder()
                .where(ConnectionsDao.Properties.SSID.eq(deviceName))
                .list();
        return (matchedConnection != null && !matchedConnection.isEmpty());
    }*/

    public void saveConnection(Connections conn){
        ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
        List<Connections> matchedConnection = connectionsDao.queryBuilder()
                .where(ConnectionsDao.Properties.SSID.eq(conn.getSSID()))
                .list();
        if(matchedConnection != null && !matchedConnection.isEmpty()){
            for(Connections connections : matchedConnection){
                connections.setConnectionName(conn.getConnectionName());
            }
            connectionsDao.updateInTx(matchedConnection);
        } else {
            daoSession.getConnectionsDao().save(conn);
        }
    }
}
