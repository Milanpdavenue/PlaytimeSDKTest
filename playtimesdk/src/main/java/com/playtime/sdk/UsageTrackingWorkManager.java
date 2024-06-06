package com.playtime.sdk;

import android.content.Context;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.playtime.sdk.utils.SharePrefs;
import com.playtime.sdk.utils.Logger;
public class UsageTrackingWorkManager extends Worker {
    private static final String TAG = UsageTrackingWorkManager.class.getSimpleName();

    public UsageTrackingWorkManager(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
       if (!SharePrefs.getInstance(getApplicationContext()).getBoolean(SharePrefs.IS_SYNC_IN_PROGRESS)) {
            new SyncDataUtils().syncData(getApplicationContext());
        }
        return Result.success(null);
    }
}
