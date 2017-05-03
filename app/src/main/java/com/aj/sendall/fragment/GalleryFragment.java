package com.aj.sendall.fragment;

import android.app.Activity;
import android.net.Uri;
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
import com.aj.sendall.adapter.GalleryAdapter;
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.interfaces.ItemSelectableView;
import com.aj.sendall.utils.AppUtils;

import java.util.HashSet;
import java.util.Set;

import static android.widget.Toast.LENGTH_SHORT;


public class GalleryFragment extends Fragment implements ItemSelectableView{
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

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance(Activity parentActivity) {
        GalleryFragment fragment = new GalleryFragment();
        fragment.parentActivity = parentActivity;
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
        lnrLytVideos.getLayoutParams().width = AppUtils.getGallerySectionWidth(parentActivity);
        lnrLytAudios.getLayoutParams().width = AppUtils.getGallerySectionWidth(parentActivity);
        lnrLytImages.getLayoutParams().width = AppUtils.getGallerySectionWidth(parentActivity);
        lnrLytOtherFiles.getLayoutParams().width = AppUtils.getGallerySectionWidth(parentActivity);

        recyclerViewVideos.setHasFixedSize(true);
        GridLayoutManager layoutManagerVideo = new GridLayoutManager(getContext(), 4);
        recyclerViewVideos.setLayoutManager(layoutManagerVideo);
        GalleryAdapter adapterVideo = new GalleryAdapter(getContext(), MediaConsts.TYPE_VIDEO, this);
        recyclerViewVideos.setAdapter(adapterVideo);

        recyclerViewAudios.setHasFixedSize(true);
        GridLayoutManager layoutManagerAudio = new GridLayoutManager(getContext(), 4);
        recyclerViewAudios.setLayoutManager(layoutManagerAudio);
        GalleryAdapter adapterAudio = new GalleryAdapter(getContext(), MediaConsts.TYPE_AUDIO, this);
        recyclerViewAudios.setAdapter(adapterAudio);

        recyclerViewImages.setHasFixedSize(true);
        GridLayoutManager layoutManagerImages = new GridLayoutManager(getContext(), 3);
        recyclerViewImages.setLayoutManager(layoutManagerImages);
        GalleryAdapter adapterImages = new GalleryAdapter(getContext(), MediaConsts.TYPE_IMAGE, this);
        recyclerViewImages.setAdapter(adapterImages);

        recyclerViewOtherFiles.setHasFixedSize(true);
        GridLayoutManager layoutManagerOtherFiles = new GridLayoutManager(getContext(), 4);
        recyclerViewOtherFiles.setLayoutManager(layoutManagerOtherFiles);
        GalleryAdapter adapterOtherFiles = new GalleryAdapter(getContext(), MediaConsts.TYPE_OTHER, this);
        recyclerViewOtherFiles.setAdapter(adapterOtherFiles);

        fltBtnSend.setVisibility(View.GONE);
    }

    private void setClickListeners(){
        fltBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Uri> selectedItems = new HashSet<>();
                for(RecyclerView recyclerView : getRecyclerViews()){
                    selectedItems.addAll(((GalleryAdapter)recyclerView.getAdapter()).getSelectedItemUris());
                }
                Toast.makeText(GalleryFragment.this.getContext(), "" + selectedItems.iterator().next(), LENGTH_SHORT).show();
            }
        });
    }

    private Set<RecyclerView> getRecyclerViews(){
        Set<RecyclerView> recyclerViews = new HashSet<>();
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
}
