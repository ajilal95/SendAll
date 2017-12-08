package com.aj.sendall.db.dto;

import com.aj.sendall.db.enums.FileStatus;

public class PersonalInteractionDTO extends FileInfoDTO{
    public long id;
    public int mediaType;
    public FileStatus status;
    public int percentageTransfered;
}
