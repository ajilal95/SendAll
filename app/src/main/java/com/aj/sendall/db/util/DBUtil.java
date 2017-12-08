package com.aj.sendall.db.util;

import android.content.Context;

import com.aj.sendall.db.converters.OnDemandConverterList;
import com.aj.sendall.db.dao.ConnectionsDao;
import com.aj.sendall.db.dao.DaoMaster;
import com.aj.sendall.db.dao.DaoSession;
import com.aj.sendall.db.dao.PersonalInteractionDao;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.PersonalInteractionDTO;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;

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

    @SuppressWarnings("unchecked")
    public List<PersonalInteractionDTO> getAllPersonalInteractionDTO(long connId){
        List<PersonalInteraction> pis = daoSession.getPersonalInteractionDao()
                .queryBuilder()
                .where(PersonalInteractionDao.Properties.ConnectionId.eq(connId))
                .orderDesc(PersonalInteractionDao.Properties.ModifiedTime)
                .list();
        return new OnDemandConverterList<>(pis, new OnDemandConverterList.EntityConverter<PersonalInteraction, PersonalInteractionDTO>() {
            public PersonalInteractionDTO convert(PersonalInteraction pi){
                PersonalInteractionDTO pidto = new PersonalInteractionDTO();
                pidto.id = pi.getPersonalInteractionId();
                pidto.filePath = pi.getFilePath();
                pidto.mediaType = pi.getMediaType();
                pidto.status = pi.getFileStatus();
                pidto.size = pi.getFileSize();
                pidto.title = pi.getFileName();
                return pidto;
            }
        });
    }

    public Connections getConnectionBySSID(String SSID){
        return daoSession.getConnectionsDao().queryBuilder().where(ConnectionsDao.Properties.SSID.eq(SSID)).unique();
    }

    public PersonalInteraction getPersonalInteraction(long connId, String fileName, int mediaType, long fileSize){
        PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
        List<PersonalInteraction> matchingPIs = personalInteractionDao.queryBuilder()
                .where(PersonalInteractionDao.Properties.FileName.eq(fileName),
                        PersonalInteractionDao.Properties.ConnectionId.eq(connId),
                        PersonalInteractionDao.Properties.MediaType.eq(mediaType),
                        PersonalInteractionDao.Properties.FileSize.eq(fileSize))
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

    public void save(Connections conn){
        ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
        if(conn != null){
            connectionsDao.insert(conn);
        }
    }

    public void update(PersonalInteraction pi){
        if(pi != null){
            PersonalInteractionDao personalInteractionDao = daoSession.getPersonalInteractionDao();
            personalInteractionDao.update(pi);
        }
    }

    public void update(Connections conn){
        if(conn != null){
            ConnectionsDao connectionsDao = daoSession.getConnectionsDao();
            connectionsDao.update(conn);
        }
    }
}
