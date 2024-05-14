package com.playtime.sdk.async;

import com.google.gson.JsonObject;
import com.playtime.sdk.network.ApiClient;
import com.playtime.sdk.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClickOfferAsync {

    public ClickOfferAsync(String link) {
        try {
            if (link != null && link.length() > 0) {
                Call<JsonObject> getOffers = ApiClient.getClient().create(ApiInterface.class).callAppClickApi(link);
                getOffers.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPostExecute(String response) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
