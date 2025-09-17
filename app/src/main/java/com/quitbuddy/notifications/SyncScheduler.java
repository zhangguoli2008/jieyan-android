package com.quitbuddy.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.quitbuddy.data.AppDatabase;
import com.quitbuddy.data.AppExecutors;
import com.quitbuddy.data.model.QuitPlanEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SyncScheduler {

    private static final String WORK_HIGH_RISK = "highRiskWork";
    private static final int REMINDER_BASE_ID = 2000;
    private static final int MILESTONE_ID = 3000;

    private SyncScheduler() {
    }

    public static void scheduleAll(Context context) {
        scheduleDailyReminders(context);
        scheduleMilestones(context);
    }

    public static void scheduleDailyReminders(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            QuitPlanEntity plan = AppDatabase.getInstance(context).quitPlanDao().getPlanSync();
            if (plan == null) {
                return;
            }
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager == null) {
                return;
            }
            List<String> times = plan.reminderTimes;
            for (int i = 0; i < times.size(); i++) {
                scheduleSingleReminderInternal(context, times.get(i), i, manager);
            }
        });
    }

    public static void scheduleSingleReminder(Context context, String time, int index) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null || TextUtils.isEmpty(time)) {
            return;
        }
        scheduleSingleReminderInternal(context, time, index, manager);
    }

    private static void scheduleSingleReminderInternal(Context context, String time, int index, AlarmManager manager) {
        String[] parts = time.split(":");
        if (parts.length != 2) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TIME, time);
        intent.putExtra("index", index);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REMINDER_BASE_ID + index, intent, pendingIntentFlags());
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void scheduleMilestones(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            QuitPlanEntity plan = AppDatabase.getInstance(context).quitPlanDao().getPlanSync();
            if (plan == null) {
                return;
            }
            LocalDate startDate = plan.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int[] milestones = new int[]{3, 7, 14, 30, 90, 180, 365};
            LocalDate today = LocalDate.now();
            for (int milestone : milestones) {
                LocalDate target = startDate.plusDays(milestone);
                if (!today.isAfter(target)) {
                    scheduleMilestoneAlarm(context, target, milestone);
                    break;
                }
            }
        });
    }

    private static void scheduleMilestoneAlarm(Context context, LocalDate targetDate, int milestone) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, targetDate.getYear());
        calendar.set(Calendar.MONTH, targetDate.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, targetDate.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            return;
        }
        Intent intent = new Intent(context, MilestoneReceiver.class);
        intent.putExtra(MilestoneReceiver.EXTRA_MESSAGE, "恭喜你达成 " + milestone + " 天无烟！");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MILESTONE_ID, intent, pendingIntentFlags());
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void scheduleHighRiskAnalysis(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(HighRiskAnalysisWorker.class, 6, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_HIGH_RISK, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    public static void toggleCloudSync(@NonNull Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("pref_sync", enabled)
                .apply();
        if (enabled) {
            scheduleHighRiskAnalysis(context);
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_HIGH_RISK);
        }
    }

    static PendingIntent createHighRiskPendingIntent(Context context, int requestCode, int minutesBefore) {
        Intent intent = new Intent(context, HighRiskReceiver.class);
        intent.putExtra(HighRiskReceiver.EXTRA_WINDOW, String.valueOf(minutesBefore));
        return PendingIntent.getBroadcast(context, 4000 + requestCode, intent, pendingIntentFlags());
    }

    static int pendingIntentFlags() {
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }
}
