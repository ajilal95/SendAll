package com.aj.sendall.controller;

public class AppConsts {

    //General purpose constants
    public static final String ACCEPT_CONN = "com.aj.sendall.controller.AppController.ACCEPT_CONN";
    public static final String CLOSE_SOCKET = "com.aj.sendall.controller.AppController.CLOSE_SOCKET";
    public static final String FAILED = "com.aj.sendall.controller.AppController.FAILED";
    public static final String SUCCESS = "com.aj.sendall.controller.AppController.SUCCESS";

    public static final int FILE_TRANS_BUFFER_SIZE = 8192;
    public static final int SOCKET_TRANSF_SIZE = 131072;

    //For rerouting the low level updates to ui
    public static final String FILE_TRANSFER_SUCCESS = "FILE_TRANSFER_SUCCESS";
    public static final String FILE_TRANSFER_FAILED_AUTH_ERR = "FILE_TRANSFER_FAILED_AUTH_ERR";
    public static final String FILE_TRANSFER_FAILED_IN_SUFF_SPACE = "FILE_TRANSFER_FAILED_IN_SUFF_SPACE";
    public static final String FILE_TRANSFER_FAILED_NO_EXT_MEDIA = "FILE_TRANSFER_FAILED_NO_EXT_MEDIA";
    public static final String FILE_TRANSFER_FAILED_NET_IO_ERR = "FILE_TRANSFER_FAILED_NET_IO_ERR";
    public static final String FILE_TRANSFER_FAILED_FILE_IO_ERR = "FILE_TRANSFER_FAILED_FILE_IO_ERR";

    //Intent extras
    public static final String INTENT_EXTRA_KEY_1 = "extra-1";
    public static final String INTENT_EXTRA_KEY_2 = "extra-2";

}
