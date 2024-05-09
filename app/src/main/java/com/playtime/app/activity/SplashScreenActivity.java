package com.playtime.app.activity;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playtime.app.ApplicationController;
import com.playtime.app.R;
import com.playtime.app.utils.AppLogger;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.utils.CommonUtils;

import java.util.Calendar;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {
    private ImageView ivOfferWall;
    private ApplicationController app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setLightTheme(SplashScreenActivity.this);
        setContentView(R.layout.activity_splash_screen);
        AppLogger.getInstance().e("SYSTEM USER AGENT", "===" + System.getProperty("http.agent"));
        AppLogger.getInstance().e("WEBVIEW USER AGENT", "===" + WebSettings.getDefaultUserAgent(SplashScreenActivity.this));
        app = (ApplicationController) getApplication();
        app.initPlaytimeSDK();

//        try {
//            long currentTime = Calendar.getInstance().getTimeInMillis();
//            Calendar endCal = Calendar.getInstance();
//            endCal.setTime(CommonUtils.formatDate("2024-05-08 00:00:01"));
//            long lastTime = endCal.getTimeInMillis();
////
//            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
////
////            Map<String, UsageStats> aggregatedStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(lastTime, currentTime);
////            for (Map.Entry<String, UsageStats> entry : aggregatedStatsMap.entrySet()) {
////                String key = entry.getKey();
////                UsageStats value = entry.getValue();
////                Log.e("MAP:", "PACKAGE: " + key.toString() + " USAGE: " + (value.getTotalTimeInForeground() / 1000) / 60);
////            }
////            Log.e("===============================:", "===============================:");
//            final List<UsageStats> stats =
//                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
//                            lastTime, currentTime);
//            if (stats != null) {
//                final int statCount = stats.size();
//                for (int i = 0; i < statCount; i++) {
//                    final android.app.usage.UsageStats pkgStats = stats.get(i);
//                    Log.e("FOR LOOP:", "PACKAGE: " + pkgStats.getPackageName() + " USAGE: " + pkgStats.getTotalTimeInForeground() + "=== USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
//                    if (pkgStats.getTotalTimeInForeground() > 0 && pkgStats.getLastTimeUsed() > endCal.getTimeInMillis()) {
////                        Log.e("FOR LOOP:", "PACKAGE: " + pkgStats.getPackageName() + " USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
////                        Log.e("FOR LOOP:", "PACKAGE: " + pkgStats.getPackageName() + " USAGE: " + ((pkgStats.getTotalTimeInForeground() / 1000) / 60) + " USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


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


    public static void setLightTheme(Activity context) {
        Window window = context.getWindow();
        window.setNavigationBarColor(context.getColor(R.color.white));

        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.parseColor("#F4F4F4"));
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            app.destroy();
        }
    }
}