package com.aj.sendall.network.utils;

/**
 * Created by ajilal on 17/6/17.
 */

public class Constants {
    public static final String P2P_SERVICE_INSTANCE_NAME = "_sendall";
    public static final String P2P_SERVICE_SERVICE_TYPE = "_sendall._tcp";
    public static final String P2P_SERVICE_FULL_DOMAIN_NAME = P2P_SERVICE_INSTANCE_NAME + '.' + P2P_SERVICE_SERVICE_TYPE + ".local.";

    public static final String ADV_KEY_GROUP_PURPOSE = "0";
    public static final String ADV_KEY_NETWORK_NAME = "1";
    public static final String ADV_KEY_NETWORK_PASSPHRASE = "2";
    public static final String ADV_KEY_SERVER_PORT = "3";

    public static final String ADV_VALUE_DATA_AVAILABLE = "4";
    public static final String ADV_VALUE_PURPOSE_DATA_TRANSFER = "5";
    public static final String ADV_VALUE_PURPOSE_CONNECTION_CREATION = "6";

}
