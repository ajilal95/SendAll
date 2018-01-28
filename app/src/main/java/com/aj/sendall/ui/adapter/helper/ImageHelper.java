package com.aj.sendall.ui.adapter.helper;

import android.content.Context;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.utils.CommonUiUtils;
import com.bumptech.glide.Glide;

import java.util.List;

class ImageHelper implements AdapterHelper {
    private Context c;
    private String[] projections = new String[]{
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.DATA
    };

    ImageHelper(Context c){
        this.c = c;
    }

    @Override
    public List<FileInfoDTO> getData() {
        return CommonUiUtils.getMediaStoreData(
                c,
                MediaConsts.COL_MEDIA_TYPE + "=" + MediaConsts.TYPE_IMAGE,
                MediaConsts.IMAGE_SORT_FIELD + " DESC",
                projections,
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATA,
                MediaConsts.TYPE_IMAGE
        );
    }

    @Override
    public void setThumbnail(ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO) {
        Glide.with(c)
                .load(fileInfoDTO.filePath)
                .error(R.mipmap.def_media_thumb)
                .centerCrop()
                .override(overrideWidth, overrideHeight)
                .into(imgVw);
    }
}
