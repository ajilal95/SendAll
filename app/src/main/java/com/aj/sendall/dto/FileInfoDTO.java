package com.aj.sendall.dto;

import android.net.Uri;

import com.aj.sendall.consts.FileStatus;

/**
 * Created by ajilal on 26/4/17.
 */

public class FileInfoDTO {
    public boolean isSelected = false;
    public Uri uri = null;
    public int id;
    public String title;
    public long size;
    public int albumId;
}
