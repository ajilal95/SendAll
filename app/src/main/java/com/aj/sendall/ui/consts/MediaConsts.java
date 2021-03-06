package com.aj.sendall.ui.consts;

import android.net.Uri;
import android.provider.MediaStore;

public class MediaConsts {
    public static Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");
    public static int TITLE_STRING_MAX_LENGTH = 20;

    public static final int TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    public static final int TYPE_AUDIO = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
    public static final int TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    public static final int TYPE_APK = -1;
    public static final int TYPE_OTHER = -2;

    public static final String VIDEO_SORT_FIELD = MediaStore.Video.VideoColumns.DATE_MODIFIED;
    public static final String AUDIO_SORT_FIELD = MediaStore.Audio.AudioColumns.ALBUM_ID;
    public static final String IMAGE_SORT_FIELD = MediaStore.Images.ImageColumns.DATE_MODIFIED;
    public static final String OTHER_SORT_FIELD = MediaStore.Files.FileColumns.DATE_MODIFIED;

    public static final String COL_MIME_TYPE = MediaStore.MediaColumns.MIME_TYPE;
    public static final String COL_FILE_TITLE = MediaStore.Files.FileColumns.TITLE;
    public static final String COL_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE;

    private static final String QUOTED_MIME_TYPE_PDF = "'application/pdf'";
    private static final String QUOTED_MIME_TYPE_XML = "'application/xml'";
    private static final String QUOTED_MIME_TYPE_ZIP1 = "'application/zip'";
    private static final String QUOTED_MIME_TYPE_ZIP2 = "'application/x-compressed-zip'";
    private static final String QUOTED_MIME_TYPE_XLS = "'application/vnd.ms-excel'";
    private static final String QUOTED_MIME_TYPE_TXT = "'text/plain'";
    private static final String QUOTED_MIME_TYPE_TAR = "'application/x-tar'";
    private static final String QUOTED_MIME_TYPE_SGML = "'text/sgml'";
    private static final String QUOTED_MIME_TYPE_RTF = "'application/rtf'";
    private static final String QUOTED_MIME_TYPE_PPT = "'application/vnd.ms-powerpoint'";
    private static final String QUOTED_MIME_TYPE_JAR = "'application/java-archive'";
    private static final String QUOTED_MIME_TYPE_HTML = "'text/html'";
    private static final String QUOTED_MIME_TYPE_XZIP = "'application/x-gzip'";
    private static final String QUOTED_MIME_TYPE_DOC = "'application/msword'";

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

    public static int MEDIA_THUMBNAIL_WIDTH_SMALL = 100;
    public static int MEDIA_THUMBNAIL_HEIGHT_SMALL = 100;

    public static int MEDIA_THUMBNAIL_WIDTH_BIG = 200;
    public static int MEDIA_THUMBNAIL_HEIGHT_BIG = 200;

    public static String SELECT_MEDIA_ACTIVITY_TITLE = "Select files";
    public static String GALLERY_ACTIVITY_TITLE = "Gallery";

}
