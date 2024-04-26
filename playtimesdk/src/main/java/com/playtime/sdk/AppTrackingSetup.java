package com.playtime.sdk;

import static com.playtime.sdk.utils.Constants.CHECK_DEVICE_STATUS_WORKER;
import static com.playtime.sdk.utils.Constants.CHECK_USAGE_STATUS_WORKER;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

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
        //scheduleJob();
        if (!isWorkScheduled(context, CHECK_USAGE_STATUS_WORKER)) { // check if your CHECK_USAGE_STATUS_WORKER is not already scheduled
            schedulePeriodicWork(context, CHECK_USAGE_STATUS_WORKER); // schedule your work
        }
//        if (!isWorkScheduled(context, CHECK_DEVICE_STATUS_WORKER)) { // check if your CHECK_DEVICE_STATUS_WORKER is not already scheduled
//            scheduleDeviceStatusCheckingWork(context, CHECK_DEVICE_STATUS_WORKER); // schedule your work
//        }
    }

    public static void stopTracking(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(CHECK_USAGE_STATUS_WORKER);
        WorkManager.getInstance(context).cancelAllWorkByTag(CHECK_DEVICE_STATUS_WORKER);
    }

    private static void schedulePeriodicWork(Context context, String tag) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest.Builder photoCheckBuilder =
                new PeriodicWorkRequest.Builder(MyWork.class, 1,
                        TimeUnit.MINUTES)
                        .setConstraints(constraints);
        PeriodicWorkRequest photoCheckWork = photoCheckBuilder.build();
        WorkManager instance = WorkManager.getInstance(context);
        instance.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, photoCheckWork);
        Log.e("PLAYTIME SDK schedulePeriodicWork","schedulePeriodicWork=== Worker is scheduled");
    }

//    private static void scheduleDeviceStatusCheckingWork(Context context, String tag) {
//        Constraints constraints = new Constraints.Builder().setRequiresDeviceIdle(true).build();
//        PeriodicWorkRequest.Builder photoCheckBuilder =
//                new PeriodicWorkRequest.Builder(MyWork.class, 1,
//                        TimeUnit.MINUTES)
//                        .setConstraints(constraints);
//        PeriodicWorkRequest photoCheckWork = photoCheckBuilder.build();
//        WorkManager instance = WorkManager.getInstance(context);
//        instance.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, photoCheckWork);
//    }

    private static boolean isWorkScheduled(Context context, String tag) {
        WorkManager instance = WorkManager.getInstance(context);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
                Log.e("PLAYTIME SDK isWorkScheduled","isWorkScheduled=== "+tag+" Worker is already scheduled");
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

    private static void scheduleJob(Context context) {
        final JobScheduler jobScheduler = (JobScheduler) context.getSystemService(
                Context.JOB_SCHEDULER_SERVICE);

        // The JobService that we want to run
        final ComponentName name = new ComponentName(context, CheckUsageStatusService.class);

        // Schedule the job
        final int result = jobScheduler.schedule(getJobInfo(999, 1, name));
        // If successfully scheduled, log this thing
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.e("scheduleJob===", "Scheduled job successfully!");
        }

    }

    private static JobInfo getJobInfo(final int id, final long hour, final ComponentName name) {
        final long interval = TimeUnit.MINUTES.toMillis(hour); // run every hour
        final boolean isPersistent = true; // persist through boot
        final int networkType = JobInfo.NETWORK_TYPE_ANY; // Requires some sort of connectivity

        final JobInfo jobInfo;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(id, name)
                    .setMinimumLatency(interval)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(id, name)
                    .setPeriodic(interval)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        }

        return jobInfo;
    }
}
