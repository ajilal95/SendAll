package com.aj.sendall.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
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
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.dto.FileInfoDTO;
import com.aj.sendall.interfaces.ItemSelectableView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ajilal on 25/4/17.
 */

public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.ViewHolder>{
    private String baseUriString = "";
    private final int mediaType;
    private String[] projections;
    private String sortField;
    private final Context context;
    private List<FileInfoDTO> fileInfoDTOList;

    private Set<Uri> selectedItemUris = new HashSet<>();

    private int thumbnailWidth = 96;
    private int thumbnailHeight = 96;

    private int titleStringMaxLength = 14;

    private ItemSelectableView viewParent;

    public GalleryGridAdapter(Context context, int mediaType, ItemSelectableView viewParent){
        this.context = context;
        this.mediaType = mediaType;
        this.viewParent = viewParent;
        setSortField(mediaType);
        setProjections(mediaType);
        setBaseContentUri(mediaType);
        initAdapter();
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
            fileInfoDTOList = new ArrayList<>();
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
                if(dto.title.length() > titleStringMaxLength){
                    dto.title = dto.title.substring(0, titleStringMaxLength - 3) + "...";
                }
                long sizeInBytes = cursor.getLong(sizeColIndex);
                dto.size = sizeInBytes;
                if(mediaType == MediaConsts.TYPE_AUDIO){
                    dto.albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                }
                dto.uri = Uri.parse(baseUriString + '/' + dto.id);
                fileInfoDTOList.add(dto);
            }
            cursor.close();
        }
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

    public void refresh(){
        initAdapter();
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
        FileInfoDTO fileInfoDTO = fileInfoDTOList.get(position);
        holder.txtVwFileName.setText(fileInfoDTO.title);
        holder.txtVwFileSize.setText(fileInfoDTO.getSizeString());

        setTumbnail(holder, fileInfoDTO);

        holder.itemView.setTag(fileInfoDTO);
        if(fileInfoDTO.isSelected){
            holder.itemView.setBackgroundResource(R.color.colorPrimary);
        } else {
            holder.itemView.setBackgroundResource(R.color.colorGrey);
        }
    }

    private void setTumbnail(ViewHolder holder, FileInfoDTO fileInfoDTO) {
        switch(mediaType){
            case MediaConsts.TYPE_VIDEO:
                Glide.with(context)
                        .load(fileInfoDTO.uri)
                        .centerCrop()
                        .override(thumbnailWidth, thumbnailHeight)
                        .into(holder.imgVwThumbnail);
                break;
            case MediaConsts.TYPE_IMAGE:
                Glide.with(context)
                            .load(fileInfoDTO.uri)
                            .centerCrop()
                            .override(thumbnailWidth, thumbnailHeight)
                            .into(holder.imgVwThumbnail);
                break;
            case MediaConsts.TYPE_AUDIO:
                Uri albumArtURI = ContentUris.withAppendedId(MediaConsts.ALBUM_ART_URI, fileInfoDTO.albumId);
                Glide.with(context)
                        .load(albumArtURI)
                        .error(R.mipmap.def_media_thumb)
                        .centerCrop()
                        .override(thumbnailWidth, thumbnailHeight)
                        .into(holder.imgVwThumbnail);
                break;
            case MediaConsts.TYPE_OTHER:
                Glide.with(context)
                        .load(R.mipmap.def_other_file_thumb)
                        .centerCrop()
                        .override(thumbnailWidth, thumbnailHeight)
                        .into(holder.imgVwThumbnail);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return fileInfoDTOList != null ? fileInfoDTOList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgVwThumbnail;
        private TextView txtVwFileName;
        private TextView txtVwFileSize;
        public ViewHolder(View view){
            super(view);
            imgVwThumbnail = (ImageView) view.findViewById(R.id.img_vw_thumbnail);
            txtVwFileName = (TextView) view.findViewById(R.id.txt_vw_filename);
            txtVwFileSize = (TextView) view.findViewById(R.id.txt_vw_file_size);
            setClickListeners(view);
        }

        private void setClickListeners(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FileInfoDTO)v.getTag()).isSelected = !((FileInfoDTO)v.getTag()).isSelected;
                    if(((FileInfoDTO)v.getTag()).isSelected){
                        v.setBackgroundResource(R.color.colorPrimary);
                        selectedItemUris.add(((FileInfoDTO)v.getTag()).uri);
                        viewParent.incrementTotalNoOfSelections();
                    } else {
                        v.setBackgroundResource(R.color.colorGrey);
                        selectedItemUris.remove(((FileInfoDTO)v.getTag()).uri);
                        viewParent.decrementTotalNoOfSelections();
                    }
                }
            });
        }
    }
}