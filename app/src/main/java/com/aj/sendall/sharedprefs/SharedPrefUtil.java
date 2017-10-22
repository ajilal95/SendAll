package com.aj.sendall.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class SharedPrefUtil {
    public Context context;
    private SharedPreferences.Editor editor;

    @Inject
    SharedPrefUtil(Context context){
        this.context = context;
    }

    private SharedPreferences getSharedPrefs(){
        return context.getSharedPreferences(SharedPrefConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor(){
        if(editor == null){
            editor = getSharedPrefs().edit();
        }
        return editor;
    }

    private void commit(){
        if(editor != null){
            editor.apply();
        }
        editor = null;
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
        String thisDeviceId = SharedPrefConstants.DEVICE_ID_PREFIX
                + getStringForLong(System.currentTimeMillis(), SharedPrefConstants.THIS_DEVICE_ID_LENGTH);
        getEditor().putString(SharedPrefConstants.DEVICE_ID, thisDeviceId);
        commit();
        return thisDeviceId;
    }

    private String getStringForLong(long num, int strLen){
        if(strLen != 10){
            throw new IllegalStateException("Unique id length must be 10");
        }
        //to mask last 6 bits of num,since we choose character from a set of 64 characters
        long maskbits = 0x3F;

        //64 characters.
        char[] candidate =  new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G',
                                        'H', 'I', 'J', 'K', 'L', 'M', 'N',
                                        'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                                        'V', 'W', 'X', 'Y', 'Z',
                                        'a', 'b', 'c', 'd', 'e', 'f', 'g',
                                        'h', 'i', 'j', 'k', 'l', 'm', 'n',
                                        'o', 'p', 'q', 'r', 's', 't', 'u',
                                        'v', 'w', 'x', 'y', 'z',
                                        '0', '1', '2', '3', '4', '5', '6',
                                        '7', '8', '9', '$', '#' };
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < strLen; i++){
            int nextIndex = (int) (num & maskbits);
            char character = candidate[nextIndex];
            stringBuilder.append(character);
            num = num >> 4;
        }
        return stringBuilder.reverse().toString();
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
}
