package com.quitbuddy.data.model;

import java.time.ZonedDateTime;

public class DashboardSnapshot {
    public final long smokeFreeDays;
    public final long cigarettesAvoided;
    public final double moneySaved;
    public final long minutesRecovered;
    public final String quitMode;
    public final MilestoneInfo nextMilestone;
    public final double milestoneProgress;
    public final long cravingsLogged;

    public DashboardSnapshot(long smokeFreeDays,
                              long cigarettesAvoided,
                              double moneySaved,
                              long minutesRecovered,
                              String quitMode,
                              MilestoneInfo nextMilestone,
                              double milestoneProgress,
                              long cravingsLogged) {
        this.smokeFreeDays = smokeFreeDays;
        this.cigarettesAvoided = cigarettesAvoided;
        this.moneySaved = moneySaved;
        this.minutesRecovered = minutesRecovered;
        this.quitMode = quitMode;
        this.nextMilestone = nextMilestone;
        this.milestoneProgress = milestoneProgress;
        this.cravingsLogged = cravingsLogged;
    }

    public static DashboardSnapshot empty() {
        return new DashboardSnapshot(0, 0, 0, 0, null, null, 0f, 0);
    }

    public static class MilestoneInfo {
        public final int milestoneDays;
        public final ZonedDateTime targetDate;
        public final long daysRemaining;
        public final boolean completed;

        public MilestoneInfo(int milestoneDays, ZonedDateTime targetDate, long daysRemaining, boolean completed) {
            this.milestoneDays = milestoneDays;
            this.targetDate = targetDate;
            this.daysRemaining = daysRemaining;
            this.completed = completed;
        }
    }
}
