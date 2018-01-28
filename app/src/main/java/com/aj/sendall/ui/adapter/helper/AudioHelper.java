package com.aj.sendall.ui.adapter.helper;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.utils.CommonUiUtils;
import com.bumptech.glide.Glide;

import java.util.List;

class AudioHelper implements AdapterHelper {
    private final Context c;
    private final String[] projections = new String[]{
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM_ID
    };

    AudioHelper(Context c){
        this.c = c;
    }

    @Override
    public List<FileInfoDTO> getData() {
        return CommonUiUtils.getMediaStoreData(
                c,
                MediaConsts.COL_MEDIA_TYPE + "=" + MediaConsts.TYPE_AUDIO,
                MediaConsts.AUDIO_SORT_FIELD + " DESC",
                projections,
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DATA,
                MediaConsts.TYPE_AUDIO
        );
    }

    @Override
    public void setThumbnail(ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO) {
        Uri albumArtURI = ContentUris.withAppendedId(MediaConsts.ALBUM_ART_URI, fileInfoDTO.albumId);
        Glide.with(c)
                .load(albumArtURI)
                .error(R.mipmap.def_media_thumb)
                .centerCrop()
                .override(overrideWidth, overrideHeight)
                .into(imgVw);
    }
}
