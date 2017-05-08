package com.aj.sendall.ui.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.SearchView;

import com.aj.sendall.R;
import com.aj.sendall.ui.consts.ConnectionsConstants;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.fragment.ConnectionsFragment;
import com.aj.sendall.ui.fragment.GalleryFragment;

public class Home extends AppCompatActivity {
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private SearchView searchView;
    private HomePageTabsAdapter tabsAdapter;
    private int currentTabPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        initActivity();
    }

    private void initActivity() {
        findViews();
        initViews();
        setClickListeners();
    }

    private void findViews(){
        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs_vw_home);
        searchView = (SearchView) findViewById(R.id.srch_vw_home);
    }

    private void initViews(){
        tabsAdapter = new HomePageTabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setClickListeners(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int tabPosition = tabLayout.getSelectedTabPosition();
                tabsAdapter.filter(newText, tabPosition);
                return true;
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPos = tab.getPosition();
                searchView.setQuery(tabsAdapter.getFilterString(currentTabPos), false);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //if(mViewPager != null)  mViewPager.setCurrentItem(2, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class HomePageTabsAdapter extends FragmentStatePagerAdapter {
        private ConnectionsFragment connectionsFragment;
        private GalleryFragment galleryFragment;

        private String connectionsFilterString;
        private String galleryFilterString;

        public HomePageTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0 :
                    return getConnectionsFragment();
                case 1 :
                    return getGalleryFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return ConnectionsConstants.TITLE_ALL_CONNECTIONS;
                case 1:
                    return MediaConsts.GALLERY_ACTIVITY_TITLE;
            }
            return null;
        }

        private ConnectionsFragment getConnectionsFragment(){
            if(connectionsFragment == null){
                connectionsFragment = ConnectionsFragment.newInstance(Home.this, ConnectionsConstants.PURPOSE_VIEW);
            }
            return connectionsFragment;
        }

        private GalleryFragment getGalleryFragment(){
            if(galleryFragment == null){
                galleryFragment = GalleryFragment.newInstance(Home.this);
            }
            return galleryFragment;
        }

        public void filter(String filterString, int tabPosition){
            switch(tabPosition){
                case 0 :
                    connectionsFilterString = filterString;
                    getConnectionsFragment().filter(filterString);
                    break;
                case 1 :
                    galleryFilterString = filterString;
                    getGalleryFragment().filter(filterString);
                    break;
            }
        }

        public String getFilterString(int tabPositions){
            switch(tabPositions){
                case 0 : return connectionsFilterString;
                case 1 : return galleryFilterString;
            }
            return "";
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        reset();
    }

    private void reset(){
        searchView.setQuery("", false);
        tabsAdapter = new HomePageTabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabsAdapter);
        mViewPager.setCurrentItem(currentTabPos);
    }
}
