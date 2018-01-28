package com.aj.sendall.ui.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.adapter.helper.AdapterHelper;
import com.aj.sendall.ui.adapter.helper.AdapterHelperFactory;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{
    private final int mediaType;
    private final Context context;
    private List<FileInfoDTO> allFileInfoDTOs;
    private List<FileInfoDTO> filteredFileInfoDTOs;
    private AdapterHelper adapterHelper;

    private Handler handler;

    private Set<FileInfoDTO> selectedItems;

    private ItemSelectableView viewParent;

    public GalleryAdapter(Context context, int mediaType, ItemSelectableView viewParent){
        this.context = context;
        this.mediaType = mediaType;
        this.viewParent = viewParent;
        this.handler = new Handler(context.getMainLooper());
        adapterHelper = AdapterHelperFactory.getInstance(mediaType, context);
        allFileInfoDTOs = new ArrayList<>();
        new FileLoader().execute();
    }

    private void initAdapter(){
        selectedItems = new HashSet<>();
        allFileInfoDTOs = adapterHelper.getData();
    }

    public Set<FileInfoDTO> getSelectedItems(){
        return selectedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = ((LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.gallery_grid_item, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileInfoDTO fileInfoDTO = getFilteredFileInfoDTOs().get(position);
        if(mediaType != MediaConsts.TYPE_IMAGE) {
            holder.txtVwFileName.setVisibility(View.VISIBLE);
            holder.txtVwFileName.setText(CommonUiUtils.getShortTitle(fileInfoDTO.title));
        } else {
            holder.txtVwFileName.setVisibility(View.GONE);
        }
        holder.txtVwFileSize.setText(CommonUiUtils.getFileSizeString(fileInfoDTO.size));
        adapterHelper.setThumbnail(holder.imgVwThumbnail, MediaConsts.MEDIA_THUMBNAIL_WIDTH_SMALL, MediaConsts.MEDIA_THUMBNAIL_HEIGHT_SMALL, fileInfoDTO);

        holder.itemView.setTag(fileInfoDTO);
        CommonUiUtils.setViewSelectedAppearanceRoundEdged(holder.itemView, fileInfoDTO.isSelected);
    }

    @Override
    public int getItemCount() {
        return getFilteredFileInfoDTOs() != null ? getFilteredFileInfoDTOs().size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgVwThumbnail;
        private TextView txtVwFileName;
        private TextView txtVwFileSize;
        ViewHolder(View view){
            super(view);
            imgVwThumbnail = (ImageView) view.findViewById(R.id.gallery_img_vw_thumbnail);
            txtVwFileName = (TextView) view.findViewById(R.id.gallery_txt_vw_filename);
            txtVwFileSize = (TextView) view.findViewById(R.id.gallery_txt_vw_file_size);
            setClickListeners(view);
        }

        private void setClickListeners(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FileInfoDTO)v.getTag()).isSelected = !((FileInfoDTO)v.getTag()).isSelected;
                    CommonUiUtils.setViewSelectedAppearanceRoundEdged(v, ((FileInfoDTO)v.getTag()).isSelected);
                    if(((FileInfoDTO)v.getTag()).isSelected){
                        selectedItems.add((FileInfoDTO)v.getTag());
                        viewParent.incrementTotalNoOfSelections();
                    } else {
                        selectedItems.remove(v.getTag());
                        viewParent.decrementTotalNoOfSelections();
                    }
                }
            });
        }
    }

    public void filter(String filterString){
        if(filterString == null || "".equals(filterString)){
            filteredFileInfoDTOs = null;
        } else {
            filteredFileInfoDTOs = new ArrayList<>();
            for(FileInfoDTO dto : allFileInfoDTOs){
                if(dto.isSelected || dto.title.toLowerCase().contains(filterString.toLowerCase())){
                    filteredFileInfoDTOs.add(dto);
                }
            }
        }
    }

    private List<FileInfoDTO> getFilteredFileInfoDTOs(){
        if(filteredFileInfoDTOs != null){
            return  filteredFileInfoDTOs;
        } else {
            return allFileInfoDTOs;
        }
    }

    private class FileLoader extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            initAdapter();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
            return null;
        }
    }

}