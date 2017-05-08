package com.aj.sendall.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.aj.sendall.R;
import com.aj.sendall.ui.consts.ConnectionsConstants;
import com.aj.sendall.ui.fragment.ConnectionsFragment;

public class SelectReceiversActivity extends AppCompatActivity {
    private LinearLayout base;
    private ConnectionsFragment connectionsFragment;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(ConnectionsConstants.TITLE_RECEIVERS);

        createViews();
        initViews();
        setClickListeners();

        setContentView(base);
    }

    private void createViews() {
        base = new LinearLayout(this);
        searchView = new SearchView(this);
        connectionsFragment = ConnectionsFragment.newInstance(
                this, ConnectionsConstants.PURPOSE_SELECT);
    }

    private void initViews(){
        base.setOrientation(LinearLayout.VERTICAL);
        searchView.setBackgroundResource(R.color.colorPrimary);

        base.addView(searchView);
        base.addView(connectionsFragment.onCreateView(
                (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE),
                base,
                null));
    }

    private void setClickListeners(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                connectionsFragment.filter(newText);
                return true;
            }
        });
    }
}
