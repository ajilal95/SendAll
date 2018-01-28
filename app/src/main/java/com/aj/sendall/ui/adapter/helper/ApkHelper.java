package com.aj.sendall.ui.adapter.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.consts.MediaConsts;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ApkHelper implements AdapterHelper {
    private Context c;
    private Map<String, ApplicationInfo> appNameToAppInfo = new HashMap<>();
    private PackageManager pm;

    ApkHelper(Context c){
        this.c = c;
        this.pm = c.getPackageManager();
    }

    @Override
    public List<FileInfoDTO> getData() {
        List<FileInfoDTO> data = new ArrayList<>();
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.SIGNATURE_MATCH);
        for(PackageInfo pi : infos){
            String name = pi.applicationInfo.loadLabel(pm).toString();
            String path = pi.applicationInfo.publicSourceDir;
            if(path.startsWith("/data/app")){
                File apkFile = new File(path);
                FileInfoDTO dto = new FileInfoDTO();
                dto.size = apkFile.length();
                dto.mediaType = MediaConsts.TYPE_APK;
                dto.filePath = path;
                dto.title = name;
                data.add(dto);
                appNameToAppInfo.put(name, pi.applicationInfo);
            }
        }

        return data;
    }

    @Override
    public void setThumbnail(ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO) {
        ApplicationInfo applicationInfo = appNameToAppInfo.get(fileInfoDTO.title);
        Drawable ic = applicationInfo.loadIcon(pm);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ((BitmapDrawable) ic).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos);

        Glide.with(c)
                .load(baos.toByteArray())
                .asBitmap()
                .error(R.mipmap.def_other_file_thumb)
                .override(overrideWidth, overrideHeight)
                .into(imgVw);
    }
}
