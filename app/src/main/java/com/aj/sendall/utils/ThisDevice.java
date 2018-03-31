package com.aj.sendall.utils;

import android.os.Build;

public class ThisDevice {
    public static boolean canUseSAF(){
        return Build.VERSION.SDK_INT >= 19;
    }

    public static boolean canUseTreeUri(){
        return Build.VERSION.SDK_INT >= 21;
    }
}
