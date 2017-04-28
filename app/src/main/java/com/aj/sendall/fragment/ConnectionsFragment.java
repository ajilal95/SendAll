package com.aj.sendall.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aj.sendall.R;
import com.aj.sendall.adapter.ConnectionListAdapter;
import com.aj.sendall.dto.ConnectionViewData;
import com.aj.sendall.utils.ConnectionsActivityService;

import java.util.List;

public class ConnectionsFragment extends Fragment {
    private ListView lstVwConnections;
    private FloatingActionButton fltActionButtonAdd;
    private boolean showOnlyRecent;

    public static ConnectionsFragment newInstance(boolean showOnlyRecent){
        ConnectionsFragment connectionsFragment = new ConnectionsFragment();
        connectionsFragment.init(showOnlyRecent);

        Bundle args = new Bundle();
        connectionsFragment.setArguments(args);
        return connectionsFragment;
    }
    private void init(boolean showOnlyRecent){
        this.showOnlyRecent = showOnlyRecent;
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
        lstVwConnections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "Clicked on an item", Snackbar.LENGTH_LONG).show();
            }
        });
        fltActionButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initViews(){
        new ListLoader().execute();
    }

    private class ListLoader extends AsyncTask<Void, Void, List<ConnectionViewData>>{

        @Override
        protected List<ConnectionViewData> doInBackground(Void... params) {
            List<ConnectionViewData> connections;
            if(showOnlyRecent){
                connections = ConnectionsActivityService.getRecentConnections();
            } else {
                connections = ConnectionsActivityService.getAllConnections();
            }
            return connections;
        }

        @Override
        protected void onPostExecute(List<ConnectionViewData> connections) {
            lstVwConnections.setAdapter(new ConnectionListAdapter(connections, getContext()));
        }
    }
}
