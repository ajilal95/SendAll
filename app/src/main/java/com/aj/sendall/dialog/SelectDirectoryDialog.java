package com.aj.sendall.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

import com.aj.sendall.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
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
        setNewView(new File(currentPath));

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
                File parent = new File(currentPath).getParentFile();
                setNewView(parent);
                enableViews();
            }
        });
    }

    private void initView(){
        rWContent.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void show() {
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

    public void setOnDirSelected(OnDirSelected onDirSelected){
        this.onDirSelected = onDirSelected;
    }

    private void closePopup(){
        dialog.cancel();
    }

    private void setNewView(File parent){
        try {
            currentPath = parent.getCanonicalPath();
            txtCurrPath.setText(currentPath);
            rWContent.setAdapter(new ContentAdapter(parent));
            enableViews();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder>{
        List<File> folderList = new ArrayList<>();

        ContentAdapter(File thisLocation){
            //only to add file names to the folder list
            thisLocation.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(!name.startsWith(".")){
                        //do not show hidden folders
                        try {
                            File child = new File(dir.getCanonicalPath() + "/" + name);
                            if(child.exists() && child.isDirectory() && child.canWrite()){
                                folderList.add(child);
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final File thisFile = folderList.get(position);
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
                this.folderName = (TextView) v.findViewById(R.id.txt_foldername);
            }
        }
    }
}
