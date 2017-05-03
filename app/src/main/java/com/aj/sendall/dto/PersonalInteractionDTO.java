package com.aj.sendall.dto;

import android.net.Uri;

import com.aj.sendall.consts.FileStatus;

/**
 * Created by ajilal on 1/5/17.
 */

public class PersonalInteractionDTO extends FileInfoDTO{
    public int mediaType;
    public FileStatus status;
    public int percentageTransfered;
}
