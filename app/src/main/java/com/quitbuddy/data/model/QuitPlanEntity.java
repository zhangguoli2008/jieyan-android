package com.quitbuddy.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "quit_plan")
public class QuitPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public Date startDate;
    @NonNull
    public String mode;
    public int dailyBaseline;
    public double pricePerPack;
    public int cigsPerPack;
    @NonNull
    public List<String> reminderTimes = new ArrayList<>();

    public static final String MODE_COLD_TURKEY = "coldTurkey";
    public static final String MODE_GRADUAL = "gradual";

    public QuitPlanEntity() {
    }

    @Ignore
    public QuitPlanEntity(@NonNull Date startDate, @NonNull String mode, int dailyBaseline,
                          double pricePerPack, int cigsPerPack, @NonNull List<String> reminderTimes) {
        this.startDate = startDate;
        this.mode = mode;
        this.dailyBaseline = dailyBaseline;
        this.pricePerPack = pricePerPack;
        this.cigsPerPack = cigsPerPack;
        this.reminderTimes = reminderTimes;
    }
}
