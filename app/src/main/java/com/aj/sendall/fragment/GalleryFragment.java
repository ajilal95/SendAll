package com.aj.sendall.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aj.sendall.R;
import com.aj.sendall.adapter.GalleryGridAdapter;
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.interfaces.ItemSelectableView;


public class GalleryFragment extends Fragment implements ItemSelectableView{
    private RecyclerView recyclerViewVideos;
    private RecyclerView recyclerViewAudios;
    private RecyclerView recyclerViewImages;
    private RecyclerView recyclerViewOtherFiles;

    private FloatingActionButton fltBtnSend;

    private int totalNoOfSelections = 0;

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
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
        recyclerViewVideos = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_videos);
        recyclerViewAudios = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_audios);
        recyclerViewImages = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_images);
        recyclerViewOtherFiles = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_other);
        fltBtnSend = (FloatingActionButton) rootView.findViewById(R.id.flt_btn_send_items_gallery);
    }

    private void initView() {
        recyclerViewVideos.setHasFixedSize(true);
        GridLayoutManager layoutManagerVideo = new GridLayoutManager(getContext(), 4);
        recyclerViewVideos.setLayoutManager(layoutManagerVideo);
        GalleryGridAdapter adapterVideo = new GalleryGridAdapter(getContext(), MediaConsts.TYPE_VIDEO, this);
        recyclerViewVideos.setAdapter(adapterVideo);

        recyclerViewAudios.setHasFixedSize(true);
        GridLayoutManager layoutManagerAudio = new GridLayoutManager(getContext(), 4);
        recyclerViewAudios.setLayoutManager(layoutManagerAudio);
        GalleryGridAdapter adapterAudio = new GalleryGridAdapter(getContext(), MediaConsts.TYPE_AUDIO, this);
        recyclerViewAudios.setAdapter(adapterAudio);

        recyclerViewImages.setHasFixedSize(true);
        GridLayoutManager layoutManagerImages = new GridLayoutManager(getContext(), 4);
        recyclerViewImages.setLayoutManager(layoutManagerImages);
        GalleryGridAdapter adapterImages = new GalleryGridAdapter(getContext(), MediaConsts.TYPE_IMAGE, this);
        recyclerViewImages.setAdapter(adapterImages);

        recyclerViewOtherFiles.setHasFixedSize(true);
        GridLayoutManager layoutManagerOtherFiles = new GridLayoutManager(getContext(), 4);
        recyclerViewOtherFiles.setLayoutManager(layoutManagerOtherFiles);
        GalleryGridAdapter adapterOtherFiles = new GalleryGridAdapter(getContext(), MediaConsts.TYPE_OTHER, this);
        recyclerViewOtherFiles.setAdapter(adapterOtherFiles);
    }

    private void setClickListeners(){

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
}
