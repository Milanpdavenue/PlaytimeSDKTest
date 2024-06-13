package com.playtime.app;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.messaging.FirebaseMessaging;
import com.playtime.app.utils.ActivityManager;
import com.playtime.app.utils.AppLogger;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.listeners.OfferWallInitListener;


public class ApplicationController extends Application {
    public Context mContext;

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        FirebaseMessaging.getInstance().subscribeToTopic("global");
        ActivityManager activityManager = new ActivityManager();
        registerActivityLifecycleCallbacks(activityManager);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(activityManager);
    }

    public void initPlaytimeSDK(Context mContext) {
        String userID = "1";
        String applicationKey = "635481e03a09fed8";
        PlaytimeSDK.getInstance().init(mContext, applicationKey, userID, new OfferWallInitListener() {
            @Override
            public void onInitSuccess() {
                AppLogger.getInstance().e("PLAYTIME SDK", "PLAYTIME SDK onInitSuccess======");
            }

            @Override
            public void onAlreadyInitializing() {
                AppLogger.getInstance().e("PLAYTIME SDK", "PLAYTIME SDK onAlreadyInitializing======");
            }

            @Override
            public void onInitFailed(String error) {
                AppLogger.getInstance().e("PLAYTIME SDK", "PLAYTIME SDK onInitFailed======" + error);
            }
        });
    }
    public void destroy() {
        PlaytimeSDK.getInstance().destroy();
    }
}
