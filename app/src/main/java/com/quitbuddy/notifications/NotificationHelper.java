package com.quitbuddy.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.quitbuddy.R;
import com.quitbuddy.ui.dashboard.DashboardActivity;
import com.quitbuddy.ui.intervention.MicroInterventionActivity;

public final class NotificationHelper {

    private NotificationHelper() {
    }

    public static void sendReminder(Context context, String title, String message) {
        Intent intent = new Intent(context, DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags());
        Notification notification = new NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notify(context, 1001, notification);
    }

    public static void sendHighRisk(Context context, String message) {
        Intent intent = new Intent(context, MicroInterventionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, pendingIntentFlags());
        Notification notification = new NotificationCompat.Builder(context, NotificationChannels.CHANNEL_INTERVENTIONS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("高风险提醒")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notify(context, 1002, notification);
    }

    public static void sendMilestone(Context context, String message) {
        Intent intent = new Intent(context, DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, intent, pendingIntentFlags());
        Notification notification = new NotificationCompat.Builder(context, NotificationChannels.CHANNEL_MILESTONES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("新的里程碑")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notify(context, 1003, notification);
    }

    private static void notify(Context context, int id, Notification notification) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, notification);
        }
    }

    private static int pendingIntentFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }
}
