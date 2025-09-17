package com.quitbuddy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.quitbuddy.data.model.QuitPlanEntity;

@Dao
public interface QuitPlanDao {
    @Query("SELECT * FROM quit_plan LIMIT 1")
    LiveData<QuitPlanEntity> observePlan();

    @Query("SELECT * FROM quit_plan LIMIT 1")
    QuitPlanEntity getPlanSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(QuitPlanEntity plan);

    @Update
    void update(QuitPlanEntity plan);

    @Query("DELETE FROM quit_plan")
    void clear();
}
