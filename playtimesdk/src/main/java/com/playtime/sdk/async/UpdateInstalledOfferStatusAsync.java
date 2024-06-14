package com.playtime.sdk.async;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;

import com.google.gson.Gson;
import com.playtime.sdk.AppTrackingSetup;
import com.playtime.sdk.BuildConfig;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.repositories.PartnerAppsRepository;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;
import com.playtime.sdk.utils.Logger;
import com.playtime.sdk.utils.SharePrefs;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateInstalledOfferStatusAsync {
    private Context activity;
    private JSONObject jObject;
    private Encryption cipher;
    private PartnerApps objApp;
    private CountDownTimer timer;
    private String packageId;

    public UpdateInstalledOfferStatusAsync(final Context activity, String packageId, String udid, String appId, String gaid, PartnerApps objApp, String userId, int offerId) {
        this.activity = activity;
        cipher = new Encryption();
        this.objApp = objApp;
        this.packageId = packageId;
        try {
            jObject = new JSONObject();
            jObject.put("GHJKAO", packageId);
            jObject.put("LNKUIO", appId);
            jObject.put("KVHFYI", udid);
            jObject.put("NKOWEG", gaid);
            jObject.put("WSEDRG", userId);
            jObject.put("EDRFTGP", offerId);
            jObject.put("BGHNH56", Build.MODEL);
            jObject.put("GGHNH56", Build.BRAND);
            jObject.put("BGNNH56", Build.MANUFACTURER);
            jObject.put("BGHNH99", Build.DEVICE);
            jObject.put("JKL54G", Build.VERSION.RELEASE);
            jObject.put("CVB23E", BuildConfig.VERSION_NAME);
            jObject.put("23ERF3", Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
//            Logger.getInstance().e("Offer Installed ORIGINAL ==>", jObject.toString());
//            Logger.getInstance().e("Offer Installed ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            Call<ApiResponse> call = apiService.UpdateInstalledOfferStatusAsync(userId, String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    onPostExecute(response.body());
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
    }

    private void onPostExecute(ApiResponse response) {
        try {
            ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(response.getEncrypt())), ResponseModel.class);
            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
//                Logger.getInstance().e("INSTALL DATA UPDATED ==>", responseModel.toString());
                objApp.is_installed = 1;
                objApp.install_time = responseModel.getCurrentTime();
                objApp.last_completion_time = responseModel.getCurrentTime();
                new PartnerAppsRepository(activity).updatePartnerApp(objApp);
                if (objApp.offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME) || objApp.offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                    AppTrackingSetup.startAppTracking(activity);
                    PlaytimeSDK.getInstance().setTimer();
                }
                long startTime = CommonUtils.formatDate(responseModel.getCurrentTime()).getTime();
                long endTime = CommonUtils.formatDate(responseModel.getCurrentTime()).getTime() + 60000;
                timer = new CountDownTimer((60 * 1000L), (2 * 1000L)) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        try {
//                            Logger.getInstance().e("FIRST OPEN TIMER ==>", "FIRST OPEN TIMER ==> ON TICK");
                            UsageStatsManager mUsageStatsManager = (UsageStatsManager) activity.getSystemService(Context.USAGE_STATS_SERVICE);
                            final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

                            if (!stats.isEmpty()) {
                                final int statCount = stats.size();
                                for (int j = 0; j < statCount; j++) {
                                    final UsageStats pkgStats = stats.get(j);
                                    if (pkgStats.getPackageName().equals(packageId) && pkgStats.getTotalTimeInForeground() > 0) {
                                        timer.cancel();
                                        timer = null;
                                        new UpdateFirstOpenOfferStatusAsync(activity, packageId, SharePrefs.getInstance(activity).getString(SharePrefs.UDID),
                                                SharePrefs.getInstance(activity).getString(SharePrefs.APP_ID),
                                                SharePrefs.getInstance(activity).getString(SharePrefs.GAID),
                                                SharePrefs.getInstance(activity).getString(SharePrefs.USER_ID), objApp.task_offer_id);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                }.start();
            } else {
//                Logger.getInstance().e("INSTALL DATA NOT UPDATED ==>", responseModel.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
