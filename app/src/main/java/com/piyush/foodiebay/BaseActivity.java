package com.piyush.foodiebay;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by piyush on 14/05/16.
 */
public class BaseActivity extends AppCompatActivity{

    private Toolbar toolbar;

    //to be only accessed through methods, do not change access modifier
    private FrameLayout loadingLayout;

    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * default toolbar with title and app's primary color as background
     *
     */
    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    /**
     * used to display a loader within content section
     * [IMPORTANT] include "layout_loading" inside main data container of the view
     *
     * @param isEnabled true to enable/show loader, false to disable/hide
     */
    public void displayLoader(boolean isEnabled) {
        //check if view are inflated or not
        if (loadingLayout == null) {
            loadingLayout = (FrameLayout) findViewById(R.id.loading_container);
        }

        //check if layouts available or not
        if (loadingLayout != null) {
            if (isEnabled) {
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
        }
    }
}
