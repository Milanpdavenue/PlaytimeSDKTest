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

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateFirstOpenOfferStatusAsync {
    private Context activity;
    private JSONObject jObject;
    private Encryption cipher;

    public UpdateFirstOpenOfferStatusAsync(final Context activity, String packageId, String udid, String appId, String gaid,String userId, int offerId) {
        this.activity = activity;
        cipher = new Encryption();
        try {
            jObject = new JSONObject();
            jObject.put("GBHNJF", packageId);
            jObject.put("ASDFWQ", appId);
            jObject.put("ASDERF", udid);
            jObject.put("NJMKLI", gaid);
            jObject.put("THYJUJ", userId);
            jObject.put("QWERTFS", offerId);
            jObject.put("BGHNH56", Build.MODEL);
            jObject.put("OLPUIT", Build.BRAND);
            jObject.put("BFEGRTS", Build.MANUFACTURER);
            jObject.put("1234DF", Build.DEVICE);
            jObject.put("NHFBDS", Build.VERSION.RELEASE);
            jObject.put("VCSDAA", BuildConfig.VERSION_NAME);
            jObject.put("LOPUTF", Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
//            Logger.getInstance().e("Offer First Open ORIGINAL ==>", jObject.toString());
//            Logger.getInstance().e("Offer First Open ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            Call<ApiResponse> call = apiService.UpdateFirstOpenOfferStatusAsync(userId, String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
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
//            ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(response.getEncrypt())), ResponseModel.class);
//            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
//                Logger.getInstance().e("FIRST OPEN DATA UPDATED ==>", responseModel.toString());
//            } else {
//                Logger.getInstance().e("FIRST OPEN NOT UPDATED ==>", responseModel.toString());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
