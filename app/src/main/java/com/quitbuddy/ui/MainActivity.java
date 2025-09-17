package com.quitbuddy.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.notifications.SyncScheduler;
import com.quitbuddy.ui.dashboard.DashboardActivity;
import com.quitbuddy.ui.onboarding.OnboardingActivity;
import android.app.AlarmManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    // —— 新增：用于拉起“精确闹钟”授权页
    private ActivityResultLauncher<Intent> exactAlarmLauncher;
    // —— 新增：本次会话内只弹一次，避免频繁打扰
    private boolean askedExactAlarmThisSession = false;
    private boolean askedExactAlarmOnce = false;   // 防止重复弹
    private boolean needRescheduleAfterReturn = false;
    private final Observer<QuitPlanEntity> planObserver = plan -> {
        if (plan == null) {
            startActivity(new Intent(this, OnboardingActivity.class));
        } else {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        finish();
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册授权结果回调：从系统设置页返回后，如果已经具备权限，就重排闹钟
        // 1) 注册 launcher：从设置页返回后，若已被授权就重新调度闹钟
        exactAlarmLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AlarmManager am = getSystemService(AlarmManager.class);
                        if (am != null && am.canScheduleExactAlarms()) {
                            // —— 新增：授权成功后统一在这里补一次调度
                            SyncScheduler.rescheduleAfterPermission(this);
                        }
                    }
                }
        );
        QuitBuddyRepository.getInstance(this).observePlan().observe(this, planObserver);
    }

    /** 首帧显示后再去检查并弹授权，避免界面覆盖 */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        // 用 post 保证首帧先渲染
        getWindow().getDecorView().post(this::maybeAskExactAlarmPermission);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 从设置页返回后再检查一次；若已授权则进行调度
        if (needRescheduleAfterReturn && canScheduleExactAlarms(this)) {
            needRescheduleAfterReturn = false;
            rescheduleAllAlarmsSafely(this);
        }
    }
    /** 引导授权；若已授权则直接调度 */
    private void ensureExactAlarmPermissionOrGuide() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Android 11 及以下无需权限，直接调度
            rescheduleAllAlarmsSafely(this);
            return;
        }
        if (canScheduleExactAlarms(this)) {
            rescheduleAllAlarmsSafely(this);
            return;
        }
        if (askedExactAlarmOnce) return; // 已经弹过了就不再弹

        askedExactAlarmOnce = true;
        new AlertDialog.Builder(this)
                .setTitle("需要授权精确定时")
                .setMessage("为确保提醒能按时触达，请在系统设置中授予“精确定时”权限。")
                .setNegativeButton("稍后", (d, w) -> {
                    // 不授权也不崩溃，只是提醒可能不准
                })
                .setPositiveButton("去授权", (d, w) -> {
                    openExactAlarmSettings();
                    // 记一下：从设置回来要再试着调度
                    needRescheduleAfterReturn = true;
                })
                .show();
    }

    /** 是否可以调度精确定时（Android 12+） */
    private static boolean canScheduleExactAlarms(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        return am != null && am.canScheduleExactAlarms();
    }

    /** 跳到系统授权页（兼容兜底） */
    private void openExactAlarmSettings() {
        Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(Uri.parse("package:" + getPackageName()));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            // 某些系统没有这个入口，兜底到应用详情页
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(fallback);
        }
    }

    /** 把你项目里的所有闹钟/提醒统一在这里触发一次 */
    private static void rescheduleAllAlarmsSafely(Context ctx) {
        // 这里按你项目中已有的方法调用。示例：
        try {
            SyncScheduler.scheduleDailyReminders(ctx);
        } catch (Throwable ignored) {}
        try {
            SyncScheduler.scheduleMilestones(ctx);
        } catch (Throwable ignored) {}
        try {
            SyncScheduler.scheduleHighRiskAnalysis(ctx);
        } catch (Throwable ignored) {}
    }



    /** 检查并引导“精确闹钟”授权（仅 Android 12+ 有效） */
    private void maybeAskExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

        AlarmManager am = getSystemService(AlarmManager.class);
        if (am == null) return;

        // 仅当还没权限且本会话未询问过时，才拉起设置页
        if (!am.canScheduleExactAlarms() && !askedExactAlarmThisSession) {
            askedExactAlarmThisSession = true;
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:" + getPackageName()));
            exactAlarmLauncher.launch(i);
        }
    }
}
