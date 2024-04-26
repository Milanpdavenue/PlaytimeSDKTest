package com.playtime.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefs {
    private static SharePrefs instance = null;
    private SharedPreferences pref;
    public static String APP_ID = "APP_ID";
    public static String USER_ID = "USER_ID";
    public static String GAID = "GAID";
    public static String UDID = "UUID";
    public static String FCM_TOKEN = "FCM_TOKEN";
    public static SharePrefs getInstance(Context c) {
        if (instance != null) {
            return instance;
        } else {
            return new SharePrefs(c);
        }
    }

    public SharePrefs(Context context) {
        this.pref = context.getSharedPreferences("PlaytimeSDK", 0);
    }
    public void putString(String key, String val) {
        pref.edit().putString(key, val).apply();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }
}
