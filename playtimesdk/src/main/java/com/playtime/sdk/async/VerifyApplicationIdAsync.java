package com.playtime.sdk.async;

import android.app.Activity;
import android.os.Build;
import android.provider.Settings;

import com.google.gson.Gson;
import com.playtime.sdk.R;
import com.playtime.sdk.models.ApiResponse;
import com.playtime.sdk.models.ResponseModel;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Encryption;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyApplicationIdAsync {
    private Activity activity;
    private JSONObject jObject;
    private Encryption cipher;

    public VerifyApplicationIdAsync(final Activity activity) {
        this.activity = activity;
        cipher = new Encryption();
        try {
            CommonUtils.showProgressLoader(activity);
            jObject = new JSONObject();
//            jObject.put("NK7THO", M_Win_SharedPrefs.getInstance().getString(M_Win_SharedPrefs.userId));
//            jObject.put("WEWERE", M_Win_SharedPrefs.getInstance().getString(M_Win_SharedPrefs.userToken));
//            jObject.put("JA0C6W", M_Win_SharedPrefs.getInstance().getString(M_Win_SharedPrefs.AdID));
//            jObject.put("HW23CH", Build.MODEL);
//            jObject.put("ASDADA", Build.VERSION.RELEASE);
//            jObject.put("OYWKW7", M_Win_SharedPrefs.getInstance().getString(M_Win_SharedPrefs.AppVersion));
//            jObject.put("3H2GK7", M_Win_SharedPrefs.getInstance().getInt(M_Win_SharedPrefs.totalOpen));
//            jObject.put("J0HAPQ", M_Win_SharedPrefs.getInstance().getInt(M_Win_SharedPrefs.todayOpen));
//            jObject.put("H3QPG4", Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));

            int n = CommonUtils.getRandomNumberBetweenRange(1, 1000000);
            jObject.put("RANDOM", n);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            //AppLogger.getInstance().e("Get Invite ORIGINAL ==>", jObject.toString());
//            //AppLogger.getInstance().e("Get Invite ENCRYPTED ==>", new AESCipher().encrypt( jObject.toString()));
//            Call<ApiResponse> call = apiService.verifyAppId(M_Win_SharedPrefs.getInstance().getString(M_Win_SharedPrefs.userToken), String.valueOf(n), jObject.toString());
//            call.enqueue(new Callback<ApiResponse>() {
//                @Override
//                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                    onPostExecute(response.body());
//                }
//
//                @Override
//                public void onFailure(Call<ApiResponse> call, Throwable t) {
//                    CommonUtils.dismissProgressLoader();
//                    if (!call.isCanceled()) {
//                        CommonUtils.Notify(activity, activity.getString(R.string.app_name), Constants.msg_Service_Error, false);
//                    }
//                }
//            });
        } catch (Exception e) {
            CommonUtils.dismissProgressLoader();
            e.printStackTrace();
        }
    }

    private void onPostExecute(ApiResponse response) {
        try {
            CommonUtils.dismissProgressLoader();
            ResponseModel responseModel = new Gson().fromJson(new String(cipher.decrypt(response.getEncrypt())), ResponseModel.class);
//            if (responseModel.getStatus().equals(Constants.STATUS_SUCCESS)) {
//                if (activity instanceof M_Win_InviteAndEarnActivity) {
//                    ((M_Win_InviteAndEarnActivity) activity).setData(responseModel);
//                }
//            } else if (responseModel.getStatus().equals(Constants.STATUS_ERROR)) {
//                CommonUtils.Notify(activity, activity.getString(R.string.app_name), responseModel.getMessage(), false);
//            } else if (responseModel.getStatus().equals("2")) { // not login
//                if (activity instanceof M_Win_InviteAndEarnActivity) {
//                    ((M_Win_InviteAndEarnActivity) activity).setData(responseModel);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
