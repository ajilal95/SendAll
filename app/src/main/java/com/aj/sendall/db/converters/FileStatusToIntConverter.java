package com.aj.sendall.db.converters;

import com.aj.sendall.db.enums.FileStatus;

import org.greenrobot.greendao.converter.PropertyConverter;

public class FileStatusToIntConverter implements PropertyConverter<FileStatus, Integer> {
    @Override
    public FileStatus convertToEntityProperty(Integer databaseValue) {
        return FileStatus.getFileStatus(databaseValue);
    }

    @Override
    public Integer convertToDatabaseValue(FileStatus entityProperty) {
        return entityProperty.getIntVal();
    }
}
