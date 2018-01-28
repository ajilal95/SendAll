package com.aj.sendall.ui.adapter.helper;

import android.content.Context;
import android.widget.ImageView;

import com.aj.sendall.db.dto.FileInfoDTO;

import java.util.List;

public interface AdapterHelper {
    List<FileInfoDTO> getData();
    void setThumbnail(ImageView imgVw, int overrideWidth, int overrideHeight, FileInfoDTO fileInfoDTO);
}
