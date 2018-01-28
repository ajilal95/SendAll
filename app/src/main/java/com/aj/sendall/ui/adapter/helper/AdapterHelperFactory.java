package com.aj.sendall.ui.adapter.helper;

import android.content.Context;
import com.aj.sendall.ui.consts.MediaConsts;

public class AdapterHelperFactory {
    public static AdapterHelper getInstance(int mediaType, Context c){
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                return new VideoHelper(c);
            case MediaConsts.TYPE_AUDIO:
                return new AudioHelper(c);
            case MediaConsts.TYPE_IMAGE:
                return new ImageHelper(c);
            case MediaConsts.TYPE_APK:
                return new ApkHelper(c);
            case MediaConsts.TYPE_OTHER:
                return new OtherHelper(c);
        }
        return null;
    }
}
