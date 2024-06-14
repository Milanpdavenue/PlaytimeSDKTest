package com.playtime.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.playtime.sdk.PlaytimeSDK;

public class SharePrefs {
    private static SharePrefs instance = null;
    private SharedPreferences pref;
    public static String APP_ID = "APP_ID";
    public static String USER_ID = "USER_ID";
    public static String GAID = "GAID";
    public static String UDID = "UUID";
    public static String FCM_TOKEN = "FCM_TOKEN";
    public static String IS_SYNC_IN_PROGRESS = "IS_SYNC_IN_PROGRESS";
    public static String TIMER_SYNC_ATTEMPT = "TIMER_SYNC_ATTEMPT";
    public static String IS_CONSENT_GIVEN = "IS_CONSENT_GIVEN";
    public static String CONSENT_TITLE = "CONSENT_TITLE";
    public static String CONSENT_MESSAGE = "CONSENT_MESSAGE";
    public static String ONGOING_OFFER_COUNT = "ONGOING_OFFER_COUNT";
    public static String LAST_SYNC_TIME = "LAST_SYNC_TIME";

    public static SharePrefs getInstance(Context c) {
        if (instance != null) {
            return instance;
        } else {
            return new SharePrefs(c);
        }
    }

    public SharePrefs(Context context) {
        this.pref = context.getSharedPreferences(PlaytimeSDK.getInstance().getPName(), 0);
    }

    public void putString(String key, String val) {
        pref.edit().putString(key, val).apply();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }

    public void putBoolean(String key, boolean val) {
        pref.edit().putBoolean(key, val).apply();
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public void putInt(String key, int val) {
        pref.edit().putInt(key, val).apply();
    }

    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public void putLong(String key, long val) {
        pref.edit().putLong(key, val).apply();
    }

    public long getLong(String key) {
        return pref.getLong(key, 0);
    }
}
