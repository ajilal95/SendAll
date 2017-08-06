package com.aj.sendall.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{
    private String baseUriString;
    private final int mediaType;
    private String[] projections;
    private String sortField;
    private final Context context;
    private List<FileInfoDTO> allFileInfoDTOs;
    private List<FileInfoDTO> filteredFileInfoDTOs;

    private Set<FileInfoDTO> selectedItems;

    private ItemSelectableView viewParent;

    public GalleryAdapter(Context context, int mediaType, ItemSelectableView viewParent){
        this.context = context;
        this.mediaType = mediaType;
        this.viewParent = viewParent;
        setSortField(mediaType);
        setProjections(mediaType);
        setBaseContentUri(mediaType);
        allFileInfoDTOs = new ArrayList<>();
        filteredFileInfoDTOs = new ArrayList<>();
        //initAdapter();
    }

    private void setSortField(int mediaType) {
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                sortField = MediaConsts.VIDEO_SORT_FIELD;
                break;
            case MediaConsts.TYPE_AUDIO:
                sortField = MediaConsts.AUDIO_SORT_FIELD;
                break;
            case MediaConsts.TYPE_IMAGE:
                sortField = MediaConsts.IMAGE_SORT_FIELD;
                break;
            case MediaConsts.TYPE_OTHER:
                sortField = MediaConsts.OTHER_SORT_FIELD;
                break;
        }
    }

    private void setProjections(int mediaType){
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                projections = new String[]{
                        MediaStore.Video.VideoColumns._ID,
                        MediaStore.Video.VideoColumns.TITLE,
                        MediaStore.Video.VideoColumns.SIZE
                };
                break;
            case MediaConsts.TYPE_AUDIO:
                projections = new String[]{
                        MediaStore.Audio.AudioColumns._ID,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.SIZE,
                        MediaStore.Audio.AudioColumns.ALBUM_ID
                };
                break;
            case MediaConsts.TYPE_IMAGE:
                projections = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.TITLE,
                        MediaStore.Images.ImageColumns.SIZE
                };
                break;
            case MediaConsts.TYPE_OTHER:
                projections = new String[]{
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.TITLE,
                        MediaStore.Files.FileColumns.SIZE
                };
                break;
        }
    }

    private void setBaseContentUri(int mediaType){
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                baseUriString = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaConsts.TYPE_AUDIO:
                baseUriString = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaConsts.TYPE_IMAGE:
                baseUriString = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaConsts.TYPE_OTHER:
                baseUriString = MediaStore.Files.getContentUri("external").toString();
                break;
        }
    }

    public void initAdapter(){
        selectedItems = new HashSet<>();
        allFileInfoDTOs = getGalleryItemsList();
    }

    private List<FileInfoDTO> getGalleryItemsList() {
        List<FileInfoDTO> fileInfoDTOs = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        String select = getSelectString();
        String[] selectionArgs = null;
        String sort = null;
        if(sortField != null){
            sort = sortField + " DESC";
        }
        Cursor cursor = contentResolver.query(uri, projections, select, selectionArgs, sort);
        if(cursor != null) {
            fileInfoDTOs = new ArrayList<>();
            int idColIndex = cursor.getColumnIndex(projections[0]);
            int titleColIndex = cursor.getColumnIndex(projections[1]);
            int sizeColIndex = cursor.getColumnIndex(projections[2]);
            while(cursor.moveToNext()){
                FileInfoDTO dto = new FileInfoDTO();
                dto.title = cursor.getString(titleColIndex);
                if(dto.title == null){
                    continue;//Cannot list the file
                }
                dto.id = cursor.getInt(idColIndex);
                dto.size = cursor.getLong(sizeColIndex);
                if(mediaType == MediaConsts.TYPE_AUDIO){
                    dto.albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                }
                dto.uri = Uri.parse(baseUriString + '/' + dto.id);
                fileInfoDTOs.add(dto);
            }
            cursor.close();
        }
        return fileInfoDTOs;
    }

    private String getSelectString() {
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
            case MediaConsts.TYPE_AUDIO:
            case MediaConsts.TYPE_IMAGE: return MediaConsts.COL_MEDIA_TYPE + "=" + mediaType;
            case MediaConsts.TYPE_OTHER: return getSelectStringForNonMediaTypes();
        }
        return "";
    }

    private String getSelectStringForNonMediaTypes(){
        String selectString = MediaConsts.COL_MIME_TYPE + " IN " + MediaConsts.OTHER_FILE_MIME_SET;
        return selectString;
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

        CommonUiUtils.setFileThumbnail(
                mediaType,
                context,
                holder.imgVwThumbnail,
                MediaConsts.MEDIA_THUMBNAIL_WIDTH_SMALL,
                MediaConsts.MEDIA_THUMBNAIL_HEIGHT_SMALL,
                fileInfoDTO);

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
                        selectedItems.remove(((FileInfoDTO)v.getTag()).uri);
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
}