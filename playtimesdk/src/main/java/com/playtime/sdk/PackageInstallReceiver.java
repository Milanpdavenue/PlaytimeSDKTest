package com.playtime.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("PLAYTIME SDK PACKAGE INSTALL", "onReceive===" + intent.getAction());
    }
}
