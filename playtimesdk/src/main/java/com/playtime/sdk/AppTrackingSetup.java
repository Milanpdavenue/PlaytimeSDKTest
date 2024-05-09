package com.playtime.sdk;

import static com.playtime.sdk.utils.Constants.CHECK_USAGE_STATUS_WORKER;

import android.content.Context;
import com.playtime.sdk.utils.Logger;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AppTrackingSetup {
    public static void startAppTracking(Context context) {
        if (!isWorkScheduled(context, CHECK_USAGE_STATUS_WORKER)) { // check if your CHECK_USAGE_STATUS_WORKER is not already scheduled
            schedulePeriodicWork(context, CHECK_USAGE_STATUS_WORKER); // schedule your work
        }
    }

    public static void stopTracking(Context context) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(CHECK_USAGE_STATUS_WORKER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void schedulePeriodicWork(Context context, String tag) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest.Builder photoCheckBuilder =
                new PeriodicWorkRequest.Builder(UsageTrackingWorkManager.class, 1,
                        TimeUnit.MINUTES)
                        .setConstraints(constraints);
        PeriodicWorkRequest photoCheckWork = photoCheckBuilder.build();
        WorkManager instance = WorkManager.getInstance(context);
        instance.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, photoCheckWork);
        Logger.getInstance().e("PLAYTIME SDK schedulePeriodicWork","schedulePeriodicWork=== Worker is scheduled");
    }

    private static boolean isWorkScheduled(Context context, String tag) {
        WorkManager instance = WorkManager.getInstance(context);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
                Logger.getInstance().e("PLAYTIME SDK isWorkScheduled","isWorkScheduled=== "+tag+" Worker is already scheduled");
            }
            return running;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
