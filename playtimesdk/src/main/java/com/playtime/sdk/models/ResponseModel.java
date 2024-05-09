package com.playtime.sdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.playtime.sdk.database.PartnerApps;

import java.util.ArrayList;

@Keep
public class ResponseModel {
    @Expose
    private String message;
    @Expose
    private String status;
    @Expose
    private String points;
    @Expose
    private String uuid;
    @Expose
    private String earningPoint;
    @Expose
    private String currentTime;
    @Expose
    private String minDayUsage;
    @Expose
    private String minPlaytimeUsage;

    @Expose
    private ArrayList<PartnerApps> offers;

    public ArrayList<PartnerApps> getOffers() {
        return offers;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getPoints() {
        return points;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEarningPoint() {
        return earningPoint;
    }

    public String getMinDayUsage() {
        return minDayUsage;
    }

    public String getMinPlaytimeUsage() {
        return minPlaytimeUsage;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", points='" + points + '\'' +
                ", uuid='" + uuid + '\'' +
                ", earningPoint='" + earningPoint + '\'' +
                ", currentTime='" + currentTime + '\'' +
                ", minDayUsage='" + minDayUsage + '\'' +
                ", minPlaytimeUsage='" + minPlaytimeUsage + '\'' +
                ", offers=" + offers +
                '}';
    }
}
