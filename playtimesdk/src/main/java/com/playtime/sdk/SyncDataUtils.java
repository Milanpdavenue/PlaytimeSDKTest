package com.playtime.sdk;

import android.app.usage.UsageEvents;
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
import java.util.SortedMap;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDataUtils {
    public void syncData(Context context) {
        SharePrefs.getInstance(context).putLong(SharePrefs.LAST_SYNC_TIME, Calendar.getInstance().getTimeInMillis());
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
//            Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "getOnGoingApps ORIGINAL ==>" + jObject.toString());
//            Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "getOnGoingApps ENCRYPTED ==>" + cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            Call<ApiResponse> call = apiService.getOnGoingApps(SharePrefs.getInstance(context).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // return the task status
                    HandlerThread handlerThread = new HandlerThread("MyDataT" + SharePrefs.getInstance(context).getString(SharePrefs.APP_ID));
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
                                        objPartnerApp.completed_duration = array.getJSONObject(i).getInt("completed_duration");
                                        list.add(objPartnerApp);
                                    }
                                    responseModel.setOffers(list);
                                }
                                PartnerAppsDao dao = AppDatabase.getInstance(context).partnerAppsDao();
                                ArrayList<PartnerApps> listLocalApps = (ArrayList<PartnerApps>) dao.getAllPlaytimeOffers();
//                                Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "LOCAL OFFERS ==>SIZE: " + listLocalApps.size() + " DATA: " + listLocalApps.toString());
                                if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
