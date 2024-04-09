package com.playtime.app.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playtime.app.ApplicationController;
import com.playtime.app.R;
import com.playtime.sdk.PlaytimeSDK;

public class SplashScreenActivity extends AppCompatActivity {
    private ImageView ivOfferWall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash_screen);

        ApplicationController app = (ApplicationController) getApplication();
        app.initPlaytimeSDK();

        ivOfferWall = findViewById(R.id.ivOfferWall);
        ivOfferWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlaytimeSDK.getInstance().isInitialized()) {
                    PlaytimeSDK.getInstance().open(SplashScreenActivity.this);
                } else {
                    Toast.makeText(SplashScreenActivity.this, "PlaytimeSDK is not initialized", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}