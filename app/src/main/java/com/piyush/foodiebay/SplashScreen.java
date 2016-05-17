package com.piyush.foodiebay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by piyush on 13/05/16.
 */
public class SplashScreen extends AppCompatActivity {

    private FrameLayout splashScreenLayout;
    private ImageView splashBg;
    private ImageView splashLogoFront;

    private Handler handler;
    private Runnable animationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash_screen);

        //Initialize Views
        splashScreenLayout = (FrameLayout) findViewById(R.id.splash_screen_layout);
        splashBg = (ImageView) findViewById(R.id.splash_bg);
        splashLogoFront = (ImageView) findViewById(R.id.splash_logo_front);

        //Bottom-Up Animation for FoodieBay Logo
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.splash_bottom_to_up);
        splashScreenLayout.startAnimation(bottomUp);

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //FoodieBay Logo Zoom Animation
                    Animation animationBus = AnimationUtils.loadAnimation(SplashScreen.this,
                            R.anim.splash_zoom_from_center);
                    splashLogoFront.startAnimation(animationBus);

                    //Background Zoom Full Screen Animation
                    Animation animationBackground = AnimationUtils.loadAnimation(SplashScreen.this,
                            R.anim.splash_zoom_in_full_screen);
                    animationBackground.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            //Intent to start MainActivity on animation completion
                            Intent mainActivityIntent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(mainActivityIntent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                            finish();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    splashBg.startAnimation(animationBackground);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //Start FoodieBay Logo Zoom animation
        handler = new Handler();
        handler.postDelayed(animationRunnable, 500);
    }
}
