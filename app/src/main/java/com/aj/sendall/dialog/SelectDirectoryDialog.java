package com.aj.sendall.dialog;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class SelectDirectoryDialog implements AppDialog {
    private Activity activity;
    private ImageView btnGoBack;
    private ImageView btnNewFolder;
    private TextView txtCurrPath;
    private RecyclerView rWContent;
    private Button btnSelect;

    private String currentPath;

    private AlertDialog dialog;

    interface OnDirSelected{
        void onSelected(String dir);
    }
    private OnDirSelected onDirSelected;

    SelectDirectoryDialog(Activity activity, String currentPath){
        this.activity = activity;
        this.currentPath = currentPath;
        init();
    }

    @Override
    public void init(){
        View view = ((LayoutInflater)activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dir_select_layout, new LinearLayoutCompat(activity), false);
        findViews(view);
        enableViews();
        addClickListeners();
        initView();
        setNewView(StreamManagerFactory.getInstance(activity, currentPath));

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(activity, R.style.AppTheme_NoActionBar));
        alertBuilder.setView(view);

        dialog = alertBuilder.create();
    }

    private void findViews(View view) {
        btnGoBack = (ImageView) view.findViewById(R.id.btn_go_back);
        btnNewFolder = (ImageView) view.findViewById(R.id.btn_new_folder);
        txtCurrPath = (TextView) view.findViewById(R.id.curr_path);
        rWContent = (RecyclerView) view.findViewById(R.id.rw_content);
        btnSelect = (Button) view.findViewById(R.id.btn_select);
    }

    private void enableViews(){
        File file = new File(currentPath);
        boolean canWrite = file.canWrite();
        btnSelect.setEnabled(canWrite);
        btnNewFolder.setEnabled(canWrite);
        File parent = file.getParentFile();
        btnGoBack.setEnabled(parent != null && parent.canRead());
    }

    private void addClickListeners(){
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDirSelected != null){
                    onDirSelected.onSelected(currentPath);
                }
                closePopup();
            }
        });

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StreamManager parent = StreamManagerFactory.getInstance(activity, currentPath).getParent();
                setNewView(parent);
                enableViews();
            }
        });

        btnNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDia();
            }
        });
    }

    private void showCreateFolderDia(){
        final CreateNewThingDialog dia = new CreateNewThingDialog(activity);
        dia.setNewThingNameCaption(R.string.newfoldername_txt);
        dia.setOnCancelListener(new CreateNewThingDialog.OnCancel() {
            @Override
            public void onCancel() {
                dia.close();
            }
        });
        dia.setOnCreateListener(new CreateNewThingDialog.OnCreate() {
            @Override
            public void onCreate(String newFolderName) {
                StreamManager file = StreamManagerFactory.getInstance(activity, currentPath + "/" + newFolderName);
                if(file.exists()){
                    Toast.makeText(activity, "Directory exists", Toast.LENGTH_SHORT).show();
                } else {
                    file.create();
                    if(!file.exists()){
                        Toast.makeText(activity, "Couldn't create directory", Toast.LENGTH_SHORT).show();
                    } else {
                        setNewView(file);
                    }
                    dia.close();
                }
            }
        });
        dia.show();
    }

    private void initView(){
        rWContent.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void close(){
        dialog.cancel();
    }

    void setOnDirSelected(OnDirSelected onDirSelected){
        this.onDirSelected = onDirSelected;
    }

    private void closePopup(){
        dialog.cancel();
    }

    private void setNewView(StreamManager parent){
        try {
            currentPath = parent.getActualPath();
            txtCurrPath.setText(parent.getHumanReadablePath());
            rWContent.setAdapter(new ContentAdapter(parent));
            enableViews();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder>{
        List<StreamManager> folderList = null;

        ContentAdapter(StreamManager thisLocation){
            //only to add file names to the folder getListableDirs
            folderList = thisLocation.getListableDirs();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final StreamManager thisFile = folderList.get(position);
            String fileName = thisFile.getName();
            holder.folderName.setText(fileName);
            holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNewView(thisFile);
                }
            });
        }

        @Override
        public int getItemCount() {
            return folderList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = ((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.dir_layout, new LinearLayoutCompat(parent.getContext()), false);
            return new ViewHolder(view);
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            View v;
            TextView folderName;
            private ViewHolder(View v){
                super(v);
                this.v = v;
                this.folderName = (TextView) v.findViewById(R.id.txt_new_thing_name_caption);
            }
        }
    }
}
