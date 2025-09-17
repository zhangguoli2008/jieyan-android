package com.quitbuddy.data.model;

public class DashboardSnapshot {
    public final long smokeFreeDays;
    public final long cigarettesAvoided;
    public final double moneySaved;
    public final long minutesRecovered;

    public DashboardSnapshot(long smokeFreeDays, long cigarettesAvoided, double moneySaved, long minutesRecovered) {
        this.smokeFreeDays = smokeFreeDays;
        this.cigarettesAvoided = cigarettesAvoided;
        this.moneySaved = moneySaved;
        this.minutesRecovered = minutesRecovered;
    }
}
