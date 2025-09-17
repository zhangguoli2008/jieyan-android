package com.quitbuddy.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationChannels {

    public static final String CHANNEL_REMINDERS = "reminders";
    public static final String CHANNEL_MILESTONES = "milestones";
    public static final String CHANNEL_INTERVENTIONS = "interventions";

    private NotificationChannels() {
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }
            NotificationChannel reminders = new NotificationChannel(CHANNEL_REMINDERS, "固定提醒", NotificationManager.IMPORTANCE_HIGH);
            reminders.setDescription("按计划发送的提醒");
            NotificationChannel milestones = new NotificationChannel(CHANNEL_MILESTONES, "里程碑", NotificationManager.IMPORTANCE_DEFAULT);
            milestones.setDescription("达成目标时的祝贺");
            NotificationChannel interventions = new NotificationChannel(CHANNEL_INTERVENTIONS, "微干预", NotificationManager.IMPORTANCE_DEFAULT);
            interventions.setDescription("高风险时段提醒");
            manager.createNotificationChannel(reminders);
            manager.createNotificationChannel(milestones);
            manager.createNotificationChannel(interventions);
        }
    }
}