//                                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "getOnGoingApps RESPONSE SUCCESS OFFERS ==>SIZE: " + responseModel.getOffers().size() + " DATA: " + responseModel.toString());
                                    if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                        // If user uninstall and reinstall app, sync all data from server
                                        if (listLocalApps.isEmpty()) {
                                            // Insert all
                                            dao.insertAll(responseModel.getOffers());
//                                            Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "NO LOCAL OFFERS Found ==> INSERT ALL from api");
                                        } else { // Sync offer data & target data with local DB
                                            try {
                                                dao.deleteOnlyInstalledOffers();
                                                dao.insertAll(responseModel.getOffers());
//                                                Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "SYNC OFFERS ==> SYNC OFFERS WITH LOCAL DB " + responseModel.getOffers());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        try {
                                            if (listLocalApps.size() > 0) {
                                                dao.deleteOnlyInstalledOffers();
//                                                Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "SYNC OFFERS ==> DELETE OFFER FROM LOCAL DB " + responseModel.getOffers());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    syncData(context, responseModel.getOffers(), CommonUtils.formatDate(responseModel.getCurrentTime()).getTime(), cipher, dao, Integer.parseInt(responseModel.getMinDayUsage()), Integer.parseInt(responseModel.getMinPlaytimeUsage()));
                                } else {
//                                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "getOnGoingApps ERROR ==>" + responseModel.toString());
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
//            Logger.getInstance().e("=============================", "=================GET USAGE====================" + listOnGoingApps.toString());
            boolean isUpdateUsageData = false;
            if (listOnGoingApps != null && !listOnGoingApps.isEmpty()) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                for (int i = 0; i < listOnGoingApps.size(); i++) {
                    try {
//                        Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "IS PACKAGE INSTALLED --> " + CommonUtils.isPackageInstalled(context, listOnGoingApps.get(i).package_id));
                        if (listOnGoingApps.get(i).is_completed == 0 && !CommonUtils.isStringNullOrEmpty(listOnGoingApps.get(i).last_completion_time) && CommonUtils.isPackageInstalled(context, listOnGoingApps.get(i).package_id)) {
                            if (listOnGoingApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME)) {
                                long lastTime = CommonUtils.formatDate(listOnGoingApps.get(i).install_time).getTime();
//                                Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "GET PLAYTIME USAGE FROM: " + CommonUtils.getStringDateTime(lastTime) + " TO: " + CommonUtils.getStringDateTime(currentTime));
//================================================ AGGREGATED USAGE ====================================================
//                                Map<String, UsageStats> aggregatedStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(lastTime, currentTime);
//
//                                // Get stats for particular package as follows:
//                                UsageStats usageStats = aggregatedStatsMap.get(listOnGoingApps.get(i).package_id);
//                                if (usageStats != null) {
//                                    long totalTimeInForegroundAfterDeduction = usageStats.getTotalTimeInForeground() - ((listOnGoingApps.get(i).completed_duration * 1000) * 60);
//                                    long duration = (totalTimeInForegroundAfterDeduction / 1000) / 60;
//                                    Logger.getInstance().e("APP USAGE: " + usageStats.getPackageName(), "AGGRA: # Total Time: " + usageStats.getTotalTimeInForeground() + " # Total Minutes: " + ((usageStats.getTotalTimeInForeground() / 1000) / 60) + " # Used Time: " + (usageStats.getTotalTimeInForeground() - ((listOnGoingApps.get(i).completed_duration * 1000) * 60)) + " # Used Minutes: " + duration);
//                                    if (duration >= minPlaytimeUsage) {
//                                        listOnGoingApps.get(i).usage_duration = duration;
//                                        isUpdateUsageData = true;
//                                    }
//                                    Logger.getInstance().e("GET USAGE PLAYTIME==>", ""+listOnGoingApps.get(i).usage_duration);
//                                }
//================================================ EVENT USAGE ====================================================
//                                UsageEvents usageEvents = mUsageStatsManager.queryEvents(lastTime, currentTime);
//                                UsageEvents.Event event = new UsageEvents.Event();
//                                long totalTimeInForegroundEvent = 0;
//                                String currentPackage = null;
//                                long startTime = 0;
//                                while (usageEvents.hasNextEvent()) {
//                                    usageEvents.getNextEvent(event);
//                                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getPackageName().equals(listOnGoingApps.get(i).package_id)) {
//                                        currentPackage = event.getPackageName();
//                                        startTime = event.getTimeStamp();
//                                    } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && event.getPackageName().equals(listOnGoingApps.get(i).package_id) && event.getPackageName().equals(currentPackage)) {
//                                        if (startTime >= lastTime && event.getTimeStamp() <= currentTime) {
//                                            totalTimeInForegroundEvent += event.getTimeStamp() - startTime;
//                                            currentPackage = null;
//                                            startTime = 0;
////                                        Log.e("UsageStats~~~~~~~~~~~~~~", "EVENT == Usage: " + totalTimeInForeground + " == Minutes: " + ((totalTimeInForeground / 1000) / 60) + " == ON : " + CommonUtils.getStringDateTime(event.getTimeStamp()));
//                                        }
//                                    }
//                                }
//                                if (totalTimeInForegroundEvent > 0) {
//                                    long totalTimeInForegroundAfterDeduction = totalTimeInForegroundEvent - ((listOnGoingApps.get(i).completed_duration * 1000) * 60);
//                                    long duration = (totalTimeInForegroundAfterDeduction / 1000) / 60;
//                                    Logger.getInstance().e("APP USAGE: " + listOnGoingApps.get(i).package_id, "EVENT: # Total Time: " + totalTimeInForegroundEvent + " # Total Minutes: " + ((totalTimeInForegroundEvent / 1000) / 60) + " # Used Time: " + totalTimeInForegroundAfterDeduction + " # Used Minutes: " + duration);
//                                    if (duration >= minPlaytimeUsage) {
//                                        listOnGoingApps.get(i).usage_duration = duration;
//                                        isUpdateUsageData = true;
//                                    }
//                                }
//================================================ USAGE STAT ====================================================
                                UsageEvents usageEvents = mUsageStatsManager.queryEvents(lastTime, currentTime);
                                UsageEvents.Event event = new UsageEvents.Event();
                                SortedMap<Long, Long> sortedMapUsageEvents = new TreeMap<>();
                                long totalTimeInForeground = 0;
                                String currentPackage = null;
                                long startTime = 0;
                                while (usageEvents.hasNextEvent()) {
                                    usageEvents.getNextEvent(event);
                                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getPackageName().equals(listOnGoingApps.get(i).package_id)) {
                                        currentPackage = event.getPackageName();
                                        startTime = event.getTimeStamp();
                                    } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && event.getPackageName().equals(listOnGoingApps.get(i).package_id) && event.getPackageName().equals(currentPackage)) {
                                        if (startTime >= lastTime && event.getTimeStamp() <= currentTime) {
                                            totalTimeInForeground += event.getTimeStamp() - startTime;
                                            sortedMapUsageEvents.put(event.getTimeStamp(), totalTimeInForeground);
                                            currentPackage = null;
                                            startTime = 0;
//                                            Logger.getInstance().e("UsageStats~~~~~~~~~~~~~~", "ADD EVENT Usage: " + totalTimeInForeground + " # Minutes: " + ((totalTimeInForeground / 1000) / 60) + " # USED ON: " + CommonUtils.getStringDateTime(event.getTimeStamp()));
                                        }
                                    }
                                }

                                final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, lastTime, currentTime);

                                if (!stats.isEmpty()) {
                                    final int statCount = stats.size();
                                    for (int j = 0; j < statCount; j++) {
                                        final UsageStats pkgStats = stats.get(j);
                                        if (pkgStats.getPackageName().equals(listOnGoingApps.get(i).package_id)) {
                                            if (!sortedMapUsageEvents.isEmpty()) {
                                                if (pkgStats.getLastTimeUsed() > sortedMapUsageEvents.lastKey()) {
                                                    totalTimeInForeground += (pkgStats.getTotalTimeInForeground() - sortedMapUsageEvents.get(sortedMapUsageEvents.lastKey()));
//                                                    Logger.getInstance().e("UsageStats~~~~~~~~~~~~~~", "ADD STATE Usage: " + (pkgStats.getTotalTimeInForeground() - sortedMapUsageEvents.get(sortedMapUsageEvents.lastKey())) + " # Minutes: " + (((pkgStats.getTotalTimeInForeground() - sortedMapUsageEvents.get(sortedMapUsageEvents.lastKey())) / 1000) / 60) + " # USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
                                                }
                                            } else {
                                                totalTimeInForeground += pkgStats.getTotalTimeInForeground();
//                                                Logger.getInstance().e("UsageStats~~~~~~~~~~~~~~", "EMPTY MAP ADD STATE Usage: " + pkgStats.getTotalTimeInForeground() + " # Minutes: " + ((pkgStats.getTotalTimeInForeground() / 1000) / 60) + " # USED ON: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()));
                                            }
                                        }
                                    }
                                }
                                if (totalTimeInForeground > 0) {
                                    long totalTimeInForegroundAfterDeduction = totalTimeInForeground - ((listOnGoingApps.get(i).completed_duration * 1000) * 60);
                                    long duration = (totalTimeInForegroundAfterDeduction / 1000) / 60;
//                                    Logger.getInstance().e("APP USAGE: " + listOnGoingApps.get(i).package_id, "# FINAL Usage: # Total Time: " + totalTimeInForeground + " # Total Minutes: " + ((totalTimeInForeground / 1000) / 60) + " # Used Time: " + totalTimeInForegroundAfterDeduction + " # Used Minutes: " + duration);
                                    if (duration >= minPlaytimeUsage) {
                                        listOnGoingApps.get(i).usage_duration = duration;
                                        isUpdateUsageData = true;
                                    }
                                }
                            } else if (listOnGoingApps.get(i).offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                                Calendar endCal = Calendar.getInstance();
                                long lastTime = CommonUtils.formatDate(listOnGoingApps.get(i).last_completion_time).getTime();
                                endCal.setTimeInMillis(lastTime);
//                                Logger.getInstance().e("GET DAY USAGE1", "GET DAY USAGE FROM: " + CommonUtils.getStringDateTime(lastTime) + " TO: "
//                                        + CommonUtils.getStringDateTime(currentTime) + " IS ANY TARGET COMPLETE: " + listOnGoingApps.get(i).is_any_target_completed);

                                if (!CommonUtils.getStringDate(lastTime).equals(CommonUtils.getStringDate(currentTime)) || listOnGoingApps.get(i).is_any_target_completed == 0) {
                                    if (listOnGoingApps.get(i).is_any_target_completed == 1) {
                                        endCal.add(Calendar.DAY_OF_MONTH, 1);
                                        endCal.set(Calendar.HOUR, 0);
                                        endCal.set(Calendar.HOUR_OF_DAY, 0);
                                        endCal.set(Calendar.MINUTE, 0);
                                        endCal.set(Calendar.SECOND, 0);
                                        endCal.set(Calendar.MILLISECOND, 0);
//                                        Logger.getInstance().e("GET DAY USAGE2", "GET DAY USAGE FROM: " + CommonUtils.getStringDateTime(endCal.getTimeInMillis()) + " TO: "
//                                                + CommonUtils.getStringDateTime(currentTime));

                                    }
                                    final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, endCal.getTimeInMillis(), currentTime);
                                    int dayCount = 0;
                                    if (stats != null) {
                                        for (int t = 0; t < stats.size(); t++) {
                                            final UsageStats pkgStats = stats.get(t);
//                                            Logger.getInstance().e("GET DAY USAGE pkgStats.getPackageName()", "=== " + pkgStats.getPackageName() + " USAGE: " + pkgStats.getTotalTimeInForeground());
                                            if (pkgStats.getPackageName().equals(listOnGoingApps.get(i).package_id)) {
//                                                Logger.getInstance().e("GET USAGE DAY==>", "TotalTimeInForeground: " + pkgStats.getTotalTimeInForeground() + " === minDayUsage: " + minDayUsage + " === LastTimeUsed-usage: " + CommonUtils.getStringDateTime(pkgStats.getLastTimeUsed()) + " ===  endCal.getTimeInMillis(): " + CommonUtils.getStringDateTime(endCal.getTimeInMillis()) + " === getLastTimeUsed() > endCal.getTimeInMillis(): " + (pkgStats.getLastTimeUsed() > endCal.getTimeInMillis()));
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
//                                Logger.getInstance().e("GET USAGE DAY==>", "OFFER ID: " + listOnGoingApps.get(i).task_offer_id + " === usage_duration: " + listOnGoingApps.get(i).usage_duration);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//                Logger.getInstance().e("=============================", "=================SYNC USAGE isUpdateUsageData ====================" + isUpdateUsageData);
                if (isUpdateUsageData) {
//                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "NOW SYNC USAGE STATUS ==> " + listOnGoingApps.toString());
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
//                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "updatePlaytime ORIGINAL ==>" + jObject.toString());
//                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "updatePlaytime ENCRYPTED ==>" + cipher.bytesToHex(cipher.encrypt(jObject.toString())));
                    Call<ApiResponse> call = apiService.updatePlaytime(SharePrefs.getInstance(context).getString(SharePrefs.USER_ID), String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            HandlerThread handlerThread = new HandlerThread("MyDataT1" + SharePrefs.getInstance(context).getString(SharePrefs.APP_ID));
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
//                                        Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "updatePlaytime RESPONSE: " + responseModel.toString());
                                        if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                                            try {
                                                dao.deleteOnlyInstalledOffers();
                                                if (responseModel.getOffers() != null && !responseModel.getOffers().isEmpty()) {
                                                    dao.insertAll(responseModel.getOffers());
//                                                    Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "SYNC UPDATED OFFERS WITH LOCAL DB: " + responseModel.getOffers());
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
//                Logger.getInstance().e("SyncDataUtils~~~~~~~~~~~~~~", "ALL OFFERS COMPLETE STOP WORK MANAGER -->");
                AppTrackingSetup.stopTracking(context);
                PlaytimeSDK.getInstance().stopTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
