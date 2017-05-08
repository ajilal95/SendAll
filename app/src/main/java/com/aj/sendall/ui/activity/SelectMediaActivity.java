package com.aj.sendall.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.aj.sendall.R;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.ui.fragment.GalleryFragment;

public class SelectMediaActivity extends AppCompatActivity {
    LinearLayout base;
    GalleryFragment galleryFragment;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(MediaConsts.SELECT_MEDIA_ACTIVITY_TITLE);

        createViews();
        initViews();
        setClickListeners();
        setContentView(base);
    }

    private void createViews(){
        base = new LinearLayout(this);
        searchView = new SearchView(this);
        galleryFragment = GalleryFragment.newInstance(this);
    }

    private void initViews(){
        base.setOrientation(LinearLayout.VERTICAL);
        base.addView(searchView);
        base.addView(galleryFragment.onCreateView(
                (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE),
                base,
                null));

        searchView.setSubmitButtonEnabled(true);
        searchView.setBackgroundResource(R.color.colorPrimary);
    }

    private void setClickListeners(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                galleryFragment.filter(newText);
                return false;
            }
        });
    }
}
