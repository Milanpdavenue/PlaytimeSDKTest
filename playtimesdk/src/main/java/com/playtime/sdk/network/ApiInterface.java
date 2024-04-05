package com.playtime.sdk.network;
import com.playtime.sdk.models.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("DIGBEIW")
    Call<ApiResponse> verifyAppId(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);
    @FormUrlEncoded
    @POST("SDBGJOU")
    Call<ApiResponse> UpdateInstalledOfferStatusAsync(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);
}
