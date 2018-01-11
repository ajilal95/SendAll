package com.aj.sendall.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.aj.sendall.controller.AppController;
import com.aj.sendall.sharedprefs.SharedPrefConstants;
import com.aj.sendall.sharedprefs.SharedPrefUtil;

import java.io.File;
import java.io.IOException;

public class SettingsDialog implements AppDialog {
    private SharedPrefUtil sharedPrefUtil;
    private Activity activity;
    private AlertDialog dialog;

    private EditText username;
    private TextView storageLocation;

    public SettingsDialog(Activity activity, SharedPrefUtil sharedPrefUtil){
        this.sharedPrefUtil = sharedPrefUtil;
        this.activity = activity;
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
        try {
            SelectDirectoryDialog dia = new SelectDirectoryDialog(activity, sharedPrefUtil.getStorageDirectory().getCanonicalPath());
            dia.setOnDirSelected(new SelectDirectoryDialog.OnDirSelected() {
                @Override
                public void onSelected(String dir) {
                    File file = new File(dir);
                    if(file.canWrite()) {
                        storageLocation.setText(dir);
                    } else {
                        Toast.makeText(activity, "Selected directory is not writable", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dia.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setValueForStorageLocation() {
        try {
            storageLocation.setText(sharedPrefUtil.getStorageDirectory().getCanonicalPath());
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
        sharedPrefUtil.setStorageDirectory((String)storageLocation.getText());
    }

    @Override
    public void show(){
        dialog.show();
    }

    @Override
    public void setOnClose(final OnClose onClose){
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onClose.onClose();
            }
        });
    }
}
