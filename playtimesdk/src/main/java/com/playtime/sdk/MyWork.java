package com.playtime.sdk;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.playtime.sdk.database.AppDatabase;
import com.playtime.sdk.database.PartnerAppTargets;
import com.playtime.sdk.database.PartnerAppTargetsDao;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.database.PartnerAppsDao;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;
import com.playtime.sdk.utils.SharePrefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWork extends Worker {
    public static final String RECEIVE_TASK_KEY = "receive_massage";
    private static final String TAG = MyWork.class.getSimpleName();

    public MyWork(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.e(TAG, "PLAYTIME SDK Work Manager Constructor Called==" + workerParams.getId());
    }


    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "PLAYTIME SDK Work Manager doWork() Called==");
        // get Input tasks by the User or work manager

//        Data data = getInputData();
        // notification function
        // Notifactionshow("mr.appbuilder",data.getString(TASK_KEY));
        // set a response when the Task is done
//        Data data1 = new Data.Builder().putString(RECEIVE_TASK_KEY, "mr.appbuilder Receive data Successfully and Thank you ").build();
        Log.e("=============================", "=================SYNC LOCAL DATA WITH SERVER====================");
        Encryption cipher = new Encryption();
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("ORANGE", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.APP_ID));
            jObject.put("PAKODI", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.GAID));
            jObject.put("GTHYJU", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.UDID));
            jObject.put("ERVTUH", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.USER_ID));
            jObject.put("POINTG", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.FCM_TOKEN));
            jObject.put("BGHNH5", Build.MODEL);
            jObject.put("CVBNHG", Build.VERSION.RELEASE);
            jObject.put("QAWSED", BuildConfig.VERSION_NAME);
            jObject.put("GHJKUF", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Log.e("getOnGoingApps ORIGINAL ==>", jObject.toString());
            Log.e("getOnGoingApps ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            Call<ApiResponse> call = apiService.getOnGoingApps(SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // return the task status
                    HandlerThread handlerThread = new HandlerThread("SomeOtherThread");
                    handlerThread.start();
                    Handler handler = new Handler(handlerThread.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(response.body().getEncrypt())), ResponseModel.class);
                                PartnerAppsDao dao = AppDatabase.getInstance(getApplicationContext()).partnerAppsDao();
                                PartnerAppTargetsDao daoTarget = AppDatabase.getInstance(getApplicationContext()).partnerAppTargetsDao();
                                ArrayList<PartnerApps> listLocalApps = (ArrayList<PartnerApps>) dao.getAll();
                                if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                                    Log.e("getOnGoingApps SUCCESS ==>", responseModel.toString());
                                    Log.e("getOnGoingApps OFFERS ==>", "SIZE: " + responseModel.getOffers().size() + " DATA: " + responseModel.getOffers().toString());
                                    // If user uninstall and reinstall app, sync all data from server
                                    Log.e("LOCAL OFFERS ==>", "SIZE: " + listLocalApps.size() + " DATA: " + listLocalApps.toString());
                                    if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                        if (listLocalApps.isEmpty()) {
                                            // Insert all
                                            dao.insertAll(responseModel.getOffers());
                                            listLocalApps.addAll(responseModel.getOffers());
                                            Log.e("NO LOCAL OFFERS ==>", "INSERT ALL: ");
                                            for (int i = 0; i < responseModel.getOffers().size(); i++) {
                                                if (responseModel.getOffers().get(i).data != null && !responseModel.getOffers().get(i).data.isEmpty()) {
                                                    daoTarget.insertAll(responseModel.getOffers().get(i).data);
                                                    Log.e("NO LOCAL OFFERS ==>", "INSERT TARGETS: FOR OFFER ID:" + responseModel.getOffers().get(i).task_offer_id);
                                                }
                                            }
                                        } else { // Sync offer data & target data with local DB
                                            Log.e("SYNC OFFERS ==>", "SYNC OFFERS ");
                                            for (int i = 0; i < responseModel.getOffers().size(); i++) {
                                                try {
                                                    boolean isChangeNeeded = false;
                                                    int pos = -1;
                                                    for (int j = 0; j < listLocalApps.size(); j++) {
                                                        try {
                                                            if (responseModel.getOffers().get(i).task_offer_id == listLocalApps.get(j).task_offer_id) {
                                                                for (int k = 0; k < responseModel.getOffers().get(i).data.size(); k++) {
                                                                    if (!CommonUtils.isStringNullOrEmpty(responseModel.getOffers().get(i).data.get(k).is_completed)
                                                                            && responseModel.getOffers().get(i).data.get(k).is_completed.equals("0")
                                                                            && k > 0 && !CommonUtils.isStringNullOrEmpty(responseModel.getOffers().get(i).data.get(k - 1).is_completed)
                                                                            && responseModel.getOffers().get(i).data.get(k - 1).is_completed.equals("1")) {
                                                                        isChangeNeeded = true;
                                                                        listLocalApps.get(j).last_completion_time = responseModel.getOffers().get(i).data.get(k - 1).completion_time;
                                                                        Log.e("SET LAST COMPLETION TIME ==>", "SET LAST COMPLETION TIME FROM TARGET");
                                                                        pos = j;
                                                                        break;
                                                                    }
                                                                }
                                                                if (responseModel.getOffers().get(i).is_installed == 1 && listLocalApps.get(j).is_installed == 0) {
                                                                    isChangeNeeded = true;
                                                                    listLocalApps.get(j).is_installed = responseModel.getOffers().get(i).is_installed;
                                                                    listLocalApps.get(j).is_completed = responseModel.getOffers().get(i).is_completed;
                                                                    listLocalApps.get(j).install_time = responseModel.getOffers().get(i).install_time;
                                                                    if (CommonUtils.isStringNullOrEmpty(listLocalApps.get(j).last_completion_time)) {
                                                                        listLocalApps.get(j).last_completion_time = responseModel.getOffers().get(i).install_time;
                                                                    }
                                                                    Log.e("SET LAST COMPLETION TIME ==>", "SET LAST COMPLETION TIME AS INSTALL TIME");
                                                                    pos = j;
                                                                    break;
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    if (isChangeNeeded) {
                                                        dao.update(listLocalApps.get(pos));
                                                        Log.e("UPDATE OFFER", "UPDATE OFFER: " + listLocalApps.get(pos));
                                                    }
                                                    daoTarget.deleteMultipleByTaskOfferId(responseModel.getOffers().get(i).task_offer_id);
                                                    Log.e("DELETE TARGET FOR ", "DELETE TARGET FOR OFFER ID : " + responseModel.getOffers().get(i).task_offer_id);
                                                    daoTarget.insertAll(responseModel.getOffers().get(i).data);
                                                    Log.e("ADD TARGETS ", "ADD TARGET FOR OFFER ID : " + responseModel.getOffers().get(i).task_offer_id);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                    syncData(listLocalApps, CommonUtils.formatDate(responseModel.getCurrentTime()).getTime(), cipher, dao, daoTarget);
                                } else {
                                    Log.e("getOnGoingApps ERROR ==>", responseModel.toString());
                                    syncData(listLocalApps, CommonUtils.formatDate(responseModel.getCurrentTime()).getTime(), cipher, dao, daoTarget);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0);

                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    if (!call.isCanceled()) {
                        // CommonUtils.Notify(activity, activity.getString(R.string.app_name), Constants.msg_Service_Error, false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("getOnGoingApps Result.success ==>", "WORK MANAGER SUCCESS");
//        return Result.success(data1);
        return Result.success(null);
    }

    private void syncData(ArrayList<PartnerApps> listLocalApps, long currentTime, Encryption cipher, PartnerAppsDao dao, PartnerAppTargetsDao daoTarget) {
        try {
            Log.e("=============================", "=================GET USAGE====================");
            if (listLocalApps != null && !listLocalApps.isEmpty()) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
//                long currentTime = CommonUtils.formatDate(responseModel.getCurrentTime()).getTime();
                for (int i = 0; i < listLocalApps.size(); i++) {
                    try {
                        if (listLocalApps.get(i).is_completed == 0 && !CommonUtils.isStringNullOrEmpty(listLocalApps.get(i).last_completion_time)) {
                            if (listLocalApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME)) {
                                long lastTime = CommonUtils.formatDate(listLocalApps.get(i).last_completion_time).getTime();

                                Map<String, UsageStats> aggregatedStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(lastTime, currentTime);

                                // Get stats for particular package as follows:
                                UsageStats usageStats = aggregatedStatsMap.get(listLocalApps.get(i).package_id);
                                if (usageStats != null) {
                                    listLocalApps.get(i).usage_duration = (usageStats.getTotalTimeInForeground() / 1000) / 60;
                                    Log.e("GET USAGE PLAYTIME==>", "OFFER ID: " + listLocalApps.get(i).task_offer_id + listLocalApps.get(i).usage_duration);
                                }
                            } else if (listLocalApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                                Calendar endCal = Calendar.getInstance();
                                endCal.setTime(CommonUtils.formatDate(listLocalApps.get(i).last_completion_time));
                                long lastTime = endCal.getTimeInMillis();
                                final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, lastTime, currentTime);
                                int dayCount = 0;
                                if (stats != null) {
                                    for (int t = 0; t < stats.size(); t++) {
                                        final UsageStats pkgStats = stats.get(t);
                                        if (pkgStats.getPackageName().equals(listLocalApps.get(i).package_id)
                                                && pkgStats.getTotalTimeInForeground() > 0 && pkgStats.getLastTimeUsed() > endCal.getTimeInMillis()) {
                                            dayCount++;
                                        }
                                    }
                                }
                                listLocalApps.get(i).usage_duration = dayCount;
                                Log.e("GET USAGE DAY==>", "OFFER ID: " + listLocalApps.get(i).task_offer_id + listLocalApps.get(i).usage_duration);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.e("=============================", "=================SYNC USAGE====================");
                Log.e("NOW SYNC USAGE STATUS ==>", listLocalApps.toString());
                // NOW SEND ALL DATA TO SERVER
                JSONObject jObject = new JSONObject();
                jObject.put("ORANGE", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.APP_ID));
                jObject.put("PAKODI", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.GAID));
                jObject.put("GTHYJU", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.UDID));
                jObject.put("ERVTUH", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.USER_ID));
                jObject.put("POINTG", SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.FCM_TOKEN));
                jObject.put("BGHNH5", Build.MODEL);
                jObject.put("CVBNHG", Build.VERSION.RELEASE);
                jObject.put("QAWSED", BuildConfig.VERSION_NAME);
                jObject.put("THYJUK", new JSONArray(listLocalApps));
                jObject.put("GHJKUF", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));

                int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
                jObject.put("RANDOM", n);
                ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                Log.e("updatePlaytime ORIGINAL ==>", jObject.toString());
                Log.e("updatePlaytime ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
                Call<ApiResponse> call = apiService.updatePlaytime(SharePrefs.getInstance(getApplicationContext()).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        try {
                            ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(response.body().getEncrypt())), ResponseModel.class);
                            Log.e("updatePlaytime ", "updatePlaytime RESPONSE: " + responseModel.toString());
                            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                                Log.e("updatePlaytime SUCCESS ", "updatePlaytime STATUS_SUCCESS: ");
                                if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                    Log.e("updatePlaytime SUCCESS ", "updatePlaytime UPDATED OFFERS: " + responseModel.getOffers().toString());
                                    for (int i = 0; i < responseModel.getOffers().size(); i++) {
                                        try {
                                            dao.update(responseModel.getOffers().get(i));
                                            daoTarget.deleteMultipleByTaskOfferId(responseModel.getOffers().get(i).task_offer_id);
                                            daoTarget.insertAll(responseModel.getOffers().get(i).data);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        if (!call.isCanceled()) {
                            // CommonUtils.Notify(activity, activity.getString(R.string.app_name), Constants.msg_Service_Error, false);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
