package com.aj.sendall.services;

import com.aj.sendall.consts.FileStatus;
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.dto.FileInfoDTO;
import com.aj.sendall.dto.PersonalInteractionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajilal on 1/5/17.
 */

public final class PersonalInteractionsService {

    public static List<PersonalInteractionDTO> getFileInteractionsByConnectionId(int connectionId){
        List<PersonalInteractionDTO> dtos = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            PersonalInteractionDTO infoDTO = new PersonalInteractionDTO();
            infoDTO.mediaType = getMediaType(i % 4);
            infoDTO.title = "File kbksdkfgskdbkvskdffgksndllnbsdnbksdkkgrkbdkb" + i;
            infoDTO.size = 10000l;
            infoDTO.status = FileStatus.getFileStatus(i % 4);
            dtos.add(infoDTO);
        }
        return dtos;
    }

    private static int getMediaType(int index){
        switch(index){
            case 0 : return MediaConsts.TYPE_VIDEO;
            case 1 : return MediaConsts.TYPE_AUDIO;
            case 2 : return MediaConsts.TYPE_IMAGE;
            default : return MediaConsts.TYPE_OTHER;
        }
    }
}
