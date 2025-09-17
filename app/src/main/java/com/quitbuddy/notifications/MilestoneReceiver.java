package com.quitbuddy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MilestoneReceiver extends BroadcastReceiver {

    public static final String EXTRA_MESSAGE = "extra_message";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        if (message == null) {
            message = "继续坚持，新的里程碑达成！";
        }
        NotificationHelper.sendMilestone(context, message);
    }
}
