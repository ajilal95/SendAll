package com.aj.sendall.consts;

import android.provider.MediaStore;

/**
 * Created by ajilal on 28/4/17.
 */

public class MediaConsts {
    public static final int VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    public static final int AUDIO = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
    public static final int IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    public static final int OTHER = -1;

    public static final String VIDEO_SORT_FIELD = MediaStore.Video.VideoColumns.DATE_MODIFIED;
    public static final String AUDIO_SORT_FIELD = MediaStore.Audio.AudioColumns.DATE_MODIFIED;
    public static final String IMAGE_SORT_FIELD = MediaStore.Images.ImageColumns.DATE_MODIFIED;
    public static final String OTHER_SORT_FIELD = MediaStore.Files.FileColumns.DATE_MODIFIED;
}
