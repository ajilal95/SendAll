package com.aj.sendall.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.consts.MediaConsts;
import com.aj.sendall.dto.FileInfoDTO;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajilal on 25/4/17.
 */

public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.ViewHolder>{
    private String baseUri = "";
    private int mediaType;
    private String[] projections;
    private String sortField;
    private Context context;
    private List<FileInfoDTO> fileInfoDTOList;

    private int thumbnailWidth = 96;
    private int thumbnailHeight = 96;

    private int titleStringMaxLength = 14;

    public GalleryGridAdapter(Context context, int mediaType){
        this.context = context;
        this.mediaType = mediaType;
        setSortField(mediaType);
        setProjections(mediaType);
        setBaseContentUri(mediaType);
        initAdapter();
    }

    private void setSortField(int mediaType) {
        switch(mediaType){
            case MediaConsts.VIDEO :
                sortField = MediaStore.Video.VideoColumns.DATE_MODIFIED;
                break;
            case MediaConsts.AUDIO:
                sortField = MediaStore.Audio.AudioColumns.DATE_MODIFIED;
                break;
            case MediaConsts.IMAGE:
                sortField = MediaStore.Images.ImageColumns.DATE_MODIFIED;
                break;
            case MediaConsts.OTHER:
                sortField = MediaStore.Files.FileColumns.DATE_MODIFIED;
                break;
        }
    }

    private void setProjections(int mediaType){
        switch(mediaType){
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO :
                projections = new String[]{
                        MediaStore.Video.VideoColumns._ID,
                        MediaStore.Video.VideoColumns.TITLE,
                        MediaStore.Video.VideoColumns.SIZE
                };
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                projections = new String[]{
                        MediaStore.Audio.AudioColumns._ID,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.SIZE
                };
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                projections = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.TITLE,
                        MediaStore.Images.ImageColumns.SIZE
                };
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_NONE:
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
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO :
                baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_NONE:
                baseUri = MediaStore.Files.getContentUri("external").toString();
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
                dto.id = cursor.getInt(idColIndex);
                dto.title = cursor.getString(titleColIndex);
                if(dto.title.length() > titleStringMaxLength){
                    dto.title = dto.title.substring(0, titleStringMaxLength - 3) + "...";
                }
                long sizeInBytes = cursor.getLong(sizeColIndex);
                if(sizeInBytes < 1024l){
                    dto.size = sizeInBytes + " Bytes";
                } else if(sizeInBytes < (1024 * 1024)){
                    dto.size = (sizeInBytes / 1024) + " KB";
                } else if(sizeInBytes < (1024 * 1024 * 1024)){
                    String sizeString = ((float) sizeInBytes) / (1024 * 1024) + "";
                    int indexOfDecimalPoint = sizeString.indexOf('.');
                    sizeString = sizeString.substring(0,
                            indexOfDecimalPoint > 0 ?
                                    Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                                    sizeString.length());
                    dto.size = sizeString + " MB";
                } else {
                    String sizeString = ((float) sizeInBytes) / (1024 * 1024 * 1024) + "";
                    int indexOfDecimalPoint = sizeString.indexOf('.');
                    sizeString = sizeString.substring(0,
                            indexOfDecimalPoint > 0 ?
                                    Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                                    sizeString.length());
                    dto.size = sizeString + " MB";
                }
                fileInfoDTOList.add(dto);
            }
        }
    }

    @NonNull
    private String getSelectString() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + mediaType;
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
        holder.txtVwFileSize.setText(fileInfoDTO.size);

        Uri fileUri = Uri.parse(baseUri + "/" + fileInfoDTO.id);
        Glide.with(context)
                .load(fileUri)
                .centerCrop()
                .override(thumbnailWidth, thumbnailHeight)
                .into(holder.imgVwThumbnail);

        holder.itemView.setTag(fileUri);
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
        }
    }
}
