package com.aj.sendall.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Switch;

import com.aj.sendall.R;
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.event.AppStatusChanged;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.ui.consts.ConnectionsConstants;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.fragment.ConnectionsFragment;
import com.aj.sendall.ui.fragment.GalleryFragment;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class HomeActivity extends AppCompatActivity{
    @Inject
    public AppController appController;

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private SearchView searchView;
    private Switch statusSwitch;
    private HomePageTabsAdapter tabsAdapter;
    private Handler handler;
    private int currentTabPos = 0;

    private static final int PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ThisApplication) getApplication()).getDaggerInjector().inject(this);
        setContentView(R.layout.activity_home);
        checkPerms();
    }

    private void checkPerms() {
        String[] deniedPerms = appController.getDeniedSystemPermissions();
        if (deniedPerms == null || deniedPerms.length == 0) {
            //All permissions granted
            initActivity();
        } else {
            //get the missing permissions
            appController.requestPermissions(
                    this,
                    deniedPerms,
                    PERMISSION_REQ_CODE,
                    "No! Exit",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HomeActivity.this.finish();
                        }
                    }
            );
        }
    }

    private void initActivity() {
        handler = new Handler();
        findViews();
        initViews();
        setClickListeners();
    }

    private void findViews() {
        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs_vw_home);
        searchView = (SearchView) findViewById(R.id.srch_vw_home);
        statusSwitch = (Switch) findViewById(R.id.action_bar_nw_state_switch);
    }

    private void initViews() {
        tabsAdapter = new HomePageTabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        statusSwitch.setChecked(!appController.isSystemFree());
    }

    private void setClickListeners() {
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

        subscribeEvents();
        statusSwitch.setOnCheckedChangeListener(new StateSwitckCheckedChangedListener());
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
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class HomePageTabsAdapter extends FragmentStatePagerAdapter {
        private ConnectionsFragment connectionsFragment;
        private GalleryFragment galleryFragment;

        private String connectionsFilterString;
        private String galleryFilterString;

        HomePageTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getConnectionsFragment();
                case 1:
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

        private ConnectionsFragment getConnectionsFragment() {
            if (connectionsFragment == null) {
                connectionsFragment = ConnectionsFragment.newInstance(HomeActivity.this, ConnectionsConstants.PURPOSE_VIEW);
            }
            return connectionsFragment;
        }

        private GalleryFragment getGalleryFragment() {
            if (galleryFragment == null) {
                galleryFragment = GalleryFragment.newInstance(HomeActivity.this);
            }
            return galleryFragment;
        }

        void filter(String filterString, int tabPosition) {
            switch (tabPosition) {
                case 0:
                    connectionsFilterString = filterString;
                    getConnectionsFragment().filter(filterString);
                    break;
                case 1:
                    galleryFilterString = filterString;
                    getGalleryFragment().filter(filterString);
                    break;
            }
        }

        String getFilterString(int tabPositions) {
            switch (tabPositions) {
                case 0:
                    return connectionsFilterString;
                case 1:
                    return galleryFilterString;
            }
            return "";
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPerms();
        reset();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubscribeEvents();
    }

    private AppStatusListener appStatusListener = new AppStatusListener();
    private void subscribeEvents(){
        appController.registerAppStatusListener(appStatusListener);
    }

    private void unsubscribeEvents(){
        appController.unregisterAppStatusListener(appStatusListener);
    }

    private class AppStatusListener implements EventRouter.Receiver<AppStatusChanged>{
        @Override
        public void receive(AppStatusChanged event) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statusSwitch.setChecked(!appController.isSystemFree());
                }
            });
        }
    }

    private void reset() {
        searchView.setQuery("", false);
        tabsAdapter = new HomePageTabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabsAdapter);
        mViewPager.setCurrentItem(currentTabPos);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Set<String> deniedPerms = new HashSet<>();
        if (requestCode == PERMISSION_REQ_CODE) {
            for(int i = 0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    deniedPerms.add(appController.getHumanReadablePermString(permissions[i]));
                }
            }

            if(deniedPerms.isEmpty()){
                //All permissions granted. Now proceed
                initActivity();
            } else {
               finish();
            }
        }
    }

    private class StateSwitckCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.isPressed()) {
                if (!isChecked) {
                    appController.setSystemIdle();
                } else {
                    appController.showToggleReceiverNotification();
                }
            }

        }
    }

}
