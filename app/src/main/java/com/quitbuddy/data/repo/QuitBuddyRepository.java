package com.quitbuddy.data.repo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;

import com.quitbuddy.data.AppDatabase;
import com.quitbuddy.data.AppExecutors;
import com.quitbuddy.data.dao.CravingEventDao;
import com.quitbuddy.data.dao.QuitPlanDao;
import com.quitbuddy.data.model.Achievement;
import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.model.DashboardSnapshot;
import com.quitbuddy.data.model.MilestoneCelebration;
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
    private final SharedPreferences celebrationPrefs;
    private static final String PREFS_CELEBRATION = "celebration_prefs";
    private static final String KEY_LAST_CELEBRATED = "lastCelebrated";
    private static final int[] MILESTONES = new int[]{3, 7, 14, 30, 90, 180, 365};

    private QuitBuddyRepository(Context context) {
        this.appContext = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.appContext);
        this.planDao = database.quitPlanDao();
        this.eventDao = database.cravingEventDao();
        this.executors = AppExecutors.getInstance();
        this.celebrationPrefs = this.appContext.getSharedPreferences(PREFS_CELEBRATION, Context.MODE_PRIVATE);
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
            DashboardSnapshot snapshot = plan == null ? DashboardSnapshot.empty()
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
        long cravingsLogged = eventDao.count();
        DashboardSnapshot.MilestoneInfo milestoneInfo = computeNextMilestone(start, days);
        double milestoneProgress = milestoneInfo == null || milestoneInfo.completed
                ? 1f
                : Math.min(1f, (double) days / milestoneInfo.milestoneDays);
        return new DashboardSnapshot(days, avoided, moneySaved, minutesRecovered, plan.mode,
                milestoneInfo, milestoneProgress, cravingsLogged);
    }

    private DashboardSnapshot.MilestoneInfo computeNextMilestone(ZonedDateTime start, long days) {
        for (int milestone : MILESTONES) {
            if (days < milestone) {
                ZonedDateTime target = start.plusDays(milestone);
                long remaining = Math.max(0, milestone - days);
                return new DashboardSnapshot.MilestoneInfo(milestone, target, remaining, false);
            }
        }
        if (MILESTONES.length == 0) {
            return null;
        }
        int last = MILESTONES[MILESTONES.length - 1];
        ZonedDateTime target = start.plusDays(last);
        return new DashboardSnapshot.MilestoneInfo(last, target, 0, true);
    }

    public DashboardSnapshot getSnapshotSync() {
        QuitPlanEntity plan = planDao.getPlanSync();
        if (plan == null) {
            return DashboardSnapshot.empty();
        }
        return buildSnapshot(plan);
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
                for (int milestone : MILESTONES) {
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

    public void checkPendingCelebration(Callback<MilestoneCelebration> callback) {
        executors.diskIO().execute(() -> {
            QuitPlanEntity plan = planDao.getPlanSync();
            MilestoneCelebration celebration = null;
            if (plan != null) {
                DashboardSnapshot snapshot = buildSnapshot(plan);
                int lastCelebrated = celebrationPrefs.getInt(KEY_LAST_CELEBRATED, 0);
                int unlocked = 0;
                for (int milestone : MILESTONES) {
                    if (snapshot.smokeFreeDays >= milestone) {
                        unlocked = milestone;
                    }
                }
                if (unlocked > lastCelebrated) {
                    celebration = new MilestoneCelebration(unlocked, snapshot.smokeFreeDays,
                            snapshot.nextMilestone != null ? snapshot.nextMilestone.targetDate : ZonedDateTime.now());
                }
            }
            MilestoneCelebration result = celebration;
            executors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    public void markCelebrated(int milestoneDays) {
        celebrationPrefs.edit().putInt(KEY_LAST_CELEBRATED, milestoneDays).apply();
    }

    public void clearCravings(Runnable completion) {
        executors.diskIO().execute(() -> {
            eventDao.clearAll();
            DashboardWidgetProvider.updateWidget(appContext);
            if (completion != null) {
                executors.mainThread().execute(completion);
            }
        });
    }

    public void updateReminderTime(int index, String time, Runnable completion) {
        executors.diskIO().execute(() -> {
            QuitPlanEntity plan = planDao.getPlanSync();
            if (plan == null) {
                if (completion != null) {
                    executors.mainThread().execute(completion);
                }
                return;
            }
            List<String> times = new ArrayList<>(plan.reminderTimes);
            while (times.size() <= index) {
                times.add(time);
            }
            times.set(index, time);
            plan.reminderTimes = times;
            planDao.insert(plan);
            SyncScheduler.scheduleSingleReminder(appContext, time, index);
            if (completion != null) {
                executors.mainThread().execute(completion);
            }
        });
    }

    public LiveData<List<CravingEventEntity>> observeFilteredCravings(String trigger, Integer didSmoke, String query) {
        String normalizedTrigger = TextUtils.isEmpty(trigger) || "all".equals(trigger) ? null : trigger;
        Integer normalizedDidSmoke = didSmoke;
        String normalizedQuery = TextUtils.isEmpty(query) ? null : query;
        return eventDao.observeFiltered(normalizedTrigger, normalizedDidSmoke, normalizedQuery);
    }
}
