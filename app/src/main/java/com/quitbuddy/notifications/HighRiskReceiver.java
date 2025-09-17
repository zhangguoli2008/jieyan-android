package com.quitbuddy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HighRiskReceiver extends BroadcastReceiver {

    public static final String EXTRA_WINDOW = "extra_window";

    @Override
    public void onReceive(Context context, Intent intent) {
        String window = intent.getStringExtra(EXTRA_WINDOW);
        String message = window == null ? "即将进入高风险时段，试试微干预" : "距高风险时段 " + window + " 分钟，立即行动";
        NotificationHelper.sendHighRisk(context, message);
    }
}
