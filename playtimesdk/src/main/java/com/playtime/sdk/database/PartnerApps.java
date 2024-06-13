package com.playtime.sdk.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "PartnerApps")
public class PartnerApps implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "task_offer_id")
    public int task_offer_id;

    @ColumnInfo(name = "task_offer_name")
    public String task_offer_name;

    @ColumnInfo(name = "package_id")
    public String package_id;

    @ColumnInfo(name = "is_installed")
    public int is_installed;

    @ColumnInfo(name = "install_time")
    public String install_time;

    @ColumnInfo(name = "conversion_id")
    public int conversion_id;

    @ColumnInfo(name = "last_completion_time")
    public String last_completion_time;

    @ColumnInfo(name = "offer_type_id")
    public String offer_type_id;

    @ColumnInfo(name = "is_completed")
    public int is_completed;
    @ColumnInfo(name = "click_time")
    public long click_time;

    @Ignore
    public long usage_duration;
    @Ignore
    public long completed_duration;
    @Ignore
    public int is_any_target_completed;

    public PartnerApps(int task_offer_id, String task_offer_name, String package_id, int is_installed, String install_time,
                       int conversion_id, String last_completion_time, String offer_type_id, int is_completed) {
        this.task_offer_id = task_offer_id;
        this.task_offer_name = task_offer_name;
        this.package_id = package_id;
        this.is_installed = is_installed;
        this.install_time = install_time;
        this.conversion_id = conversion_id;
        this.last_completion_time = last_completion_time;
        this.offer_type_id = offer_type_id;
        this.is_completed = is_completed;
    }

    @Override
    public String toString() {
        return "PartnerApps{" +
                "task_offer_id=" + task_offer_id +
                ", task_offer_name='" + task_offer_name + '\'' +
                ", package_id='" + package_id + '\'' +
                ", is_installed=" + is_installed +
                ", install_time='" + install_time + '\'' +
                ", conversion_id=" + conversion_id +
                ", last_completion_time='" + last_completion_time + '\'' +
                ", offer_type_id='" + offer_type_id + '\'' +
                ", is_completed=" + is_completed +
                ", usage_duration=" + usage_duration +
                ", completed_duration=" + completed_duration +
                ", is_any_target_completed=" + is_any_target_completed +
                '}';
    }
}
