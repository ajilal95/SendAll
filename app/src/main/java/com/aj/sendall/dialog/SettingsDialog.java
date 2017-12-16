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
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.sharedprefs.SharedPrefConstants;
import com.aj.sendall.ui.activity.HomeActivity;

public class SettingsDialog implements AppDialog {
    private AppController appController;
    private Activity activity;
    private AlertDialog dialog;

    private EditText username;

    public SettingsDialog(Activity activity, AppController appController){
        this.appController = appController;
        this.activity = activity;
        refresh();
    }

    public void refresh(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        View v = ((LayoutInflater) appController
                .getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.settings_layout, new LinearLayoutCompat(appController.getApplicationContext()), false);
        alertBuilder.setView(v);

        username = (EditText) v.findViewById(R.id.username);
        username.setFilters(new InputFilter[]{new InputFilter.LengthFilter(SharedPrefConstants.USERNAME_MAX_LEN)});
        username.setText(appController.getUsername());

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
        appController.setUsername(this.username.getText().toString());
    }

    public void show(){
        dialog.show();
    }
}
