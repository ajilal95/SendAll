package com.aj.sendall.controller;

public class AppConsts {

    private static final String WAIT_FOR_CURRENT_OP_TOAST = "Wait for the current operation to finish";

    public static final String CURRENT_APP_STATUS = "curr-app-status";
    public static final String ACTION_APP_STATUS_CHANGED = "curr-app-status-changed";

    //General purpose constants
    public static final String ACCEPT_CONN = "com.aj.sendall.controller.AppController.ACCEPT_CONN";
    public static final String CLOSE_SOCKET = "com.aj.sendall.controller.AppController.CLOSE_SOCKET";
    public static final String FAILED = "com.aj.sendall.controller.AppController.FAILED";
    public static final String SUCCESS = "com.aj.sendall.controller.AppController.SUCCESS";

    public static final long FILE_TRANS_BUFFER_SIZE = 2048;

    //For rerouting the low level updates to ui
    public static final String WIFI_AP_SCANNER_UPDATE_SSIDs = "WIFI_AP_SCANNER_UPDATE_SSIDs";
    public static final String WIFI_AP_SCANNER_SENDALL_NET_AVAILABLE = "WIFI_AP_SCANNER_SENDALL_NET_AVAILABLE";

    public static final String SERVER_OP_FINISHED = "SERVER_OP_FINISHED";
    public static final String CLILENT_OP_FINISHED = "CLIENT_OP_FINISHED";
    public static final String OP_STATUS = "OP_STATUS";
    public static final String FILE_TRANSFER_CLIENT_SSID = "FILE_TRANSFER_CLIENT_SSID";
    public static final String FILE_TRANSFER_SUCCESS = "FILE_TRANSFER_SUCCESS";
    public static final String FILE_TRANSFER_FAILED_AUTH_ERR = "FILE_TRANSFER_FAILED_AUTH_ERR";
    public static final String FILE_TRANSFER_FAILED_IN_SUFF_SPACE = "FILE_TRANSFER_FAILED_IN_SUFF_SPACE";
    public static final String FILE_TRANSFER_FAILED_NO_EXT_MEDIA = "FILE_TRANSFER_FAILED_NO_EXT_MEDIA";
    public static final String FILE_TRANSFER_FAILED_NET_IO_ERR = "FILE_TRANSFER_FAILED_NET_IO_ERR";
    public static final String FILE_TRANSFER_FAILED_FILE_IO_ERR = "FILE_TRANSFER_FAILED_FILE_IO_ERR";

    public static final String CLIENT_COMMUNICATOR = "CLIENT_COMMUNICATOR";
    public static final String CLIENT_AVAILABLE = "CLIENT_AVAILABLE";
    public static final String USERNAME = "USERNAME";

    public static final String DEV_ID = "DEV_ID";

}
