package com.aj.sendall.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PermissionUtil {
    private Context context;

    @Inject
    PermissionUtil(Context context){
        this.context = context;
    }

    public String[] getDeniedPermissions() {
        Set<String> deniedPermissions = new HashSet<>();
        if (!isWriteExternalStorageGranted()) {
            deniedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!isAccessWifiStateGranted()) {
            deniedPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (!isChangeWifiStateGranted()) {
            deniedPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        }
        if (!isWakeLockGranted()) {
            deniedPermissions.add(Manifest.permission.WAKE_LOCK);
        }
        if (!isInternetGranted()) {
            deniedPermissions.add(Manifest.permission.INTERNET);
        }
        if (!isAccessNetorkStateGranted()) {
            deniedPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (!isChanegNetworkStateGranted()) {
            deniedPermissions.add(Manifest.permission.CHANGE_NETWORK_STATE);
        }
        if (!isWriteSettingsGranted()) {
            deniedPermissions.add(Manifest.permission.WRITE_SETTINGS);
        }

        String[] deniedPermStringArray = null;
        int permsize = deniedPermissions.size();
        if(permsize > 0){
            deniedPermStringArray = new String[permsize];
            deniedPermStringArray = deniedPermissions.toArray(deniedPermStringArray);
        }

        return deniedPermStringArray;
    }

    boolean isPermissionGranted(String permission) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
    }

    boolean isWriteExternalStorageGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean isAccessWifiStateGranted() {
        return isPermissionGranted(Manifest.permission.ACCESS_WIFI_STATE);
    }

    boolean isChangeWifiStateGranted() {
        return isPermissionGranted(Manifest.permission.CHANGE_WIFI_STATE);
    }

    boolean isWakeLockGranted() {
        return isPermissionGranted(Manifest.permission.WAKE_LOCK);
    }

    private boolean isInternetGranted() {
        return isPermissionGranted(Manifest.permission.INTERNET);
    }

    boolean isAccessNetorkStateGranted() {
        return isPermissionGranted(Manifest.permission.ACCESS_NETWORK_STATE);
    }

    boolean isChanegNetworkStateGranted() {
        return isPermissionGranted(Manifest.permission.CHANGE_NETWORK_STATE);
    }

    boolean isWriteSettingsGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_SETTINGS);
    }

    public String getHumanReadablePermString(String permission){
        String hrps = permission;
        if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)){
            hrps = "Write external storage";
        } else if(Manifest.permission.ACCESS_WIFI_STATE.equals(permission)){
            hrps = "Access wifi state";
        } else if(Manifest.permission.CHANGE_WIFI_STATE.equals(permission)){
            hrps = "Change wifi state";
        } else if(Manifest.permission.WAKE_LOCK.equals(permission)){
            hrps = "Wake lock";
        } else if(Manifest.permission.INTERNET.equals(permission)){
            hrps = "Internet";
        } else if(Manifest.permission.ACCESS_NETWORK_STATE.equals(permission)){
            hrps = "Access network state";
        } else if(Manifest.permission.CHANGE_NETWORK_STATE.equals(permission)){
            hrps = "Change network state";
        } else if(Manifest.permission.WRITE_SETTINGS.equals(permission)){
            hrps = "Write settings";
        }
        return hrps;
    }

    public void getPermissions(final Activity activity,
                               final String[] missingPermissions,
                               final int permissionReqCode,
                               String negativeButtonCaption,
                               DialogInterface.OnClickListener negativeButtonAction) {
        if (missingPermissions != null && missingPermissions.length > 0) {
            //Find the permissions that needs an explanation to be shown to the user
            boolean showDia = false;
            for (String perm : missingPermissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    showDia = true;
                    break;
                }
            }

            if (showDia) {
                //needs to show an explanation
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                alertBuilder.setTitle("Need permission");
                StringBuilder permsReqrd = new StringBuilder();
                permsReqrd.append(getHumanReadablePermString(missingPermissions[0]));
                for (int i = 1; i < missingPermissions.length; i++) {
                    permsReqrd.append('\n');
                    permsReqrd.append(getHumanReadablePermString(missingPermissions[i]));
                }
                alertBuilder.setMessage(permsReqrd.toString());
                alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(activity, missingPermissions, permissionReqCode);
                    }
                });
                if(negativeButtonCaption != null && negativeButtonAction != null) {
                    alertBuilder.setNegativeButton("No! Close App", negativeButtonAction);
                }
                alertBuilder.show();
            } else {
                //directly request permissions
                requestPermissions(activity, missingPermissions, permissionReqCode);
            }
        }
    }

    void requestPermissions(Activity activity, String[] perms, int permissionReqCode) {
        ActivityCompat.requestPermissions(activity, perms, permissionReqCode);
    }
}
