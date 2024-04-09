package com.playtime.sdk.listeners;

import androidx.annotation.Keep;

@Keep
public interface OfferWallInitListener {
    void onInitSuccess();
    void onAlreadyInitializing();
    void onInitFailed(String error);
}