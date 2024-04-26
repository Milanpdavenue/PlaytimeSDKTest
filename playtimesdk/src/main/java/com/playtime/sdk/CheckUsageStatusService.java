package com.playtime.sdk;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY})
@TargetApi(21)
public class CheckUsageStatusService extends JobService {
    private static final String TAG = CheckUsageStatusService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.e(TAG, "onStartJob() was called" + params.getJobId());
        HandlerThread handlerThread = new HandlerThread("SomeOtherThread");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Running!!!!!!!!!!!!!");
                jobFinished(params, true);
            }
        });

//      Returning true tells the system that, the job is not done right now, we do not know when it will get finished as it might be running in the background. But once the job is finished, we will let you know. Job completion, in this case, is indicated by the jobFinished() method. Till we call this method, system will hold on to all the resources for us.
//      Returning false means we want to end this job entirely right now and the job scheduler will not call onStartJob() when the constraint will be satisfied again
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        Log.e(TAG, "onStopJob() was called" + params.getJobId());
        //If your job is such that you can’t break it into pieces then, finish it on onStopJob() callback by returning true from it. When the next time the constraints will be satisfied, the whole job will be re-run.
        return true;
    }
}
