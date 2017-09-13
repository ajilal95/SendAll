package com.aj.sendall.db.util;

import android.content.Context;

import com.aj.sendall.db.converters.OnDemandConverterList;
import com.aj.sendall.db.dao.ConnectionsDao;
import com.aj.sendall.db.dao.DaoMaster;
import com.aj.sendall.db.dao.DaoSession;
import com.aj.sendall.db.dao.PersonalInteractionDao;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.Date;
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

    public void saveOrUpdate(Connections conn){
        ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
        List<Connections> matchedConnection = connectionsDao.queryBuilder()
                .where(ConnectionsDao.Properties.SSID.eq(conn.getSSID()))
                .list();
        if(matchedConnection != null && !matchedConnection.isEmpty()){
            for(Connections connections : matchedConnection){
                connections.setConnectionName(conn.getConnectionName());
                connections.setLastContaced(conn.getLastContaced());
            }
            connectionsDao.updateInTx(matchedConnection);
        } else {
            daoSession.getConnectionsDao().save(conn);
        }
    }

    public void saveOrUpdate(PersonalInteraction pi){
        PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
        List<PersonalInteraction> matchingPIs = personalInteractionDao.queryBuilder()
                .where(PersonalInteractionDao.Properties.FilePath.eq(pi.getFilePath()),
                        PersonalInteractionDao.Properties.ConnectionId.eq(pi.getConnectionId()),
                        PersonalInteractionDao.Properties.MediaType.eq(pi.getMediaType()))
                .list();
        if(matchingPIs == null || matchingPIs.isEmpty()){
            personalInteractionDao.save(pi);
        } else {
            for(PersonalInteraction mpi : matchingPIs){
                mpi.setModifiedTime(pi.getModifiedTime());
                mpi.setFileUri(pi.getFileUri());
                mpi.setBytesTransfered(pi.getBytesTransfered());
                mpi.setFileSize(pi.getFileSize());
                mpi.setFileStatus(pi.getFileStatus());
            }

            personalInteractionDao.updateInTx(matchingPIs);
        }
    }

    public PersonalInteraction getPersonalInteraction(long connId, String filePath, int mediaType){
        PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
        List<PersonalInteraction> matchingPIs = personalInteractionDao.queryBuilder()
                .where(PersonalInteractionDao.Properties.FilePath.eq(filePath),
                        PersonalInteractionDao.Properties.ConnectionId.eq(connId),
                        PersonalInteractionDao.Properties.MediaType.eq(mediaType))
                .list();
        if(matchingPIs == null || matchingPIs.isEmpty()){
            return null;
        } else {
            return matchingPIs.get(0);
        }
    }

    public void save(PersonalInteraction pi){
        PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
        if(pi != null){
            personalInteractionDao.save(pi);
        }
    }

    public void update(PersonalInteraction pi){
        PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
        if(pi != null){
            personalInteractionDao.update(pi);
        }
    }
}
