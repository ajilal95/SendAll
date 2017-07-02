package com.aj.sendall.db.contentprovidutil;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 2/7/17.
 */

@Singleton
public class ContentProviderUtil {
    public Context context;

    @Inject
    public ContentProviderUtil(Context context){

    }
}
