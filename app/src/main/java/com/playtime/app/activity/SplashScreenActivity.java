package com.playtime.app.activity;

import android.app.Activity;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playtime.app.ApplicationController;
import com.playtime.app.R;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.utils.CommonUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SplashScreenActivity extends AppCompatActivity {
    private ImageView ivOfferWall;
    private ApplicationController app;
    private CountDownTimer timer;

    public void destroy() {
        stopTimer();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    public void setTimer() {
        try {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (timer == null) {
                        Log.e("UsageStats~~~~~~~~~~~~~~", "Timer SET===");
                        Calendar endCal = Calendar.getInstance();
                        Calendar startCal = Calendar.getInstance();
                        try {
                            startCal.setTime(CommonUtils.formatDate("2024-06-08 12:47:09"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        startCal.set(Calendar.MILLISECOND, 0);
                        endCal.set(Calendar.MILLISECOND, 0);
                        long endTime = endCal.getTimeInMillis();
                        long beginTime = startCal.getTimeInMillis();

                        timer = new CountDownTimer((30 * 60 * 1000L), (60 * 1000L)) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                try {
                                    Log.e("CURRENT TOP ACTIVITY", "ON TICK -> ");
                                    UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                                    UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
                                    UsageEvents.Event event = new UsageEvents.Event();
                                    SortedMap<Long, Long> sortedMapUsageEvents = new TreeMap<>();
                                    long totalTimeInForeground = 0;
                                    String currentPackage = null;
                                    long startTime = 0;
                                    while (usageEvents.hasNextEvent()) {
                                        usageEvents.getNextEvent(event);
                                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getPackageName().equals("com.pocket.money.pocketpay")) {
                                            currentPackage = event.getPackageName();
                                            startTime = event.getTimeStamp();
                                        } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && event.getPackageName().equals("com.pocket.money.pocketpay") && event.getPackageName().equals(currentPackage)) {
                                            if (startTime >= beginTime && event.getTimeStamp() <= endTime) {
                                                totalTimeInForeground += event.getTimeStamp() - startTime;
                                                sortedMapUsageEvents.put(event.getTimeStamp(), totalTimeInForeground);
                                                currentPackage = null;
                                                startTime = 0;
                                                Log.e("UsageStats~~~~~~~~~~~~~~", "EVENT ADD == Usage: " + totalTimeInForeground + "=== USED ON: " + CommonUtils.getStringDateTime(event.getTimeStamp()));
                                            }
                                        }
                                    }
//                                  Log.e("UsageStats~~~~~~~~~~~~~~", "TOTAL EVENT == Usage: " + totalTimeInForeground + "=== USED ON: " + CommonUtils.getStringDateTime(event.getTimeStamp()));
                                    final List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
                                    if (stats != null) {
                                        final int statCount = stats.size();
                                        for (int i = 0; i < statCount; i++) {
                                            final android.app.usage.UsageStats pkgStats = stats.get(i);

                                            if (pkgStats.getPackageName().equals("com.pocket.money.pocketpay")) {
                                                Log.e("UsageStats~~~~~~~~~~~~~~" + pkgStats.getPackageName(), "STAT USAGE: " + pkgStats.getTotalTimeInForeground() + "=== USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
                                                if (!sortedMapUsageEvents.isEmpty()) {
                                                    if (pkgStats.getLastTimeUsed() > sortedMapUsageEvents.lastKey()) {
                                                        totalTimeInForeground += (pkgStats.getTotalTimeInForeground() - sortedMapUsageEvents.get(sortedMapUsageEvents.lastKey()));
                                                        Log.e("UsageStats~~~~~~~~~~~~~~" + pkgStats.getPackageName(), "STAT ADD USAGE: " + (pkgStats.getTotalTimeInForeground() - sortedMapUsageEvents.get(sortedMapUsageEvents.lastKey())) + "=== USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
                                                    }
                                                } else {
                                                    totalTimeInForeground += pkgStats.getTotalTimeInForeground();
                                                    Log.e("UsageStats~~~~~~~~~~~~~~" + pkgStats.getPackageName(), "BLANK MAP STAT ADD USAGE: " + pkgStats.getTotalTimeInForeground() + "=== USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
                                                }
                                            }
                                        }
                                    }
                                    Log.e("UsageStats~~~~~~~~~~~~~~", "FINAL == Usage: " + totalTimeInForeground);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFinish() {
                            }
                        }.start();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setLightTheme(SplashScreenActivity.this);
        setContentView(R.layout.activity_splash_screen);
        //CommonUtils.requestUsageStatsPermission(SplashScreenActivity.this,"test","test");
//        setTimer();
//        AppLogger.getInstance().e("SYSTEM USER AGENT", "===" + System.getProperty("http.agent"));
//        AppLogger.getInstance().e("WEBVIEW USER AGENT", "===" + WebSettings.getDefaultUserAgent(SplashScreenActivity.this));
        app = (ApplicationController) getApplication();
        app.initPlaytimeSDK(SplashScreenActivity.this);

//        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        Log.e("=========================", "====================================");
////        long endTime = System.currentTimeMillis();
////        long beginTime = endTime - 1000 * 60 * 60 * 120; // Last 24 hours
//        Calendar endCal = Calendar.getInstance();
//        Calendar startCal = Calendar.getInstance();
//        try {
//            startCal.setTime(CommonUtils.formatDate("2024-06-08 22:16:26"));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        startCal.set(Calendar.MILLISECOND, 0);
//        endCal.set(Calendar.MILLISECOND, 0);
//        long endTime = endCal.getTimeInMillis();
//        long beginTime = startCal.getTimeInMillis();
//
//        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
//        Log.e("UsageStats~~~~~~~~~~~~~~", "beginTime: " + beginTime + ", endTime: " + endTime);
//        Logger.getInstance().e("UsageStats~~~~~~~~~~~~~~", "GET PLAYTIME USAGE FROM: " + CommonUtils.getStringDateTime(beginTime) + " TO: " + CommonUtils.getStringDateTime(endTime));
//
//        Map<String, UsageStats> aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime);
//
//        for (Map.Entry<String, UsageStats> entry : aggregatedStats.entrySet()) {
//            UsageStats usageStats = entry.getValue();
//            if (usageStats.getPackageName().equals("com.games24x7.my11circle.fantasycricket")) {
//                Log.e("UsageStats~~~~~~~~~~~~~~", "AGGREGATE Package: " + usageStats.getPackageName() + "Total Time in Foreground: " + usageStats.getTotalTimeInForeground() + " == Minutes: " + ((usageStats.getTotalTimeInForeground() / 1000) / 60));
//            }
//        }
//
//// Query raw usage events
//        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
//        UsageEvents.Event event = new UsageEvents.Event();
//        long totalTimeInForeground = 0;
//        String currentPackage = null;
//        long startTime = 0;
////        Log.e("UsageStats~~~~~~~~~~~~~~", "EVENT == " + "usageEvents: " + usageEvents);
//        while (usageEvents.hasNextEvent()) {
//            usageEvents.getNextEvent(event);
//            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getPackageName().equals("com.games24x7.my11circle.fantasycricket")) {
//                currentPackage = event.getPackageName();
//                startTime = event.getTimeStamp();
//            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && event.getPackageName().equals("com.games24x7.my11circle.fantasycricket") && event.getPackageName().equals(currentPackage)) {
//                totalTimeInForeground += event.getTimeStamp() - startTime;
//                currentPackage = null;
//                startTime = 0;
//            }
//        }
//        Log.e("UsageStats~~~~~~~~~~~~~~", "EVENT == Usage: " + totalTimeInForeground + " == Minutes: " + ((totalTimeInForeground / 1000) / 60) + " == ON : " + CommonUtils.getStringDateTime(event.getTimeStamp()));
//        List<UsageStats> usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
//        for (UsageStats stat : usageStats) {
////            Log.e("UsageStats~~~~~~~~~~~~~~", "Package: " + usageStats.getPackageName() + ", Total Time in Foreground: " + usageStats.getTotalTimeInForeground());
//            if (stat.getPackageName().equals("com.games24x7.my11circle.fantasycricket")) {
//                Log.e("UsageStats~~~~~~~~~~~~~~", "QUERY Package: " + stat.getPackageName() + " == Usage Time: " + stat.getTotalTimeInForeground() + " == Minutes: " + ((stat.getTotalTimeInForeground() / 1000) / 60));
//            }
//        }
//        Log.e("UsageStats~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~DAY~~~~~~~~~~~~~~~~~~~~~~~~~ ");

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
            stopTimer();
            app.destroy();
        }
    }
}