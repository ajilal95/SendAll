package com.aj.sendall.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.aj.sendall.R;
import com.aj.sendall.adapter.ConnectionListAdapter;
import com.aj.sendall.utils.AppUtils;

public class Connections extends AppCompatActivity {
    private SearchView srchVwAllContacts;
    private ListView lstVwConnections;
    private FloatingActionButton fltBtnAddConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);
        Toolbar toolbar = (Toolbar) findViewById(R.id.titleBar);
        setSupportActionBar(toolbar);

        findViews();
        setListeners();
        initViews();
    }

    private void findViews(){
        srchVwAllContacts = (SearchView) findViewById(R.id.srch_vw_all_contacts);
        lstVwConnections = (ListView) findViewById(R.id.lst_vw_all_contacts);
        fltBtnAddConnection = (FloatingActionButton) findViewById(R.id.flt_btn_add_connection);
    }

    private void setListeners(){
        srchVwAllContacts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        lstVwConnections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "Clicked on an item", Snackbar.LENGTH_LONG).show();
            }
        });
        fltBtnAddConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initViews(){
        //Init lst_vw_all_contacts
        ConnectionListAdapter adapter = new ConnectionListAdapter(AppUtils.getAllConnections(), this);
        lstVwConnections.setAdapter(adapter);
    }

}
