package com.playtime.sdk.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "PartnerAppTargets")
public class PartnerAppTargets implements Serializable {

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "task_offer_id")
    public String task_offer_id;

    @ColumnInfo(name = "conversion_id")
    public String conversion_id;

    @ColumnInfo(name = "completion_time")
    public String completion_time;

    @ColumnInfo(name = "target_time")
    public int target_time;

    @ColumnInfo(name = "target_payout")
    public String target_payout;

    @ColumnInfo(name = "is_completed")
    public String is_completed;

    public PartnerAppTargets(int id, String task_offer_id, String conversion_id, String completion_time, int target_time, String target_payout, String is_completed) {
        this.id = id;
        this.task_offer_id = task_offer_id;
        this.conversion_id = conversion_id;
        this.completion_time = completion_time;
        this.target_time = target_time;
        this.target_payout = target_payout;
        this.is_completed = is_completed;
    }

    @Override
    public String toString() {
        return "PartnerAppTargets{" +
                "id=" + id +
                ", task_offer_id='" + task_offer_id + '\'' +
                ", conversion_id='" + conversion_id + '\'' +
                ", completion_time='" + completion_time + '\'' +
                ", target_time=" + target_time +
                ", target_payout='" + target_payout + '\'' +
                ", is_completed='" + is_completed + '\'' +
                '}';
    }
}
