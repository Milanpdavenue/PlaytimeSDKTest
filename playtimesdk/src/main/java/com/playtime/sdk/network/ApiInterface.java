package com.playtime.sdk.network;
import com.google.gson.JsonObject;
import com.playtime.sdk.models.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("DIGBEIW")
    Call<ApiResponse> verifyAppId(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);
    @FormUrlEncoded
    @POST("SDBGJOU")
    Call<ApiResponse> UpdateInstalledOfferStatusAsync(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);

    @FormUrlEncoded
    @POST("DALPAKVANYUMMY")
    Call<ApiResponse> getOnGoingApps(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);
    @FormUrlEncoded
    @POST("UPDATEPLAYTIMEUSAGEPUDI")
    Call<ApiResponse> updatePlaytime(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);

    @GET
    Call<JsonObject> callAppClickApi(@Url String Value);

    @FormUrlEncoded
    @POST("ULTAVADAPAVBHAJI")
    Call<ApiResponse> UpdateFirstOpenOfferStatusAsync(@Header("Token") String token, @Header("Secret") String random, @Field("details") String details);
}
