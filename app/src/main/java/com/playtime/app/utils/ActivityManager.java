package com.playtime.app.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
public class ActivityManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    public static boolean appInForeground;
    private int numberActivitiesStart = 0;
    private String TAG = this.getClass().getSimpleName();

    /**
     * LifecycleObserver method that shows the app open ad when the app moves to foreground.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        try {
            AppLogger.getInstance().e(TAG, "onMoveToForeground===  app went to foreground");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        try {
            if (numberActivitiesStart == 0) {
                // The application come from background to foreground
                AppLogger.getInstance().e(TAG, "AppController status > onActivityStarted:  app went to foreground");
                if (!appInForeground) {
                    appInForeground = true;
                }
            }
            numberActivitiesStart++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
