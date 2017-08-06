package com.aj.sendall.db.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.widget.Toast;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;


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

    public String getUserName() throws IllegalStateException{
        String username = getSharedPrefs().getString(SharedPrefConstants.USER_NAME, null);
        if(username == null){
            return getThisDeviceId();
        } else {
            return username;
        }
    }

    public void setUserName(String username) throws IllegalStateException{
        if(username != null && !(username.length() > SharedPrefConstants.USERNAME_MAX_LEN)) {
            getEditor().putString(SharedPrefConstants.USER_NAME, username);
        } else {
            throw new IllegalStateException();
        }
    }

    public String getThisDeviceId(){
        String thisDeviceId = getSharedPrefs().getString(SharedPrefConstants.DEVICE_ID, null);
        if(thisDeviceId == null){
            thisDeviceId = createIdForThisDevice();
        }
        return thisDeviceId;
    }

    private String createIdForThisDevice(){
        //creating a random string
        char[] candidate =  new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G',
                                        'H', 'I', 'J', 'K', 'L', 'M', 'N',
                                        'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                                        'V', 'W', 'X', 'Y', 'Z',
                                        '0', '1', '2', '3', '4', '5', '6',
                                        '7', '8', '9' };
        StringBuilder idRandomize = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < SharedPrefConstants.THIS_DEVICE_ID_LENGTH; i++){
            idRandomize.append(candidate[random.nextInt(36)]);
        }
        String thisDeviceId = SharedPrefConstants.DEVICE_ID_PREFIX + idRandomize.toString();
        getEditor().putString(SharedPrefConstants.DEVICE_ID, thisDeviceId);
        commit();
        return thisDeviceId;
    }

    public String getDefaultWifiPass(){
        return SharedPrefConstants.DEFAULT_HOTSPOT_PASS;
    }

    public boolean isOurNetwork(ScanResult scanResult){
        if(scanResult != null){
            String ssid = scanResult.SSID;
            if(ssid != null && ssid.startsWith(SharedPrefConstants.DEVICE_ID_PREFIX)){
                return true;
            }
        }
        return false;
    }

    public int getDefServerPort(){
        return SharedPrefConstants.DEF_SERVER_PORT;
    }
}
