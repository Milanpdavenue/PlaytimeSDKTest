package com.playtime.sdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.playtime.sdk.activity.PlaytimeOfferWallActivity;
import com.playtime.sdk.database.AppDatabase;
import com.playtime.sdk.listeners.OfferWallInitListener;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;
import com.playtime.sdk.utils.Logger;
import com.playtime.sdk.utils.SharePrefs;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Keep
public class PlaytimeSDK {
    //    private final String baseUrl = "https://appcampaign.in/playtime_sdk/web_view/index.php";
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
    public static BroadcastReceiver deviceStatusBroadcast;
    private CountDownTimer timer;

    public PlaytimeSDK() {
    }

    static {
        System.loadLibrary("sdk");
    }

    public native String getBaseUrl();

    public native String getUrl();

    public native String getMIV();

    public native String getKey();

    public native String getPName();

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
        stopTimer();
        instance = null;
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void setTimer() {
        try {
            if (CommonUtils.isNetworkAvailable(context)) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (timer == null) {
                            timer = new CountDownTimer((30 * 60 * 1000L), (60 * 1000L)) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    try {
                                        if (CommonUtils.isNetworkAvailable(context)) {
                                            Logger.getInstance().e(" START SYNC FROM TIMER ==>", "START SYNC FROM TIMER");
                                            if (!SharePrefs.getInstance(context).getBoolean(SharePrefs.IS_SYNC_IN_PROGRESS)) {
                                                new SyncDataUtils().syncData(context);
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
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Context context, String appIdStr, String userIdStr, OfferWallInitListener listener) {
        if (listener != null && this.context != null) {
            listener.onAlreadyInitializing();
            return;
        }
        if (listener != null && !CommonUtils.isNetworkAvailable(context)) {
            listener.onInitFailed("No internet connection");
            return;
        }
        if (listener != null && (appIdStr == null || appIdStr.trim().isEmpty())) {
            listener.onInitFailed("Set proper application id");
            return;
        }
        if (listener != null && (userIdStr == null || userIdStr.trim().isEmpty())) {
            listener.onInitFailed("Set proper user id");
            return;
        }
        this.context = context;
        this.appId = appIdStr;
        this.userId = userIdStr;
        this.listener = listener;
        try {
            (new GetAdvertisingIdTask(context)).execute(new Void[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if (context instanceof Activity) {
                    ((Activity) context).overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }
        } else {
            CommonUtils.setToast(context, "No internet connection");
        }
    }

    private String buildUrl(String gaid, String appId, String uuId) {
        StringBuilder urlBuilder = new StringBuilder(getUrl());
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
            return getUrl();
        }
    }

    private class GetAdvertisingIdTask extends AsyncTask<Void, Void, String> {
        private final Context context;

        GetAdvertisingIdTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(Void... voids) {
            try {
                try {
                    if (!AppDatabase.getInstance(context).partnerAppsDao().getAllPlaytimeOffers().isEmpty()) {
                        AppTrackingSetup.startAppTracking(context);
                        setTimer();
                    } else {
                        AppTrackingSetup.stopTracking(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            } catch (Exception var4) {
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
            jObject.put("212E4D6", Build.BRAND);
            jObject.put("CDVFBG", Build.MANUFACTURER);
            jObject.put("VDHNHQW", Build.DEVICE);
            jObject.put("BG6GH56", Build.VERSION.RELEASE);
            jObject.put("DF3DFG", BuildConfig.VERSION_NAME);
            jObject.put("DFG899", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            Logger.getInstance().e("verifyAppId ORIGINAL ==>", jObject.toString());
            Logger.getInstance().e("verifyAppId ENCRYPTED ==>", cipher.bytesToHex(cipher.encrypt(jObject.toString())));
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
//            Logger.getInstance().e("verifyAppId responseModel: ", "responseModel: " + responseModel);
            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
                defaultUrl = buildUrl(gaIdStr, appId, responseModel.getUuid());
                SharePrefs.getInstance(context).putString(SharePrefs.APP_ID, appId);
                SharePrefs.getInstance(context).putString(SharePrefs.GAID, gaIdStr);
                SharePrefs.getInstance(context).putString(SharePrefs.FCM_TOKEN, fcmToken);
                SharePrefs.getInstance(context).putString(SharePrefs.UDID, responseModel.getUuid());
                SharePrefs.getInstance(context).putString(SharePrefs.USER_ID, userId);
                SharePrefs.getInstance(context).putString(SharePrefs.CONSENT_TITLE, responseModel.getConsentTitle());
                SharePrefs.getInstance(context).putString(SharePrefs.CONSENT_MESSAGE, responseModel.getConsentMessage());
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
