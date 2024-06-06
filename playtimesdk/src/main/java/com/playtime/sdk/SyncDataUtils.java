package com.playtime.sdk;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.playtime.sdk.database.AppDatabase;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.database.PartnerAppsDao;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;
import com.playtime.sdk.utils.Logger;
import com.playtime.sdk.utils.SharePrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDataUtils {
    public void syncData(Context context) {
        SharePrefs.getInstance(context).putBoolean(SharePrefs.IS_SYNC_IN_PROGRESS, true);
        Encryption cipher = new Encryption();
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("ORANGE", SharePrefs.getInstance(context).getString(SharePrefs.APP_ID));
            jObject.put("PAKODI", SharePrefs.getInstance(context).getString(SharePrefs.GAID));
            jObject.put("GTHYJU", SharePrefs.getInstance(context).getString(SharePrefs.UDID));
            jObject.put("ERVTUH", SharePrefs.getInstance(context).getString(SharePrefs.USER_ID));
            jObject.put("POINTG", SharePrefs.getInstance(context).getString(SharePrefs.FCM_TOKEN));
            jObject.put("BGHNH5", Build.MODEL);
            jObject.put("GGHN4D6", Build.BRAND);
            jObject.put("TYNNH56", Build.MANUFACTURER);
            jObject.put("VDHNH99", Build.DEVICE);
            jObject.put("CVBNHG", Build.VERSION.RELEASE);
            jObject.put("QAWSED", BuildConfig.VERSION_NAME);
            jObject.put("GHJKUF", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ApiResponse> call = apiService.getOnGoingApps(SharePrefs.getInstance(context).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
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
                                String jsonString = new String(cipher.decrypt(response.body().getEncrypt()));
                                JSONObject json = new JSONObject(jsonString);
                                ResponseModel responseModel = new Gson().fromJson(jsonString, ResponseModel.class);
                                JSONArray array = json.getJSONArray("offers");
                                if (array.length() > 0) {
                                    ArrayList<PartnerApps> list = new ArrayList<>();
                                    for (int i = 0; i < array.length(); i++) {
                                        PartnerApps objPartnerApp = new PartnerApps(
                                                array.getJSONObject(i).getInt("task_offer_id"),
                                                array.getJSONObject(i).getString("task_offer_name"),
                                                array.getJSONObject(i).getString("package_id"),
                                                array.getJSONObject(i).getInt("is_installed"),
                                                array.getJSONObject(i).getString("install_time"),
                                                array.getJSONObject(i).getInt("conversion_id"),
                                                array.getJSONObject(i).getString("last_completion_time"),
                                                array.getJSONObject(i).getString("offer_type_id"),
                                                array.getJSONObject(i).getInt("is_completed"));
                                        objPartnerApp.is_any_target_completed = array.getJSONObject(i).getInt("is_any_target_completed");
                                        list.add(objPartnerApp);
                                    }
                                    responseModel.setOffers(list);
                                }
                                PartnerAppsDao dao = AppDatabase.getInstance(context).partnerAppsDao();
                                ArrayList<PartnerApps> listLocalApps = (ArrayList<PartnerApps>) dao.getAllPlaytimeOffers();
                                if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                                    if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                        // If user uninstall and reinstall app, sync all data from server
                                        if (listLocalApps.isEmpty()) {
                                            // Insert all
                                            dao.insertAll(responseModel.getOffers());
                                        } else { // Sync offer data & target data with local DB
                                            try {
                                                dao.deleteOnlyInstalledOffers();
                                                dao.insertAll(responseModel.getOffers());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        try {
                                            if (listLocalApps.size() > 0) {
                                                dao.deleteOnlyInstalledOffers();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    syncData(context, responseModel.getOffers(), CommonUtils.formatDate(responseModel.getCurrentTime()).getTime(), cipher, dao, Integer.parseInt(responseModel.getMinDayUsage()), Integer.parseInt(responseModel.getMinPlaytimeUsage()));
                                } else {
                                    syncData(context, responseModel.getOffers(), CommonUtils.formatDate(responseModel.getCurrentTime()).getTime(), cipher, dao, Integer.parseInt(responseModel.getMinDayUsage()), Integer.parseInt(responseModel.getMinPlaytimeUsage()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            handlerThread.quit();
                        }
                    }, 0);

                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    if (!call.isCanceled()) {
                        SharePrefs.getInstance(context).putBoolean(SharePrefs.IS_SYNC_IN_PROGRESS, false);
                        // CommonUtils.Notify(activity, activity.getString(R.string.app_name), Constants.msg_Service_Error, false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncData(Context context, ArrayList<PartnerApps> listOnGoingApps, long currentTime, Encryption cipher, PartnerAppsDao dao, int minDayUsage, int minPlaytimeUsage) {
        try {
            boolean isUpdateUsageData = false;
            if (listOnGoingApps != null && !listOnGoingApps.isEmpty()) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//                long currentTime = CommonUtils.formatDate(responseModel.getCurrentTime()).getTime();
                for (int i = 0; i < listOnGoingApps.size(); i++) {
                    try {
                        if (listOnGoingApps.get(i).is_completed == 0 && !CommonUtils.isStringNullOrEmpty(listOnGoingApps.get(i).last_completion_time) && CommonUtils.isPackageInstalled(context, listOnGoingApps.get(i).package_id)) {
                            if (listOnGoingApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME)) {
                                long lastTime = CommonUtils.formatDate(listOnGoingApps.get(i).last_completion_time).getTime();
                                Map<String, UsageStats> aggregatedStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(lastTime, currentTime);

                                // Get stats for particular package as follows:
                                UsageStats usageStats = aggregatedStatsMap.get(listOnGoingApps.get(i).package_id);
                                if (usageStats != null) {
                                    long duration = (usageStats.getTotalTimeInForeground() / 1000) / 60;
                                    if (duration >= minPlaytimeUsage) {
                                        listOnGoingApps.get(i).usage_duration = duration;
                                        isUpdateUsageData = true;
                                    }
                                }
                            } else if (listOnGoingApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                                Calendar endCal = Calendar.getInstance();
                                long lastTime = CommonUtils.formatDate(listOnGoingApps.get(i).last_completion_time).getTime();
                                endCal.setTimeInMillis(lastTime);
                                if (!CommonUtils.getStringDate(lastTime).equals(CommonUtils.getStringDate(currentTime)) || listOnGoingApps.get(i).is_any_target_completed == 0) {
                                    if (listOnGoingApps.get(i).is_any_target_completed == 1) {
                                        endCal.add(Calendar.DAY_OF_MONTH, 1);
                                        endCal.set(Calendar.HOUR, 0);
                                        endCal.set(Calendar.HOUR_OF_DAY, 0);
                                        endCal.set(Calendar.MINUTE, 0);
                                        endCal.set(Calendar.SECOND, 0);
                                        endCal.set(Calendar.MILLISECOND, 0);
                                    }
                                    final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, endCal.getTimeInMillis(), currentTime);
                                    int dayCount = 0;
                                    if (stats != null) {
                                        for (int t = 0; t < stats.size(); t++) {
                                            final UsageStats pkgStats = stats.get(t);
                                            if (pkgStats.getPackageName().equals(listOnGoingApps.get(i).package_id)) {
                                                if (((pkgStats.getTotalTimeInForeground() / 1000) / 60) >= minDayUsage && pkgStats.getLastTimeUsed() > endCal.getTimeInMillis()) {
                                                    dayCount++;
                                                }
                                            }
                                        }
                                    }
                                    listOnGoingApps.get(i).usage_duration = dayCount;
                                    if (listOnGoingApps.get(i).usage_duration > 0) {
                                        isUpdateUsageData = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (isUpdateUsageData) {
                    // NOW SEND ALL DATA TO SERVER
//                    Gson gson = new Gson();
//
//                    String listString = gson.toJson(
//                            listOnGoingApps,
//                            new TypeToken<ArrayList<PartnerApps>>() {
//                            }.getType());

                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < listOnGoingApps.size(); i++) {
                        try {
                            JSONObject jsonObject1 = getJsonObject(listOnGoingApps, i);
                            jsonArray.put(jsonObject1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject jObject = new JSONObject();
                    jObject.put("PLOKTH", SharePrefs.getInstance(context).getString(SharePrefs.APP_ID));
                    jObject.put("LKM98", SharePrefs.getInstance(context).getString(SharePrefs.GAID));
                    jObject.put("EFRGTH", SharePrefs.getInstance(context).getString(SharePrefs.UDID));
                    jObject.put("BGNHG", SharePrefs.getInstance(context).getString(SharePrefs.USER_ID));
                    jObject.put("POINTG", SharePrefs.getInstance(context).getString(SharePrefs.FCM_TOKEN));
                    jObject.put("BGHNH5", Build.MODEL);
                    jObject.put("23DN4D6", Build.BRAND);
                    jObject.put("TYNWER6", Build.MANUFACTURER);
                    jObject.put("VDHNH", Build.DEVICE);
                    jObject.put("CVBNHG", Build.VERSION.RELEASE);
                    jObject.put("QAWSED", BuildConfig.VERSION_NAME);
                    jObject.put("HGJFUF", jsonArray);
                    jObject.put("GHJKUF", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

                    int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
                    jObject.put("RANDOM", n);
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                    Call<ApiResponse> call = apiService.updatePlaytime(SharePrefs.getInstance(context).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            HandlerThread handlerThread = new HandlerThread("SomeOtherThread");
                            handlerThread.start();
                            Handler handler = new Handler(handlerThread.getLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String jsonString = new String(cipher.decrypt(response.body().getEncrypt()));
                                        JSONObject json = new JSONObject(jsonString);
                                        ResponseModel responseModel = new Gson().fromJson(jsonString, ResponseModel.class);
                                        JSONArray array = json.getJSONArray("offers");
                                        if (array.length() > 0) {
                                            ArrayList<PartnerApps> list = new ArrayList<>();
                                            for (int i = 0; i < array.length(); i++) {
                                                PartnerApps objPartnerApp = new PartnerApps(
                                                        array.getJSONObject(i).getInt("task_offer_id"),
                                                        array.getJSONObject(i).getString("task_offer_name"),
                                                        array.getJSONObject(i).getString("package_id"),
                                                        array.getJSONObject(i).getInt("is_installed"),
                                                        array.getJSONObject(i).getString("install_time"),
                                                        array.getJSONObject(i).getInt("conversion_id"),
                                                        array.getJSONObject(i).getString("last_completion_time"),
                                                        array.getJSONObject(i).getString("offer_type_id"),
                                                        array.getJSONObject(i).getInt("is_completed"));
                                                list.add(objPartnerApp);
                                            }
                                            responseModel.setOffers(list);
                                        }

                                        if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                                            try {

                                                dao.deleteOnlyInstalledOffers();
                                                if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                                    dao.insertAll(responseModel.getOffers());
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    finishSyncAndStopWorkManagerIfRequired(context, dao);
                                    handlerThread.quit();
                                }
                            }, 0);
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            SharePrefs.getInstance(context).putBoolean(SharePrefs.IS_SYNC_IN_PROGRESS, false);
                            if (!call.isCanceled()) {
                                // CommonUtils.Notify(activity, activity.getString(R.string.app_name), Constants.msg_Service_Error, false);
                            }
                        }
                    });
                } else {
                    finishSyncAndStopWorkManagerIfRequired(context, dao);
                }
            } else {
                finishSyncAndStopWorkManagerIfRequired(context, dao);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static JSONObject getJsonObject(ArrayList<PartnerApps> listOnGoingApps, int i) throws JSONException {
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("task_offer_id", listOnGoingApps.get(i).task_offer_id);
        jsonObject1.put("task_offer_name", listOnGoingApps.get(i).task_offer_name);
        jsonObject1.put("package_id", listOnGoingApps.get(i).package_id);
        jsonObject1.put("is_installed", listOnGoingApps.get(i).is_installed);
        jsonObject1.put("install_time", listOnGoingApps.get(i).install_time);
        jsonObject1.put("conversion_id", listOnGoingApps.get(i).conversion_id);
        jsonObject1.put("last_completion_time", listOnGoingApps.get(i).last_completion_time);
        jsonObject1.put("offer_type_id", listOnGoingApps.get(i).offer_type_id);
        jsonObject1.put("is_completed", listOnGoingApps.get(i).is_completed);
        jsonObject1.put("usage_duration", listOnGoingApps.get(i).usage_duration);
        jsonObject1.put("is_any_target_completed", listOnGoingApps.get(i).is_any_target_completed);
        return jsonObject1;
    }

    private void finishSyncAndStopWorkManagerIfRequired(Context context, PartnerAppsDao dao) {
        try {
            SharePrefs.getInstance(context).putBoolean(SharePrefs.IS_SYNC_IN_PROGRESS, false);
            if (dao.getAllPlaytimeOffers().isEmpty()) {
                AppTrackingSetup.stopTracking(context);
                PlaytimeSDK.getInstance().stopTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
