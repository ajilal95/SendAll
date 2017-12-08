package com.aj.sendall.ui.adapter;

import android.content.Context;
import android.os.Handler;
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
import com.aj.sendall.db.dto.PersonalInteractionDTO;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.FileTransferStatusEvent;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.interfaces.ItemSelectableView;
import com.aj.sendall.ui.utils.CommonUiUtils;
import com.aj.sendall.ui.utils.PersonalInteractionsUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PersonalInteractionsAdapter extends RecyclerView.Adapter<PersonalInteractionsAdapter.ViewHolder>{
    private EventRouter eventRouter = EventRouterFactory.getInstance();
    private Context context;
    private List<PersonalInteractionDTO> personalInteractionDTOs;
    private long connectionId;

    private ItemSelectableView parentItemSelectable;

    private PersonalInteractionsUtil personalInteractionsUtil;
    private Handler handler;
    private Set<ViewHolder> allViewHolders = new HashSet<>();

    public PersonalInteractionsAdapter(long connectionId, @NonNull Context context, ItemSelectableView parentItemSelectable, PersonalInteractionsUtil personalInteractionsUtil, Handler handler){
        this.parentItemSelectable = parentItemSelectable;
        this.context = context;
        this.connectionId = connectionId;
        this.personalInteractionsUtil = personalInteractionsUtil;
        this.handler = handler;
        initAdapter();
    }

    private void initAdapter(){
        personalInteractionDTOs = personalInteractionsUtil.getFileInteractionsByConnectionId(connectionId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.pers_inter_item, parent, false);
        ViewHolder holder = new ViewHolder(rootView);
        allViewHolders.add(holder);
        return holder;
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
        String size;
        if(FileStatus.SENT.equals(dto.status) || FileStatus.RECEIVED.equals(dto.status)) {
            size = CommonUiUtils.getFileSizeString(dto.size);
        } else {
            size = "Incomplete..";
        }
        holder.txtVwFileSize.setText(size);
        holder.itemView.setTag(dto);
        holder.subscribeEvents(dto);
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
                        parentItemSelectable.incrementTotalNoOfSelections();
                    } else {
                        parentItemSelectable.decrementTotalNoOfSelections();
                    }
                }
            });
        }

        EventRouter.Receiver<FileTransferStatusEvent> receiver = null;
        PersonalInteractionDTO currentDTO = null;
        private void subscribeEvents(final PersonalInteractionDTO dto){
            if(!dto.equals(currentDTO)){
                unsubscribeEvents();//un-subscribe on recycling the view
                currentDTO = dto;
            }

            if(FileStatus.RECEIVING.equals(dto.status) || FileStatus.SENDING.equals(dto.status)){
                receiver = new EventRouter.Receiver<FileTransferStatusEvent>() {
                    @Override
                    public void receive(final FileTransferStatusEvent event) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(event.connectionId.equals(dto.id) && txtVwFileName.getText().equals(event.fileName)){
                                    if(event.totalTransferred != FileTransferStatusEvent.COMPLETED){
                                        txtVwFileSize.setText(
                                                CommonUiUtils.getFileSizeString(event.totalTransferred)
                                                + "/" + CommonUiUtils.getFileSizeString(dto.size)
                                        );
                                    } else {
                                        txtVwFileSize.setText(CommonUiUtils.getFileSizeString(dto.size));
                                        //change the status so that the dto won't subscribe again
                                        if(FileStatus.RECEIVING.equals(dto.status)){
                                            dto.status = FileStatus.RECEIVED;
                                        } else {//else status must be SENDING as per the parent conditional
                                            dto.status = FileStatus.SENT;
                                        }
                                        unsubscribeTheListener();
                                    }
                                }
                            }
                        });
                    }

                    private void unsubscribeTheListener(){
                        eventRouter.unsubscribe(FileTransferStatusEvent.class, this);
                    }
                };
                eventRouter.subscribe(FileTransferStatusEvent.class, receiver);
            }
        }

        void unsubscribeEvents(){
            if(receiver != null){
                eventRouter.unsubscribe(FileTransferStatusEvent.class, receiver);
                receiver = null;
            }
        }
    }

    public void unsubscribeFileTransferStatusEvents(){
        for(ViewHolder holder : allViewHolders){
            holder.unsubscribeEvents();
        }
    }
}
