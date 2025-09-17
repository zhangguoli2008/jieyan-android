package com.quitbuddy.data.model;

import java.time.ZonedDateTime;

public class MilestoneCelebration {
    public final int milestoneDays;
    public final long smokeFreeDays;
    public final ZonedDateTime achievedAt;

    public MilestoneCelebration(int milestoneDays, long smokeFreeDays, ZonedDateTime achievedAt) {
        this.milestoneDays = milestoneDays;
        this.smokeFreeDays = smokeFreeDays;
        this.achievedAt = achievedAt;
    }
}
