package com.playtime.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceStatusTracker extends BroadcastReceiver {
    public DeviceStatusTracker() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("PLAYTIME SDK DEVICE STATUS", "DeviceStatusTracker onReceive===" + intent.getAction());
    }
}
