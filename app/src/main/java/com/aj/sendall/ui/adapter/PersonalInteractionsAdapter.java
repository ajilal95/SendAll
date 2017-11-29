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
import com.aj.sendall.db.dto.PersonalInteractionDTO;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.utils.PersonalInteractionsUtil;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PersonalInteractionsAdapter extends RecyclerView.Adapter<PersonalInteractionsAdapter.ViewHolder>{

    private Context context;
    private List<PersonalInteractionDTO> personalInteractionDTOs;
    private long connectionId;

    private ItemSelectableView parentItemSelectable;

    private Set<Uri> selectedItemUris;

    private PersonalInteractionsUtil personalInteractionsUtil;


    public PersonalInteractionsAdapter(long connectionId, @NonNull Context context, ItemSelectableView parentItemSelectable, PersonalInteractionsUtil personalInteractionsUtil){
        this.parentItemSelectable = parentItemSelectable;
        this.context = context;
        this.connectionId = connectionId;
        this.personalInteractionsUtil = personalInteractionsUtil;
        initAdapter();
    }

    private void initAdapter(){
        selectedItemUris = new HashSet<>();
        personalInteractionDTOs = personalInteractionsUtil.getFileInteractionsByConnectionId(connectionId);
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
        CommonUiUtils.setFileThumbnail(
                dto.mediaType,
                context,
                holder.imgVwItemThumbnail,
                MediaConsts.MEDIA_THUMBNAIL_WIDTH_BIG,
                MediaConsts.MEDIA_THUMBNAIL_HEIGHT_BIG,
                dto);
        holder.txtVwFileName.setText(dto.title);
        holder.txtVwFileSize.setText(CommonUiUtils.getFileSizeString(dto.size));
        holder.itemView.setTag(dto);
        setInteractionViewParams(holder, dto);
    }

    private void setInteractionViewParams(ViewHolder holder, PersonalInteractionDTO dto){
        switch(dto.status){
            case SENDING:
            case SENT:
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).leftMargin = 75;
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).rightMargin = 0;
                ((LinearLayout)holder.itemView).setGravity(Gravity.END);
                break;
            case RECEIVING:
            case RECEIVED:
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).leftMargin = 0;
                ((RecyclerView.LayoutParams)holder.itemView.getLayoutParams()).rightMargin = 75;
                ((LinearLayout)holder.itemView).setGravity(Gravity.START);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return personalInteractionDTOs != null ? personalInteractionDTOs.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgVwItemThumbnail;
        private TextView txtVwFileName;
        private TextView txtVwFileSize;

        ViewHolder(View view){
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
                    CommonUiUtils.setViewSelectedAppearanceRoundEdged(v, ((PersonalInteractionDTO)v.getTag()).isSelected);
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
