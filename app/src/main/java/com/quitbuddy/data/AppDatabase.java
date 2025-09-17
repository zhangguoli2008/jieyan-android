package com.quitbuddy.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quitbuddy.data.converter.DateConverters;
import com.quitbuddy.data.converter.StringListConverter;
import com.quitbuddy.data.dao.CravingEventDao;
import com.quitbuddy.data.dao.QuitPlanDao;
import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.model.QuitPlanEntity;

@Database(entities = {QuitPlanEntity.class, CravingEventEntity.class}, version = 1, exportSchema = true)
@TypeConverters({DateConverters.class, StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract QuitPlanDao quitPlanDao();

    public abstract CravingEventDao cravingEventDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quitbuddy.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
