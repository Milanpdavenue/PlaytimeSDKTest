package com.playtime.sdk.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PartnerAppTargets.class, PartnerApps.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDB;

    public static AppDatabase getInstance(Context c) {
        try {
            if (appDB == null)
                appDB = Room.databaseBuilder(c, AppDatabase.class, "PartnerAppsDB")
                        .fallbackToDestructiveMigration().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appDB;
    }

    public abstract PartnerAppTargetsDao partnerAppTargetsDao();

    public abstract PartnerAppsDao partnerAppsDao();
}
