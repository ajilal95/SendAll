package com.aj.sendall.db.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 18/5/17.
 */

@Singleton
public class SharedPrefUtil {
    public Context context;
    private SharedPreferences.Editor editor;

    @Inject
    public SharedPrefUtil(Context context){
        this.context = context;
    }

    private SharedPreferences getSharedPrefs(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    private SharedPreferences.Editor getEditor(){
        if(editor == null){
            editor = getSharedPrefs().edit();
        }
        return editor;
    }

    public void commit(){
        if(editor != null){
            editor.apply();
        }
        editor = null;
        switch(getCurrentAppStatus()){
            case SharedPrefConstants.CURR_STATUS_IDLE:
                Toast.makeText(context, "Idle", Toast.LENGTH_LONG).show();
                break;
            case SharedPrefConstants.CURR_STATUS_RECEIVABLE:
                Toast.makeText(context, "Receivable", Toast.LENGTH_LONG).show();
                break;
            case SharedPrefConstants.CURR_STATUS_SENDING:
                Toast.makeText(context, "Sending", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public int getCurrentAppStatus(){
        return getSharedPrefs().getInt(SharedPrefConstants.CURR_APP_STATE, SharedPrefConstants.CURR_STATUS_IDLE);
    }

    public void setCurrentAppStatus(int value){
        getEditor().putInt(SharedPrefConstants.CURR_APP_STATE, value);
    }

    public boolean isAutoScanOnWifiEnabled(){
        return getSharedPrefs().getBoolean(SharedPrefConstants.IS_AUTOSCAN_ON_WIFI_ENABLED, false);
    }

    public void setAutoScanOnWifiEnabled(boolean isAutoScan){
        getEditor().putBoolean(SharedPrefConstants.IS_AUTOSCAN_ON_WIFI_ENABLED, isAutoScan);
    }

    public String getThisDeviceId(Context context){
        String thisDeviceId = getSharedPrefs().getString(SharedPrefConstants.THIS_DEVICE_ID, null);
        if(thisDeviceId == null){
            thisDeviceId = createIdForThisDevice(context);
        }
        return thisDeviceId;
    }

    private String createIdForThisDevice(Context context){
        //creating a random string
        char[] candidate =  new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G',
                                        'H', 'I', 'J', 'K', 'L', 'M', 'N',
                                        'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                                        'V', 'W', 'X', 'Y', 'Z', '_',
                                        '0', '1', '2', '3', '4', '5', '6',
                                        '7', '8', '9' };
        StringBuilder idRandomize = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < SharedPrefConstants.THIS_DEVICE_ID_LENGTH; i++){
            idRandomize.append(candidate[random.nextInt(37)]);
        }
        String thisDeviceId = SharedPrefConstants.DEVICE_ID_PREFIX + idRandomize.toString();
        getEditor().putString(SharedPrefConstants.THIS_DEVICE_ID, thisDeviceId);
        commit();
        return thisDeviceId;
    }
}
