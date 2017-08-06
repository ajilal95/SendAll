package com.aj.sendall.db.sharedprefs;


public class SharedPrefConstants {
    static final String SHARED_PREF_NAME = "com.aj.sendall.db.sharedprefs";

    static final String CURR_APP_STATE = "curr_app_state";
    public static final int CURR_STATUS_IDLE = 0;
    public static final int CURR_STATUS_RECEIVABLE = 1;
    public static final int CURR_STATUS_SENDING = 2;
    public static final int CURR_STATUS_CEATING_CONNECTION = 3;
    public static final int CURR_STATUS_STOPPING_ALL = 4;

    static final String IS_AUTOSCAN_ON_WIFI_ENABLED =  "is_autoscan_on_wifi_enable";

    public static final String DEVICE_ID = "this_device_id";
    public static final String USER_NAME = "user_name";
    public static final int USERNAME_MAX_LEN = 15;
    static final String DEVICE_ID_PREFIX = "SNDAL";
    static final String DEFAULT_HOTSPOT_PASS = "SENDALL123";
    static final int DEF_SERVER_PORT = 10001;
    static final int THIS_DEVICE_ID_LENGTH = 10;
}
