package com.aj.sendall.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aj.sendall.R;
import com.aj.sendall.adapter.GalleryGridAdapter;
import com.aj.sendall.consts.MediaConsts;


public class GalleryFragment extends Fragment {
    private RecyclerView recyclerViewVideos;
    private RecyclerView recyclerViewAudios;
    private RecyclerView recyclerViewImages;
    private RecyclerView recyclerViewOtherFiles;

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
        return rootView;
    }

    private void findViews(View rootView) {
        recyclerViewVideos = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_videos);
        recyclerViewAudios = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_audios);
        recyclerViewImages = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_images);
        recyclerViewOtherFiles = (RecyclerView) rootView.findViewById(R.id.recycl_vw_gallery_other);
    }

    private void initView() {
        recyclerViewVideos.setHasFixedSize(true);
        GridLayoutManager layoutManagerVideo = new GridLayoutManager(getContext(), 4);
        recyclerViewVideos.setLayoutManager(layoutManagerVideo);
        GalleryGridAdapter adapterVideo = new GalleryGridAdapter(getContext(), MediaConsts.VIDEO);
        recyclerViewVideos.setAdapter(adapterVideo);

        recyclerViewAudios.setHasFixedSize(true);
        GridLayoutManager layoutManagerAudio = new GridLayoutManager(getContext(), 4);
        recyclerViewAudios.setLayoutManager(layoutManagerAudio);
        GalleryGridAdapter adapterAudio = new GalleryGridAdapter(getContext(), MediaConsts.AUDIO);
        recyclerViewAudios.setAdapter(adapterAudio);

        recyclerViewImages.setHasFixedSize(true);
        GridLayoutManager layoutManagerImages = new GridLayoutManager(getContext(), 4);
        recyclerViewImages.setLayoutManager(layoutManagerImages);
        GalleryGridAdapter adapterImages = new GalleryGridAdapter(getContext(), MediaConsts.IMAGE);
        recyclerViewImages.setAdapter(adapterImages);

        recyclerViewOtherFiles.setHasFixedSize(true);
        GridLayoutManager layoutManagerOtherFiles = new GridLayoutManager(getContext(), 4);
        recyclerViewOtherFiles.setLayoutManager(layoutManagerOtherFiles);
        GalleryGridAdapter adapterOtherFiles = new GalleryGridAdapter(getContext(), MediaConsts.OTHER);
        recyclerViewOtherFiles.setAdapter(adapterOtherFiles);
    }
}
