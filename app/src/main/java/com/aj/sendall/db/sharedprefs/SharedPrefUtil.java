package com.aj.sendall.db.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * Created by ajilal on 18/5/17.
 */

public class SharedPrefUtil {
    private static SharedPreferences.Editor editor;

    private static SharedPreferences getSharedPrefs(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefConsts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    private static SharedPreferences.Editor getEditor(Context context){
        if(editor == null){
            editor = getSharedPrefs(context).edit();
        }
        return editor;
    }

    private static void commit(){
        if(editor != null){
            editor.apply();
        }
        editor = null;
    }

    public static boolean getCurrentReceivingStatus(Context context){
        return getSharedPrefs(context).getBoolean(SharedPrefConsts.CURR_RECEIVING_STATE, false);
    }

    public static void setCurrentReceivingState(Context context, boolean value, boolean doCommit){
        getEditor(context).putBoolean(SharedPrefConsts.CURR_RECEIVING_STATE, value);
        if(doCommit){
            commit();
        }
    }

    public static String getThisDeviceId(Context context){
        String thisDeviceId = getSharedPrefs(context).getString(SharedPrefConsts.THIS_DEVICE_ID, null);
        if(thisDeviceId == null){
            thisDeviceId = createIdForThisDevice(context);
        }
        return thisDeviceId;
    }

    private static String createIdForThisDevice(Context context){
        //creating a random string
        char[] candidate =  new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G',
                                        'H', 'I', 'J', 'K', 'L', 'M', 'N',
                                        'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                                        'V', 'W', 'X', 'Y', 'Z', '_',
                                        '0', '1', '2', '3', '4', '5', '6',
                                        '7', '8', '9' };
        StringBuilder idRandomize = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < SharedPrefConsts.THIS_DEVICE_ID_LENGTH; i++){
            idRandomize.append(candidate[random.nextInt(37)]);
        }
        String thisDeviceId = SharedPrefConsts.DEVICE_ID_PREFIX + idRandomize.toString();
        getEditor(context).putString(SharedPrefConsts.THIS_DEVICE_ID, thisDeviceId);
        commit();
        return thisDeviceId;
    }
}
