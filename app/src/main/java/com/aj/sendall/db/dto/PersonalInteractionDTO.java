package com.aj.sendall.db.dto;

import com.aj.sendall.db.enums.FileStatus;

/**
 * Created by ajilal on 1/5/17.
 */

public class PersonalInteractionDTO extends FileInfoDTO{
    public int mediaType;
    public FileStatus status;
    public int percentageTransfered;
}
