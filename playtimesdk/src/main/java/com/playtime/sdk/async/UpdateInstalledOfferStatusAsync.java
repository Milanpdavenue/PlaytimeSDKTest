package com.playtime.sdk.async;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.playtime.sdk.AppTrackingSetup;
import com.playtime.sdk.BuildConfig;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.repositories.PartnerAppsRepository;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateInstalledOfferStatusAsync {
    private Context activity;
    private JSONObject jObject;
    private Encryption cipher;
    private PartnerApps objApp;

    public UpdateInstalledOfferStatusAsync(final Context activity, String packageId, String userId, String appId, String gaid, PartnerApps objApp) {
        this.activity = activity;
        cipher = new Encryption();
        this.objApp = objApp;
        try {
            jObject = new JSONObject();
            jObject.put("GHJKAO", packageId);
            jObject.put("LNKUIO", appId);
            jObject.put("KVHFYI", userId);
            jObject.put("NKOWEG", gaid);
            jObject.put("BGHNH56", Build.MODEL);
            jObject.put("JKL54G", Build.VERSION.RELEASE);
            jObject.put("CVB23E", BuildConfig.VERSION_NAME);
            jObject.put("23ERF3", Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Log.e("Offer Installed ORIGINAL ==>", jObject.toString());
            Log.e("Offer Installed ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
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
                Log.e("INSTALL DATA UPDATED ==>", responseModel.toString());
                objApp.is_installed = 1;
                objApp.install_time = responseModel.getCurrentTime();
                objApp.last_completion_time = responseModel.getCurrentTime();
                new PartnerAppsRepository(activity).updatePartnerApp(objApp);
                if (objApp.offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME) || objApp.offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                    AppTrackingSetup.startAppTracking(activity);
                }
            } else {
                Log.e("INSTALL DATA NOT UPDATED ==>", responseModel.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
