package com.aj.sendall.dal.dto;

import com.aj.sendall.dal.enums.FileStatus;

/**
 * Created by ajilal on 1/5/17.
 */

public class PersonalInteractionDTO extends FileInfoDTO{
    public int mediaType;
    public FileStatus status;
    public int percentageTransfered;
}
