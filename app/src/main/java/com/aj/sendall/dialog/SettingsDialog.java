package com.aj.sendall.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.sharedprefs.SharedPrefConstants;
import com.aj.sendall.sharedprefs.SharedPrefUtil;
import com.aj.sendall.streams.FileUtil;
import com.aj.sendall.ui.activity.ActivityStarter;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.utils.ThisDevice;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

public class SettingsDialog implements AppDialog {
    @Inject
    public AppController appController;

    private SharedPrefUtil sharedPrefUtil;
    private Activity activity;
    private AlertDialog dialog;

    private EditText username;
    private TextView storageLocation;

    public SettingsDialog(Activity activity, SharedPrefUtil sharedPrefUtil){
        this.sharedPrefUtil = sharedPrefUtil;
        this.activity = activity;
        ((ThisApplication)sharedPrefUtil.context.getApplicationContext()).getDaggerInjector().inject(this);
        init();
    }

    @Override
    public void init(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        View v = ((LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.settings_layout, new LinearLayoutCompat(activity), false);
        alertBuilder.setView(v);

        username = (EditText) v.findViewById(R.id.username);
        username.setFilters(new InputFilter[]{new InputFilter.LengthFilter(SharedPrefConstants.USERNAME_MAX_LEN)});
        username.setText(sharedPrefUtil.getUserName());

        View storageLocComponent = ((LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.main_text_sub_text_layout, new LinearLayoutCompat(activity), false);
        ((TextView) storageLocComponent.findViewById(R.id.txtMainText))
                .setText(R.string.str_storage_location);
        storageLocation = ((TextView) storageLocComponent.findViewById(R.id.txtSubText));
        setValueForStorageLocation();
        LinearLayout storageLocLayout = (LinearLayout) v.findViewById(R.id.lnrlytStorageLocation);
        storageLocLayout.removeAllViews();
        storageLocLayout.addView(storageLocComponent);
        storageLocComponent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelctDirDialog();
            }
        });


        Button saveButton = (Button) v.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    save();
                    if(dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                }
            }
        });
        dialog = alertBuilder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SettingsDialog.this.dialog = null;
            }
        });
    }

    private void showSelctDirDialog() {
        if (!ThisDevice.canUseSAF()) {
            try {
                SelectDirectoryDialog dia = new SelectDirectoryDialog(activity, sharedPrefUtil.getStorageDirectory(activity).getActualPath());
                dia.setOnDirSelected(new SelectDirectoryDialog.OnDirSelected() {
                    @Override
                    public void onSelected(String dir) {
                        File file = new File(dir);
                        if (file.canWrite()) {
                            storageLocation.setText(dir);
                            storageLocation.setTag(dir);
                        } else {
                            Toast.makeText(activity, "Selected directory is not writable", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dia.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if(activity instanceof ActivityStarter){
                final int requestCode = 111;
                ActivityStarter as = (ActivityStarter) activity;
                as.setResultListener(new ActivityStarter.ActivityResultListener() {
                    @Override
                    public void onActivityResult(int rCode, int resultCode, Intent resultData) {
                        if(requestCode == rCode && Activity.RESULT_OK == resultCode){
                            Uri treeUri = resultData.getData();
                            try {
                                activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                String path = FileUtil.getFullPathFromTreeUri(treeUri, activity);
                                appController.getTempFileToWrite(1, "Aj.mp3", MediaConsts.TYPE_AUDIO, null);
//                                Uri test = Uri.parse(treeUri.toString());
//                                DocumentFile ch1 = DocumentFile.fromTreeUri(activity,test).createDirectory("AjTest");
//                                DocumentFile ch2 = ch1.createDirectory("AjTest2").createFile(null, "Ajilal.tzx");
//                                Uri.parse()
                                storageLocation.setText(path);
                                if(ThisDevice.canUseTreeUri()) {
                                    storageLocation.setTag(treeUri);
                                } else {
                                    storageLocation.setTag(path);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });

                try {
                    as.startResultReturningActivity(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), requestCode);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
//            activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 111);

        }
    }

    private void setValueForStorageLocation() {
        try {
            storageLocation.setText(sharedPrefUtil.getStorageDirectory(activity).getHumanReadablePath());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean validate(){
        boolean valid = true;
        String username = this.username.getText().toString();
        if(username.isEmpty()){
            valid = false;
        } else {
            for(int i = 0; i < username.length(); i++){
                char ch = username.charAt(i);
                if(!(Character.isAlphabetic(ch) || Character.isDigit(ch))){
                    valid = false;
                    break;
                }
            }
        }
        if(!valid){
            Toast.makeText(activity, "Username can contain 1 to 14 alphabet and numbers", Toast.LENGTH_SHORT).show();
            this.username.requestFocus();
            return false;
        }
        return true;
    }

    private void save(){
        sharedPrefUtil.setUserName(this.username.getText().toString());
        sharedPrefUtil.setStorageDirectory(storageLocation.getTag().toString());
    }

    @Override
    public void show(){
        dialog.show();
    }

    @Override
    public void close(){
        dialog.cancel();
    }
}
