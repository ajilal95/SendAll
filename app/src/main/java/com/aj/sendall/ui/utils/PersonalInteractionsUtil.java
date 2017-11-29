package com.aj.sendall.ui.utils;

import com.aj.sendall.controller.AppController;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.db.dto.PersonalInteractionDTO;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class PersonalInteractionsUtil {
    private AppController appController;
    @Inject
    PersonalInteractionsUtil(AppController appController){
        this.appController = appController;
    }

    public List<PersonalInteractionDTO> getFileInteractionsByConnectionId(long connectionId){
        return appController.getPersonalInteractionDTOs(connectionId);
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
