package com.aj.sendall.ui.businessservices;

import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.db.dto.PersonalInteractionDTO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 1/5/17.
 */

@Singleton
public final class PersonalInteractionsService {

    @Inject
    public PersonalInteractionsService(){

    }

    public List<PersonalInteractionDTO> getFileInteractionsByConnectionId(int connectionId){
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
