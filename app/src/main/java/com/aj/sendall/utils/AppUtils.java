package com.aj.sendall.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.aj.sendall.R;
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.dto.FileInfoDTO;
import com.bumptech.glide.Glide;

/**
 * Created by ajilal on 1/5/17.
 */

public class AppUtils {
    private static Point point = null;

    public static String getFileSizeString(long size){
        String sizeStringFinal;
        if(size < 1024l){
            sizeStringFinal = size + " Bytes";
        } else if(size < (1024 * 1024)){
            sizeStringFinal = (size / 1024) + " KB";
        } else if(size < (1024 * 1024 * 1024)){
            String sizeString = ((float) size) / (1024 * 1024) + "";
            int indexOfDecimalPoint = sizeString.indexOf('.');
            sizeString = sizeString.substring(0,
                    indexOfDecimalPoint >= 0 ?
                            Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                            sizeString.length());
            sizeStringFinal = sizeString + " MB";
        } else {
            String sizeString = ((float) size) / (1024 * 1024 * 1024) + "";
            int indexOfDecimalPoint = sizeString.indexOf('.');
            sizeString = sizeString.substring(0,
                    indexOfDecimalPoint >= 0 ?
                            Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                            sizeString.length());
            sizeStringFinal = sizeString + " GB";
        }
        return sizeStringFinal;
    }

    public static String getShortTitle(String title){
        if(title.length() > MediaConsts.TITLE_STRING_MAX_LENGTH){
            title = title.substring(0, MediaConsts.TITLE_STRING_MAX_LENGTH - 3) + "...";
        }
        return title;
    }

    public static void setViewSelectedAppearance(View view, boolean isSelected){
        if(isSelected){
            view.setBackgroundResource(R.drawable.style_item_selected);
        } else {
            view.setBackgroundResource(R.drawable.style_item_not_selected);
        }
    }

    public static void setFileThumbnail(int mediaType, Context context, ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO) {
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                Glide.with(context)
                        .load(fileInfoDTO.uri)
                        .error(R.mipmap.def_media_thumb)
                        .centerCrop()
                        .override(overrideWidth, overrideHeight)
                        .into(imgVw);
                break;
            case MediaConsts.TYPE_IMAGE:
                Glide.with(context)
                        .load(fileInfoDTO.uri)
                        .error(R.mipmap.def_media_thumb)
                        .centerCrop()
                        .override(overrideWidth, overrideHeight)
                        .into(imgVw);
                break;
            case MediaConsts.TYPE_AUDIO:
                Uri albumArtURI = ContentUris.withAppendedId(MediaConsts.ALBUM_ART_URI, fileInfoDTO.albumId);
                Glide.with(context)
                        .load(albumArtURI)
                        .error(R.mipmap.def_media_thumb)
                        .centerCrop()
                        .override(overrideWidth, overrideHeight)
                        .into(imgVw);
                break;
            case MediaConsts.TYPE_OTHER:
                Glide.with(context)
                        .load(R.mipmap.def_other_file_thumb)
                        .error(R.mipmap.def_other_file_thumb)
                        .centerCrop()
                        .override(overrideWidth, overrideHeight)
                        .into(imgVw);
                break;
        }
    }

    public static int getGallerySectionWidth(Activity activity){
        if(point == null) {
            point = new Point();
            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getSize(point);
        }
        return point.x - 15;
    }
}
