package com.piyush.foodiebay;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by piyush on 14/05/16.
 */
public class BaseActivity extends AppCompatActivity{

    private Toolbar toolbar;

    //to be only accessed through methods, do not change access modifier
    private FrameLayout loadingLayout;

    // Call this on very start of every activity
    /**
     * default toolbar with no title and app's primary color as background
     */
    public void initToolbar() {
        setupToolbar(null, null);
    }

    /**
     * default toolbar with title and app's primary color as background
     *
     * @param title
     */
    public void initToolbar(String title) {
        setupToolbar(title, null);
    }

    private void setupToolbar(String title, String subTitle) {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);

        if (title != null && title.length() > 0) {
            toolbar.setTitle(title);
        }

        if (subTitle != null && subTitle.length() > 0) {
            toolbar.setSubtitle(subTitle);
        }

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
