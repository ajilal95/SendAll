package com.aj.sendall.ui.activity;

import android.content.Intent;

public interface ActivityStarter {
    void setResultListener(ActivityResultListener activityResultListener);
    void startResultReturningActivity(Intent intentE, int requestCode);
    interface ActivityResultListener{
        void onActivityResult(int requestCode, int resultCode,Intent data);
    }
}
