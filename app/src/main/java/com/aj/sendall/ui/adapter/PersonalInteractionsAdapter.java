package com.aj.sendall.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aj.sendall.R;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.dal.dto.PersonalInteractionDTO;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.services.PersonalInteractionsService;
import com.aj.sendall.ui.utils.AppUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ajilal on 1/5/17.
 */

public class PersonalInteractionsAdapter extends RecyclerView.Adapter<PersonalInteractionsAdapter.ViewHolder>{

    private Context context;
    private List<PersonalInteractionDTO> personalInteractionDTOs;
    private int connectionId;

    private ItemSelectableView parentItemSelectable;

    private Set<Uri> selectedItemUris;


    public PersonalInteractionsAdapter(int connectionId, @NonNull Context context, ItemSelectableView parentItemSelectable){
        this.parentItemSelectable = parentItemSelectable;
        this.context = context;
        this.connectionId = connectionId;
        initAdapter();
    }

    private void initAdapter(){
        selectedItemUris = new HashSet<>();
        personalInteractionDTOs = PersonalInteractionsService.getFileInteractionsByConnectionId(connectionId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.pers_inter_item, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PersonalInteractionDTO dto = personalInteractionDTOs.get(position);
        AppUtils.setFileThumbnail(
                dto.mediaType,
                context,
                holder.imgVwItemThumbnail,
                MediaConsts.MEDIA_THUMBNAIL_WIDTH_BIG,
                MediaConsts.MEDIA_THUMBNAIL_HEIGHT_BIG,
                dto);
        holder.txtVwFileName.setText(dto.title);
        holder.txtVwFileSize.setText(AppUtils.getFileSizeString(dto.size));
        holder.itemView.setTag(dto);
        setInteractionViewParams(holder, dto);
    }

    private void setInteractionViewParams(ViewHolder holder, PersonalInteractionDTO dto){
        switch(dto.status){
            case SENDING:
            case SENT:
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).leftMargin = 75;
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).rightMargin = 0;
                ((LinearLayout)holder.itemView).setGravity(Gravity.RIGHT);
                break;
            case RECEIVING:
            case RECEIVED:
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).leftMargin = 0;
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).rightMargin = 75;
                ((LinearLayout)holder.itemView).setGravity(Gravity.LEFT);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return personalInteractionDTOs != null ? personalInteractionDTOs.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgVwItemThumbnail;
        private TextView txtVwFileName;
        private TextView txtVwFileSize;

        public ViewHolder(View view){
            super(view);

            imgVwItemThumbnail = (ImageView) view.findViewById(R.id.img_vw_pers_inter_thumbnail);
            txtVwFileName = (TextView) view.findViewById(R.id.txt_vw_pers_inter_file_name);
            txtVwFileSize = (TextView) view.findViewById(R.id.txt_vw_pers_inter_file_size);

            setClickListeners(view);
        }

        private void setClickListeners(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PersonalInteractionDTO)v.getTag()).isSelected = !((PersonalInteractionDTO)v.getTag()).isSelected;
                    AppUtils.setViewSelectedAppearanceRoundEdged(v, ((PersonalInteractionDTO)v.getTag()).isSelected);
                    if(((PersonalInteractionDTO)v.getTag()).isSelected){
                        selectedItemUris.add(((PersonalInteractionDTO)v.getTag()).uri);
                        parentItemSelectable.incrementTotalNoOfSelections();
                    } else {
                        selectedItemUris.remove(((PersonalInteractionDTO)v.getTag()).uri);
                        parentItemSelectable.decrementTotalNoOfSelections();
                    }
                }
            });
        }
    }
}
