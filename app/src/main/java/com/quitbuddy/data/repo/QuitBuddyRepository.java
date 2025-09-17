package com.quitbuddy.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.quitbuddy.data.AppDatabase;
import com.quitbuddy.data.AppExecutors;
import com.quitbuddy.data.dao.CravingEventDao;
import com.quitbuddy.data.dao.QuitPlanDao;
import com.quitbuddy.data.model.Achievement;
import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.model.DashboardSnapshot;
import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.notifications.SyncScheduler;
import com.quitbuddy.widget.DashboardWidgetProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuitBuddyRepository {

    private static volatile QuitBuddyRepository INSTANCE;
    private final QuitPlanDao planDao;
    private final CravingEventDao eventDao;
    private final AppExecutors executors;
    private final Context appContext;

    private QuitBuddyRepository(Context context) {
        this.appContext = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.appContext);
        this.planDao = database.quitPlanDao();
        this.eventDao = database.cravingEventDao();
        this.executors = AppExecutors.getInstance();
    }

    public static QuitBuddyRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuitBuddyRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new QuitBuddyRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<QuitPlanEntity> observePlan() {
        return planDao.observePlan();
    }

    public void savePlan(QuitPlanEntity plan, Runnable completion) {
        executors.diskIO().execute(() -> {
            planDao.clear();
            planDao.insert(plan);
            SyncScheduler.scheduleAll(appContext);
            DashboardWidgetProvider.updateWidget(appContext);
            if (completion != null) {
                executors.mainThread().execute(completion);
            }
        });
    }

    public void logCraving(CravingEventEntity event, Runnable completion) {
        executors.diskIO().execute(() -> {
            eventDao.insert(event);
            DashboardWidgetProvider.updateWidget(appContext);
            if (completion != null) {
                executors.mainThread().execute(completion);
            }
        });
    }

    public LiveData<List<CravingEventEntity>> observeCravings() {
        return eventDao.observeAll();
    }

    public void calculateSnapshot(Callback<DashboardSnapshot> callback) {
        executors.diskIO().execute(() -> {
            QuitPlanEntity plan = planDao.getPlanSync();
            DashboardSnapshot snapshot = plan == null ? new DashboardSnapshot(0, 0, 0, 0)
                    : buildSnapshot(plan);
            executors.mainThread().execute(() -> callback.onResult(snapshot));
        });
    }

    private DashboardSnapshot buildSnapshot(QuitPlanEntity plan) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = ZonedDateTime.ofInstant(plan.startDate.toInstant(), ZoneId.systemDefault());
        long days = Math.max(0, Duration.between(start.toLocalDate().atStartOfDay(), now).toDays());
        long elapsedCigs = (long) plan.dailyBaseline * Math.max(0, days + 1);
        int smoked = eventDao.getSmokedCount();
        long avoided = Math.max(0, elapsedCigs - smoked);
        double moneySaved = plan.cigsPerPack == 0 ? 0 : (double) avoided / plan.cigsPerPack * plan.pricePerPack;
        long minutesRecovered = avoided * 8L;
        return new DashboardSnapshot(days, avoided, moneySaved, minutesRecovered);
    }

    public interface Callback<T> {
        void onResult(T data);
    }

    public void loadAchievements(Callback<List<Achievement>> callback) {
        executors.diskIO().execute(() -> {
            QuitPlanEntity plan = planDao.getPlanSync();
            List<Achievement> achievements = new ArrayList<>();
            if (plan != null) {
                DashboardSnapshot snapshot = buildSnapshot(plan);
                long days = snapshot.smokeFreeDays;
                int[] milestones = new int[]{3, 7, 14, 30, 90, 180, 365};
                for (int milestone : milestones) {
                    boolean achieved = days >= milestone;
                    achievements.add(new Achievement(milestone + " 天", achieved,
                            achieved ? "已达成" : "继续坚持"));
                }
                ZonedDateTime now = ZonedDateTime.now();
                ZonedDateTime weekAgo = now.minusDays(7);
                int smoked = eventDao.countSmokedBetween(Date.from(weekAgo.toInstant()), Date.from(now.toInstant()));
                achievements.add(new Achievement("连续 7 日未吸", smoked == 0,
                        smoked == 0 ? "保持无烟状态" : "从今天重新开始"));
            }
            executors.mainThread().execute(() -> callback.onResult(achievements));
        });
    }

    public void exportCravings(File file, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            boolean success;
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("timestamp,intensity,trigger,didSmoke,note\n");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                List<CravingEventEntity> events = eventDao.loadBetween(new Date(0), new Date());
                for (CravingEventEntity event : events) {
                    writer.write(String.format(Locale.getDefault(), "%s,%d,%s,%s,%s\n",
                            format.format(event.timestamp),
                            event.intensity,
                            event.trigger,
                            event.didSmoke,
                            sanitize(event.note)));
                }
                success = true;
            } catch (IOException e) {
                success = false;
            }
            boolean finalSuccess = success;
            executors.mainThread().execute(() -> callback.onResult(finalSuccess));
        });
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace(',', ';');
    }
}
