package com.aj.sendall.dialog;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aj.sendall.R;

class CreateNewThingDialog implements AppDialog {
    private Activity activity;

    private TextView newThingCaption;
    private EditText newThingName;
    private Button btnCancel;
    private Button btnCreate;

    private AlertDialog dialog;

    CreateNewThingDialog(Activity activity){
        this.activity = activity;
        init();
    }

    @Override
    public void init() {
        View v = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.create_dir_layout, new LinearLayout(activity), false);

        newThingCaption = (TextView) v.findViewById(R.id.txt_new_thing_name_caption);
        newThingName = (EditText) v.findViewById(R.id.edttxt_new_thing_name);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);
        btnCreate = (Button) v.findViewById(R.id.btn_create);

        AlertDialog.Builder b = new AlertDialog.Builder(
                new ContextThemeWrapper(activity, R.style.AppTheme_NoActionBar));
        b.setView(v);
        dialog = b.create();
    }

    @Override
    public void show() {
        dialog.show();
    }

    void setNewThingNameCaption(int resId){
        newThingCaption.setText(resId);
    }

    void setNewThingNameHint(int resId){
        newThingName.setHint(resId);
    }

    void setOnCancelListener(final OnCancel onCancel){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel.onCancel();
            }
        });
    }


    void setOnCreateListener(final OnCreate onCreate){
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreate.onCreate(newThingName.getText().toString());
            }
        });
    }

    @Override
    public void close() {
        dialog.cancel();
    }

    interface OnCancel{
        void onCancel();
    }

    interface OnCreate{
        void onCreate(String newThingName);
    }
}
