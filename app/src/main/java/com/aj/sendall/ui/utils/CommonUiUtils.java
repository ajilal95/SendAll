package com.aj.sendall.ui.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.consts.MediaConsts;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CommonUiUtils {
    private static Point point = null;

    public static String getFileSizeString(long size){
        String sizeStringFinal;
        if(size < 1024L){
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

    public static void setViewSelectedAppearanceRoundEdged(View view, boolean isSelected){
        if(isSelected){
            view.setBackgroundResource(R.drawable.style_item_selected);
        } else {
            view.setBackgroundResource(R.drawable.style_item_not_selected);
        }
    }

    public static void setViewSelectedAppearanceSimple(View view, boolean isSelected){
        if(isSelected){
            view.setBackgroundResource(R.color.colorDarkGrey);
        } else {
            view.setBackgroundResource(R.color.colorWhite);
        }
    }

    public static void setViewActive(View view, boolean active){
        if(active){
            view.setBackgroundResource(R.color.colorSteelBlue);
        } else {
            view.setBackgroundResource(R.color.colorWhite);
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

    public static List<FileInfoDTO> getMediaStoreData(Context context, String selectClause, String sortField, String[] projection, String idCol, String sizeCol, String pathCol, int mediaType){
        List<FileInfoDTO> fileInfoDTOs = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = contentResolver.query(uri, projection, selectClause, null, sortField);
        if(cursor != null) {
            fileInfoDTOs = new ArrayList<>();
            int idColIndex = cursor.getColumnIndex(idCol);
            int sizeColIndex = cursor.getColumnIndex(sizeCol);
            int pathColIndex = cursor.getColumnIndex(pathCol);
            while(cursor.moveToNext()){
                FileInfoDTO dto = new FileInfoDTO();
                dto.id = cursor.getInt(idColIndex);
                dto.size = cursor.getLong(sizeColIndex);
                dto.mediaType = mediaType;
                if(mediaType == MediaConsts.TYPE_AUDIO){
                    dto.albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                }
                dto.filePath = cursor.getString(pathColIndex);
                dto.title = dto.filePath.substring(dto.filePath.lastIndexOf('/') + 1);
                fileInfoDTOs.add(dto);
            }
            cursor.close();
        }
        return fileInfoDTOs;
    }
}
