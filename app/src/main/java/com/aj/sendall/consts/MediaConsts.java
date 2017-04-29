package com.aj.sendall.consts;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by ajilal on 28/4/17.
 */

public class MediaConsts {
    public static Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");

    public static final int TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    public static final int TYPE_AUDIO = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
    public static final int TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    public static final int TYPE_OTHER = -1;

    public static final String VIDEO_SORT_FIELD = MediaStore.Video.VideoColumns.DATE_MODIFIED;
    public static final String AUDIO_SORT_FIELD = MediaStore.Audio.AudioColumns.DATE_MODIFIED;
    public static final String IMAGE_SORT_FIELD = MediaStore.Images.ImageColumns.DATE_MODIFIED;
    public static final String OTHER_SORT_FIELD = MediaStore.Files.FileColumns.DATE_MODIFIED;

    public static final String COL_MIME_TYPE = MediaStore.MediaColumns.MIME_TYPE;
    public static final String COL_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE;

    public static final String QUOTED_MIME_TYPE_PDF = "'application/pdf'";
    public static final String QUOTED_MIME_TYPE_XML = "'application/xml'";
    public static final String QUOTED_MIME_TYPE_ZIP1 = "'application/zip'";
    public static final String QUOTED_MIME_TYPE_ZIP2 = "'application/x-compressed-zip'";
    public static final String QUOTED_MIME_TYPE_XLS = "'application/vnd.ms-excel'";
    public static final String QUOTED_MIME_TYPE_TXT = "'text/plain'";
    public static final String QUOTED_MIME_TYPE_TAR = "'application/x-tar'";
    public static final String QUOTED_MIME_TYPE_SGML = "'text/sgml'";
    public static final String QUOTED_MIME_TYPE_RTF = "'application/rtf'";
    public static final String QUOTED_MIME_TYPE_PPT = "'application/vnd.ms-powerpoint'";
    public static final String QUOTED_MIME_TYPE_JAR = "'application/java-archive'";
    public static final String QUOTED_MIME_TYPE_HTML = "'text/html'";
    public static final String QUOTED_MIME_TYPE_XZIP = "'application/x-gzip'";
    public static final String QUOTED_MIME_TYPE_DOC = "'application/msword'";

    public static final String OTHER_FILE_MIME_SET = '('
            + QUOTED_MIME_TYPE_PDF + ','
            + QUOTED_MIME_TYPE_XML + ','
            + QUOTED_MIME_TYPE_ZIP1 + ','
            + QUOTED_MIME_TYPE_ZIP2 + ','
            + QUOTED_MIME_TYPE_XLS + ','
            + QUOTED_MIME_TYPE_TXT + ','
            + QUOTED_MIME_TYPE_TAR + ','
            + QUOTED_MIME_TYPE_SGML + ','
            + QUOTED_MIME_TYPE_RTF + ','
            + QUOTED_MIME_TYPE_PPT + ','
            + QUOTED_MIME_TYPE_JAR + ','
            + QUOTED_MIME_TYPE_HTML + ','
            + QUOTED_MIME_TYPE_XZIP + ','
            + QUOTED_MIME_TYPE_DOC
            +')';
}
