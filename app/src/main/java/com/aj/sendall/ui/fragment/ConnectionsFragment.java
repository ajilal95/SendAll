package com.aj.sendall.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.ui.activity.ConnectionCreatorActivity;
import com.aj.sendall.ui.activity.HomeActivity;
import com.aj.sendall.ui.activity.PersonalInteractionsActivity;
import com.aj.sendall.ui.activity.SelectMediaActivity;
import com.aj.sendall.ui.adapter.ConnectionAdapter;
import com.aj.sendall.ui.consts.ConnectionsConstants;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.ui.utils.ConnectionsActivityUtil;
import com.aj.sendall.ui.utils.FileTransferUIUtil;
import com.aj.sendall.ui.interfaces.ItemFilterableView;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class ConnectionsFragment extends Fragment implements ItemFilterableView{
    private ListView lstVwConnections;
    private ConnectionAdapter adapter;
    private FloatingActionButton fltActionButtonAdd;
    private Activity parentActivity;
    private String purpose;

    @Inject
    public ConnectionsActivityUtil connectionsActivityUtil;
    @Inject
    public FileTransferUIUtil fileTransferUIUtil;

    public static ConnectionsFragment newInstance(Activity parentActivity, String purpose){
        ConnectionsFragment connectionsFragment = new ConnectionsFragment();
        connectionsFragment.parentActivity = parentActivity;
        connectionsFragment.purpose = purpose;
        connectionsFragment.adapter = new ConnectionAdapter(null, parentActivity);
        ((ThisApplication)parentActivity.getApplication()).getDaggerInjector().inject(connectionsFragment);

        return connectionsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connections, container, false);
        findViews(rootView);
        setListeners();
        initViews();
        return rootView;
    }
    private void findViews(View rootView){
        lstVwConnections = (ListView) rootView.findViewById(R.id.lst_vw_all_contacts);
        fltActionButtonAdd = (FloatingActionButton) rootView.findViewById(R.id.flt_btn_add_connection);
    }

    private void setListeners(){
        if(ConnectionsConstants.PURPOSE_VIEW.equals(purpose)){
            setListenersForPurposeView();
        } else if(ConnectionsConstants.PURPOSE_SELECT.equals(purpose)){
            setListenersForPurposeSelect();
        }
    }

    private void setListenersForPurposeView(){
        lstVwConnections.setOnItemClickListener(new ListViewItemClickListenerForPurposeView());
        fltActionButtonAdd.setOnClickListener(new FltBtnClickListenerForAdd());
    }

    private void setListenersForPurposeSelect(){
        lstVwConnections.setOnItemClickListener(new ListViewItemClickListenerForPurposeSelect());
        fltActionButtonAdd.setOnClickListener(new FltBtnClickListenerForAdd());
    }

    private void initViews(){
        new ListLoader().execute();
    }

    @Override
    public void filter(String filterString) {
        adapter.setFilterString(filterString);
        if(lstVwConnections != null) {
            lstVwConnections.setAdapter(adapter);
        }
    }

    private class ListLoader extends AsyncTask<Void, Void, List<ConnectionViewData>>{

        @Override
        protected List<ConnectionViewData> doInBackground(Void... params) {
            return connectionsActivityUtil.getAllConnections();
        }

        @Override
        protected void onPostExecute(List<ConnectionViewData> connections) {
            adapter.setDataList(connections);
            lstVwConnections.setAdapter(adapter);
        }
    }

    private class ListViewItemClickListenerForPurposeView implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent personalInteractionView = new Intent(parentActivity, PersonalInteractionsActivity.class);
            personalInteractionView.putExtra("id", ((ConnectionViewData)view.getTag()).profileId);
            personalInteractionView.putExtra("title", ((ConnectionViewData)view.getTag()).profileName);
            parentActivity.startActivity(personalInteractionView);
        }
    }

    private class ListViewItemClickListenerForPurposeSelect implements AdapterView.OnItemClickListener{
        private Set<ConnectionViewData> receivers = new HashSet<>();
        private FltBtnClickListenerForSend buttonClickListenerSend = null;
        private FltBtnClickListenerForAdd buttonClickListenerAdd = null;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ((ConnectionViewData)view.getTag()).isSelected = !((ConnectionViewData)view.getTag()).isSelected;
            CommonUiUtils.setViewSelectedAppearanceSimple(view, ((ConnectionViewData)view.getTag()).isSelected);

            if(((ConnectionViewData)view.getTag()).isSelected){
                receivers.add((ConnectionViewData)view.getTag());
            } else {
                receivers.remove(view.getTag());
            }

            if(receivers.size() > 0){
                fltActionButtonAdd.setOnClickListener(getButtonClickListenerSend());
                fltActionButtonAdd.setImageResource(ConnectionsConstants.FLOATING_BUTTON_IMAGE_SEND);
            } else {
                fltActionButtonAdd.setOnClickListener(getButtonClickListenerAdd());
                fltActionButtonAdd.setImageResource(ConnectionsConstants.FLOATING_BUTTON_IMAGE_ADD_NEW_CONNECTION);
            }
        }

        private FltBtnClickListenerForSend getButtonClickListenerSend(){
            if(buttonClickListenerSend == null){
                buttonClickListenerSend = new FltBtnClickListenerForSend(receivers);
            }
            return buttonClickListenerSend;
        }

        private FltBtnClickListenerForAdd getButtonClickListenerAdd(){
            if(buttonClickListenerAdd == null){
                buttonClickListenerAdd = new FltBtnClickListenerForAdd();
            }
            return buttonClickListenerAdd;
        }
    }

    private class FltBtnClickListenerForAdd implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            Intent connectorIntent = new Intent(parentActivity, ConnectionCreatorActivity.class);
            parentActivity.startActivity(connectorIntent);
        }
    }

    private class FltBtnClickListenerForSend implements View.OnClickListener{
        private Set<ConnectionViewData> receivers;

        FltBtnClickListenerForSend(Set<ConnectionViewData> receivers){
            this.receivers = receivers;
        }

        @Override
        public void onClick(View view) {
            FileTransferUIUtil.SendOperationResult result = fileTransferUIUtil.send_to(receivers);
            if(FileTransferUIUtil.SendOperationResult.URI_EMPTY.equals(result)){
                Intent fileSelectIntent = new Intent(parentActivity, SelectMediaActivity.class);
                parentActivity.startActivity(fileSelectIntent);
            } else if(FileTransferUIUtil.SendOperationResult.SENDING.equals(result)){
                Toast.makeText(parentActivity, "Sending..", Toast.LENGTH_SHORT).show();
                if(!(parentActivity instanceof HomeActivity)){
                    parentActivity.finish();
                }
            }
        }
    }
}
