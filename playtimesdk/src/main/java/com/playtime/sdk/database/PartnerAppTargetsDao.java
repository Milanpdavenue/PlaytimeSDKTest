package com.playtime.sdk.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PartnerAppTargetsDao {
    @Query("SELECT * FROM PartnerAppTargets WHERE task_offer_id = :offerId")
    List<PartnerAppTargets> getAll(int offerId);

    @Insert(onConflict = REPLACE)
    void insertAll(List<PartnerAppTargets> partnerAppTargets);

    @Insert
    void insert(PartnerAppTargets partnerAppTargets);

    @Delete
    void delete(PartnerAppTargets partnerAppTargets);

    @Update
    int update(PartnerAppTargets partnerAppTargets);

    //Delete one item by id
    @Query("DELETE FROM PartnerAppTargets WHERE id = :itemId")
    void deleteById(int itemId);

    //Delete multiple item NOT IN api data
    @Query("DELETE FROM PartnerAppTargets WHERE task_offer_id = :itemId")
    void deleteMultipleByTaskOfferId(int itemId);

    @Query("DELETE FROM PartnerAppTargets WHERE id IN (:itemId)")
    void deleteMultipleByIds(int[] itemId);

    //Delete All
    @Query("DELETE FROM PartnerAppTargets")
    void deleteAll();
}
