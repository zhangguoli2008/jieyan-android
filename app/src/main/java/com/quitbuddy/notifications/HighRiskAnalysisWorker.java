package com.quitbuddy.notifications;

import android.app.AlarmManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.quitbuddy.data.AppDatabase;
import com.quitbuddy.data.dao.CravingEventDao;
import com.quitbuddy.data.model.CravingEventEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HighRiskAnalysisWorker extends Worker {

    public HighRiskAnalysisWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        CravingEventDao dao = AppDatabase.getInstance(context).cravingEventDao();
        Calendar calendar = Calendar.getInstance();
        Date end = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date start = calendar.getTime();
        List<CravingEventEntity> events = dao.loadBetween(start, end);
        if (events == null || events.isEmpty()) {
            return Result.success();
        }
        Map<Integer, Integer> buckets = new HashMap<>();
        Calendar eventCal = Calendar.getInstance();
        for (CravingEventEntity event : events) {
            eventCal.setTime(event.timestamp);
            int hour = eventCal.get(Calendar.HOUR_OF_DAY);
            buckets.put(hour, buckets.getOrDefault(hour, 0) + 1);
        }
        List<Integer> topHours = buckets.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return Result.success();
        }
        for (int i = 0; i < topHours.size(); i++) {
            int hour = topHours.get(i);
            Calendar trigger = Calendar.getInstance();
            trigger.set(Calendar.HOUR_OF_DAY, hour);
            trigger.set(Calendar.MINUTE, 0);
            trigger.set(Calendar.SECOND, 0);
            trigger.set(Calendar.MILLISECOND, 0);
            trigger.add(Calendar.MINUTE, -10);
            if (trigger.before(Calendar.getInstance())) {
                trigger.add(Calendar.DAY_OF_YEAR, 1);
            }
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(),
                    SyncScheduler.createHighRiskPendingIntent(context, i, 10));
        }
        return Result.success();
    }
}
