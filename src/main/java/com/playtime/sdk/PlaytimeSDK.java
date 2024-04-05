package com.playtime.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.playtime.sdk.activity.PlaytimeOfferWallActivity;
import com.playtime.sdk.listeners.OfferWallInitListener;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaytimeSDK {
    private final String baseUrl = "https://appcampaign.in/playtime_sdk/web_view/index.php";
    private String defaultUrl;
    private Boolean isInitialized = false;
    private String appId;
    private String userId, uuId;
    private String gaIdStr;
    private String fcmToken;
    private Context context;
    private OfferWallInitListener listener;
    private static PlaytimeSDK instance;
    public static BroadcastReceiver packageInstallBroadcast;
    public PlaytimeSDK() {
    }

    public static PlaytimeSDK getInstance() {
        if (instance == null) {
            synchronized (PlaytimeSDK.class) {
                if (instance == null) {
                    instance = new PlaytimeSDK();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        instance = null;
    }

    public void init(Context context, String appIdStr, String userIdStr, OfferWallInitListener listener) {
        if (listener != null && this.context != null) {
            listener.onAlreadyInitializing();
            return;
        }
        if (listener != null && (appIdStr == null || appIdStr.trim().length() == 0)) {
            listener.onInitFailed("Set proper application id");
            return;
        }
        if (listener != null && (userIdStr == null || userIdStr.trim().length() == 0)) {
            listener.onInitFailed("Set proper user id");
            return;
        }
        this.context = context;
        this.appId = appIdStr;
        this.userId = userIdStr;
        this.listener = listener;
        (new GetAdvertisingIdTask(context)).execute(new Void[0]);
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Context getContext() {
        return context;
    }

    public void open(Context context) {
        if (CommonUtils.isNetworkAvailable(context)) {
            if (this.isInitialized) {
                Intent intent = new Intent(context, PlaytimeOfferWallActivity.class);
                intent.putExtra("url", this.defaultUrl);
                intent.putExtra("appId", this.appId);
                intent.putExtra("userId", this.uuId);
                intent.putExtra("gaId", this.gaIdStr);
                context.startActivity(intent);
            }
        } else {
            CommonUtils.setToast(context, "No internet connection");
        }
    }

    private String buildUrl(String gaid, String appId, String uuId) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");
        try {
            if (gaid != null) {
                urlBuilder.append("gaid=").append(URLEncoder.encode(gaid, "UTF-8")).append("&");
            }
            if (fcmToken != null && !fcmToken.isEmpty()) {
                urlBuilder.append("fcmToken=").append(URLEncoder.encode(fcmToken, "UTF-8")).append("&");
            }
            if (appId != null && !appId.isEmpty()) {
                urlBuilder.append("appId=").append(URLEncoder.encode(appId, "UTF-8")).append("&");
            }
            if (uuId != null && !uuId.isEmpty()) {
                urlBuilder.append("userId=").append(URLEncoder.encode(uuId, "UTF-8")).append("&");
            }
            urlBuilder.append("deviceId=").append(URLEncoder.encode(Settings.Secure.getString(context.getContentResolver(), "android_id"), "UTF-8")).append("&");
            urlBuilder.append("deviceName=").append(URLEncoder.encode(Build.MODEL, "UTF-8")).append("&");
            try {
                urlBuilder.append("versionName=").append(URLEncoder.encode(BuildConfig.VERSION_NAME, "UTF-8")).append("&");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
                urlBuilder.setLength(urlBuilder.length() - 1);
            }

            this.appId = appId;
            this.uuId = uuId;
            this.gaIdStr = gaid;
            return urlBuilder.toString();
        } catch (UnsupportedEncodingException var7) {
            var7.printStackTrace();
            return baseUrl;
        }
    }

    private class GetAdvertisingIdTask extends AsyncTask<Void, Void, String> {
        private final Context context;

        GetAdvertisingIdTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(Void... voids) {
            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                fcmToken = task.getResult();
                            }
                        });
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this.context);
                gaIdStr = adInfo.getId();
            } catch (GooglePlayServicesRepairableException | IOException |
                     GooglePlayServicesNotAvailableException var4) {
                var4.printStackTrace();
            }
            return gaIdStr;
        }

        protected void onPostExecute(String url) {
            validateAppId();
        }
    }

    private void validateAppId() {
        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Encryption cipher = new Encryption();
            JSONObject jObject = new JSONObject();
            jObject.put("DE4E86", String.valueOf(context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).packageName));
            jObject.put("XDV465", appId);
            jObject.put("SDV465", userId);
            jObject.put("DF456DF", gaIdStr);
            jObject.put("QW23GB", fcmToken);
            jObject.put("GB45TGG", Build.MODEL);
            jObject.put("BG6GH56", Build.VERSION.RELEASE);
            jObject.put("DF3DFG", BuildConfig.VERSION_NAME);
            jObject.put("DFG899", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            Log.e("verifyAppId ORIGINAL ==>", jObject.toString());
            Log.e("verifyAppId ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            Call<ApiResponse> call = apiService.verifyAppId(userId, String.valueOf(n), cipher.bytesToHex(cipher.encrypt(jObject.toString())));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    onPostExecute(response.body());
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    CommonUtils.dismissProgressLoader();
                    if (!call.isCanceled()) {
                        if (listener != null) {
                            listener.onInitFailed(t.getMessage());
                        }
                        //CommonUtils.Notify(context, context.getString(R.string.app_name), Constants.msg_Service_Error, false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPostExecute(ApiResponse apiResponse) {
        try {
            Encryption cipher = new Encryption();
            ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(apiResponse.getEncrypt())), ResponseModel.class);
            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                defaultUrl = buildUrl(gaIdStr, appId, responseModel.getUuid());
                isInitialized = true;
                if (listener != null) {
                    listener.onInitSuccess();
                }
            } else {
                if (listener != null) {
                    listener.onInitFailed(responseModel.getMessage());
                }
//                CommonUtils.Notify(context, context.getString(R.string.app_name), responseModel.getMessage(), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
