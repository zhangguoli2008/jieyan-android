package com.quitbuddy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TIME = "extra_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        String time = intent.getStringExtra(EXTRA_TIME);
        NotificationHelper.sendReminder(context, "戒烟提醒", "按照计划深呼吸，保持无烟");
        if (time != null) {
            SyncScheduler.scheduleSingleReminder(context, time, intent.getIntExtra("index", 0));
        }
    }
}
