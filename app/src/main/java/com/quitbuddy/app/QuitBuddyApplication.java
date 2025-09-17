package com.quitbuddy.app;

import android.app.Application;

import androidx.work.Configuration;

import com.quitbuddy.notifications.NotificationChannels;
import com.quitbuddy.notifications.SyncScheduler;
import com.quitbuddy.ui.ThemeManager;

public class QuitBuddyApplication extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);
        NotificationChannels.createChannels(this);
        SyncScheduler.scheduleHighRiskAnalysis(this);
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
