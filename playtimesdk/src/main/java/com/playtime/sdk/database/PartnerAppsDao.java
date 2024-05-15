package com.playtime.sdk.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.playtime.sdk.utils.Constants;

import java.util.List;

@Dao
public interface PartnerAppsDao {
    @Query("SELECT * FROM PartnerApps WHERE is_completed = 0 AND offer_type_id IN(" + Constants.OFFER_TYPE_DAY + "," + Constants.OFFER_TYPE_PLAYTIME + ")")
    List<PartnerApps> getAllPlaytimeOffers();

    @Query("SELECT * FROM PartnerApps WHERE task_offer_id = :itemId")
    PartnerApps getPartnerAppsById(int itemId);

    @Query("SELECT count(task_offer_id) FROM PartnerApps WHERE task_offer_id = :itemId")
    int isPartnerAppExist(int itemId);

    @Query("SELECT * FROM PartnerApps WHERE package_id = :itemId ORDER BY click_time DESC")
    PartnerApps getPartnerAppByPackageId(String itemId);

    @Query("SELECT package_id FROM PartnerApps")
    List<String> getOnlyPackageIds();

    @Insert(onConflict = REPLACE)
    void insertAll(List<PartnerApps> partnerAppsList);

    @Insert(onConflict = REPLACE)
    long insert(PartnerApps partnerApps);

    @Delete
    void delete(PartnerApps partnerApps);

    @Update
    int update(PartnerApps partnerApps);

    //Delete one item by id
    @Query("DELETE FROM PartnerApps WHERE task_offer_id = :itemId")
    void deleteById(int itemId);

    @Query("DELETE FROM PartnerApps WHERE is_installed = 1")
    void deleteOnlyInstalledOffers();

    //Delete multiple item NOT IN api data
    @Query("DELETE FROM PartnerApps WHERE task_offer_id NOT IN (:itemId)")
    void deleteMultipleByIds(int[] itemId);

    //Delete All
    @Query("DELETE FROM PartnerApps")
    void deleteAll();
}
