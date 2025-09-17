package com.quitbuddy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.quitbuddy.data.model.CravingEventEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface CravingEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CravingEventEntity event);

    @Query("SELECT * FROM craving_event ORDER BY timestamp DESC")
    LiveData<List<CravingEventEntity>> observeAll();

    @Query("SELECT * FROM craving_event WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    List<CravingEventEntity> loadBetween(Date start, Date end);

    @Query("SELECT COUNT(*) FROM craving_event WHERE didSmoke = 1")
    int getSmokedCount();

    @Query("SELECT COUNT(*) FROM craving_event")
    int count();

    @Query("SELECT COUNT(*) FROM craving_event WHERE didSmoke = 1 AND timestamp BETWEEN :start AND :end")
    int countSmokedBetween(Date start, Date end);
}
