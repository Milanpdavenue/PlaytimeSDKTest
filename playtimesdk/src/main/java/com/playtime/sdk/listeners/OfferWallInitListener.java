package com.playtime.sdk.listeners;

public interface OfferWallInitListener {
    void onInitSuccess();
    void onAlreadyInitializing();
    void onInitFailed(String error);
}