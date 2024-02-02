package com.playtime.app.utils;

import android.util.Log;

import com.playtime.app.BuildConfig;

public class AppLogger {
    private static AppLogger instance = new AppLogger();
    private boolean isLogEnabled = false;

    private AppLogger() {
        isLogEnabled = BuildConfig.DEBUG;
    }

    public static AppLogger getInstance() {
        return instance;
    }

    public void d(String a, String b) {
        if (isLogEnabled) Log.d(a, b);
    }

    public void e(String a, String b) {
        if (isLogEnabled) Log.e(a, b);
    }

    public void e_long(String TAG, String message) {
        int maxLogSize = 2000;
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            e(TAG, message.substring(start, end));
        }
    }

    public void e(String a, String b, Throwable t) {
        if (isLogEnabled)
            Log.e(a, b, t);
    }

    public void i(String a, String b) {
        if (isLogEnabled) Log.i(a, b);
    }

    public void w(String a, String b) {
        if (isLogEnabled) Log.w(a, b);
    }

    public void v(String a, String b) {
        if (isLogEnabled) Log.v(a, b);
    }
}
