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

class OtherHelper implements AdapterHelper {
    private Context c;
    private String[] projections = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA
    };

    OtherHelper(Context c){
        this.c = c;
    }

    @Override
    public List<FileInfoDTO> getData() {
        return CommonUiUtils.getMediaStoreData(
                c,
                MediaConsts.COL_MIME_TYPE + " IN " + MediaConsts.OTHER_FILE_MIME_SET,
                MediaConsts.OTHER_SORT_FIELD + " DESC",
                projections,
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATA,
                MediaConsts.TYPE_OTHER
        );
    }

    @Override
    public void setThumbnail(ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO) {
        Glide.with(c)
                .load(R.mipmap.def_other_file_thumb)
                .error(R.mipmap.def_other_file_thumb)
                .centerCrop()
                .override(overrideWidth, overrideHeight)
                .into(imgVw);
    }
}
