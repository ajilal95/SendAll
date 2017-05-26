package com.aj.sendall.db.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ajilal on 18/5/17.
 */

public class SharedPrefUtil {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static SharedPreferences getSharedPrefs(Context context){
        if(sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SharedPrefConsts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        }
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

    public static void setCurrentReceivingState(Context context, boolean value, boolean commit){
        getEditor(context).putBoolean(SharedPrefConsts.CURR_RECEIVING_STATE, value);
        if(commit){
            commit();
        }
    }
}
