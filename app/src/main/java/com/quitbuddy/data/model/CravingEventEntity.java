package com.quitbuddy.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "craving_event", indices = {@Index(value = "timestamp")})
public class CravingEventEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public Date timestamp;
    public int intensity;
    @NonNull
    public String trigger;
    public boolean didSmoke;
    public String note;

    public CravingEventEntity() {
    }

    @Ignore
    public CravingEventEntity(@NonNull Date timestamp, int intensity, @NonNull String trigger,
                              boolean didSmoke, String note) {
        this.timestamp = timestamp;
        this.intensity = intensity;
        this.trigger = trigger;
        this.didSmoke = didSmoke;
        this.note = note;
    }
}
