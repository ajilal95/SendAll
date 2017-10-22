package com.aj.sendall.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.activity.SelectReceiversActivity;
import com.aj.sendall.ui.adapter.GalleryAdapter;
import com.aj.sendall.ui.utils.FileTransferUIUtil;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.interfaces.ItemFilterableView;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;


public class GalleryFragment extends Fragment implements ItemSelectableView, ItemFilterableView{
    private LinearLayout lnrLytVideos;
    private LinearLayout lnrLytAudios;
    private LinearLayout lnrLytImages;
    private LinearLayout lnrLytOtherFiles;

    private RecyclerView recyclerViewVideos;
    private RecyclerView recyclerViewAudios;
    private RecyclerView recyclerViewImages;
    private RecyclerView recyclerViewOtherFiles;


    private FloatingActionButton fltBtnSend;

    private Activity parentActivity;
    private int totalNoOfSelections = 0;

    @Inject
    public FileTransferUIUtil fileTransferUIUtil;

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance(Activity parentActivity) {
        GalleryFragment fragment = new GalleryFragment();
        fragment.parentActivity = parentActivity;
        ((ThisApplication)parentActivity.getApplication()).getDaggerInjector().inject(fragment);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        findViews(rootView);
        initView();
        setClickListeners();

        return rootView;
    }

    private void findViews(View rootView) {
        lnrLytVideos = (LinearLayout) rootView.findViewById(R.id.lnrlyt_gallery_section_video);
        lnrLytAudios = (LinearLayout) rootView.findViewById(R.id.lnrlyt_gallery_section_audio);
        lnrLytImages = (LinearLayout) rootView.findViewById(R.id.lnrlyt_gallery_section_images);
        lnrLytOtherFiles = (LinearLayout) rootView.findViewById(R.id.lnrlyt_gallery_section_other);

        recyclerViewVideos = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_videos);
        recyclerViewAudios = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_audios);
        recyclerViewImages = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_images);
        recyclerViewOtherFiles = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_other);
        fltBtnSend = (FloatingActionButton) rootView.findViewById(R.id.flt_btn_send_items_gallery);
    }

    private void initView() {
        lnrLytVideos.getLayoutParams().width = CommonUiUtils.getGallerySectionWidth(parentActivity);
        lnrLytAudios.getLayoutParams().width = CommonUiUtils.getGallerySectionWidth(parentActivity);
        lnrLytImages.getLayoutParams().width = CommonUiUtils.getGallerySectionWidth(parentActivity);
        lnrLytOtherFiles.getLayoutParams().width = CommonUiUtils.getGallerySectionWidth(parentActivity);

        recyclerViewVideos.setHasFixedSize(true);
        GridLayoutManager layoutManagerVideo = new GridLayoutManager(parentActivity, 4);
        recyclerViewVideos.setLayoutManager(layoutManagerVideo);
        GalleryAdapter adapterVideo = new GalleryAdapter(parentActivity, MediaConsts.TYPE_VIDEO, this);
        recyclerViewVideos.setAdapter(adapterVideo);

        recyclerViewAudios.setHasFixedSize(true);
        GridLayoutManager layoutManagerAudio = new GridLayoutManager(parentActivity, 4);
        recyclerViewAudios.setLayoutManager(layoutManagerAudio);
        GalleryAdapter adapterAudio = new GalleryAdapter(parentActivity, MediaConsts.TYPE_AUDIO, this);
        recyclerViewAudios.setAdapter(adapterAudio);

        recyclerViewImages.setHasFixedSize(true);
        GridLayoutManager layoutManagerImages = new GridLayoutManager(parentActivity, 3);
        recyclerViewImages.setLayoutManager(layoutManagerImages);
        GalleryAdapter adapterImages = new GalleryAdapter(parentActivity, MediaConsts.TYPE_IMAGE, this);
        recyclerViewImages.setAdapter(adapterImages);

        recyclerViewOtherFiles.setHasFixedSize(true);
        GridLayoutManager layoutManagerOtherFiles = new GridLayoutManager(parentActivity, 4);
        recyclerViewOtherFiles.setLayoutManager(layoutManagerOtherFiles);
        GalleryAdapter adapterOtherFiles = new GalleryAdapter(parentActivity, MediaConsts.TYPE_OTHER, this);
        recyclerViewOtherFiles.setAdapter(adapterOtherFiles);

        fltBtnSend.setVisibility(View.GONE);
    }

    private void setClickListeners(){
        fltBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<FileInfoDTO> selectedItems = new HashSet<>();
                for(RecyclerView recyclerView : getRecyclerViews()){
                    selectedItems.addAll(((GalleryAdapter)recyclerView.getAdapter()).getSelectedItems());
                }
                FileTransferUIUtil.SendOperationResult result = fileTransferUIUtil.send_items(selectedItems);
                if(result.equals(FileTransferUIUtil.SendOperationResult.SENDING)){
                    Toast.makeText(parentActivity, "Sending...", Toast.LENGTH_SHORT).show();
                } else if(result.equals(FileTransferUIUtil.SendOperationResult.RECEIVER_EMPTY)){
                    Toast.makeText(parentActivity, "Select receivers...", Toast.LENGTH_SHORT).show();
                    goToConnectionsViewToSelectConnections();
                }
            }

            private void goToConnectionsViewToSelectConnections(){
                Intent intent = new Intent(parentActivity, SelectReceiversActivity.class);
                parentActivity.startActivity(intent);
            }
        });
    }

    private List<RecyclerView> getRecyclerViews(){
        List<RecyclerView> recyclerViews = new ArrayList<>();
        recyclerViews.add(recyclerViewVideos);
        recyclerViews.add(recyclerViewAudios);
        recyclerViews.add(recyclerViewImages);
        recyclerViews.add(recyclerViewOtherFiles);
        return recyclerViews;
    }

    @Override
    public void incrementTotalNoOfSelections() {
        totalNoOfSelections++;
        showSendButton();
    }

    @Override
    public void decrementTotalNoOfSelections() {
        totalNoOfSelections--;
        showSendButton();
    }

    private void showSendButton(){
        if(totalNoOfSelections > 0){
            fltBtnSend.setVisibility(View.VISIBLE);
        } else {
            fltBtnSend.setVisibility(View.GONE);
        }
    }

    public void filter(String filterString){
        GalleryAdapter galleryAdapter;

        galleryAdapter = (GalleryAdapter) recyclerViewVideos.getAdapter();
        if(galleryAdapter != null) {
            galleryAdapter.filter(filterString);
            recyclerViewVideos.setAdapter(galleryAdapter);
        }

        galleryAdapter = (GalleryAdapter) recyclerViewAudios.getAdapter();
        if(galleryAdapter != null) {
            galleryAdapter.filter(filterString);
            recyclerViewAudios.setAdapter(galleryAdapter);
        }

        galleryAdapter = (GalleryAdapter) recyclerViewImages.getAdapter();
        if(galleryAdapter != null) {
            galleryAdapter.filter(filterString);
            recyclerViewImages.setAdapter(galleryAdapter);
        }

        galleryAdapter = (GalleryAdapter) recyclerViewOtherFiles.getAdapter();
        if(galleryAdapter != null) {
            galleryAdapter.filter(filterString);
            recyclerViewOtherFiles.setAdapter(galleryAdapter);
        }
    }
}
