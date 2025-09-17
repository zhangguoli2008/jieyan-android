package com.quitbuddy.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.quitbuddy.R;
import com.quitbuddy.data.model.DashboardSnapshot;
import com.quitbuddy.data.model.MilestoneCelebration;
import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.notifications.NotificationPermissionHelper;
import com.quitbuddy.ui.ThemeManager;
import com.quitbuddy.ui.achievements.AchievementsActivity;
import com.quitbuddy.ui.craving.CravingHistoryActivity;
import com.quitbuddy.ui.craving.CravingLogActivity;
import com.quitbuddy.ui.export.ExportDataActivity;
import com.quitbuddy.ui.intervention.MicroInterventionActivity;
import com.quitbuddy.ui.settings.SettingsActivity;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView textSmokeFreeDays;
    private TextView textMoneySaved;
    private TextView textCigsAvoided;
    private TextView textMinutesRecovered;
    private TextView textQuitMode;
    private TextView textCravingsLogged;
    private TextView textNextMilestone;
    private TextView textMilestoneEta;
    private TextView textMilestoneMessage;
    private LinearProgressIndicator progressMilestone;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ThemeManager.tintToolbar(toolbar);
        bindViews();
        loadSnapshot();
        NotificationPermissionHelper.requestIfNeeded(this);
    }

    private void bindViews() {
        textSmokeFreeDays = findViewById(R.id.textSmokeFreeDays);
        textMoneySaved = findViewById(R.id.textMoneySaved);
        textCigsAvoided = findViewById(R.id.textCigsAvoided);
        textMinutesRecovered = findViewById(R.id.textMinutesRecovered);
        textQuitMode = findViewById(R.id.textQuitMode);
        textCravingsLogged = findViewById(R.id.textCravingsLogged);
        textNextMilestone = findViewById(R.id.textNextMilestone);
        textMilestoneEta = findViewById(R.id.textMilestoneEta);
        textMilestoneMessage = findViewById(R.id.textMilestoneMessage);
        progressMilestone = findViewById(R.id.progressMilestone);

        Chip chipLog = findViewById(R.id.chipLogCraving);
        Chip chipIntervention = findViewById(R.id.chipIntervention);
        Chip chipHistory = findViewById(R.id.chipHistory);
        Chip chipAchievements = findViewById(R.id.chipAchievements);
        Chip chipExport = findViewById(R.id.chipExport);
        Chip chipSettings = findViewById(R.id.chipSettings);

        chipLog.setOnClickListener(v -> startActivity(new Intent(this, CravingLogActivity.class)));
        chipIntervention.setOnClickListener(v -> startActivity(new Intent(this, MicroInterventionActivity.class)));
        chipHistory.setOnClickListener(v -> startActivity(new Intent(this, CravingHistoryActivity.class)));
        chipAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        chipExport.setOnClickListener(v -> startActivity(new Intent(this, ExportDataActivity.class)));
        chipSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void loadSnapshot() {
        QuitBuddyRepository.getInstance(this).calculateSnapshot(snapshot -> {
            renderSnapshot(snapshot);
            checkCelebration();
        });
    }

    private void renderSnapshot(DashboardSnapshot snapshot) {
        textSmokeFreeDays.setText(numberFormat.format(snapshot.smokeFreeDays));
        textMoneySaved.setText(currencyFormat.format(snapshot.moneySaved));
        textCigsAvoided.setText(numberFormat.format(snapshot.cigarettesAvoided));
        textMinutesRecovered.setText(numberFormat.format(snapshot.minutesRecovered));
        textQuitMode.setText(getString(R.string.dashboard_mode_label, getModeDisplay(snapshot.quitMode)));
        textCravingsLogged.setText(getString(R.string.dashboard_cravings_logged, snapshot.cravingsLogged));

        if (snapshot.nextMilestone == null || snapshot.nextMilestone.completed) {
            textNextMilestone.setText(R.string.dashboard_milestone_completed);
            textMilestoneEta.setText("");
            textMilestoneMessage.setText("");
            progressMilestone.setProgressCompat(100, true);
        } else {
            String label = getString(R.string.dashboard_next_milestone, snapshot.nextMilestone.milestoneDays + " 天");
            textNextMilestone.setText(label);
            String eta = getString(R.string.dashboard_milestone_eta, dateFormatter.format(snapshot.nextMilestone.targetDate));
            textMilestoneEta.setText(eta);
            textMilestoneMessage.setText(getString(R.string.dashboard_milestone_remaining, snapshot.nextMilestone.daysRemaining));
            progressMilestone.setProgressCompat((int) Math.round(snapshot.milestoneProgress * 100), true);
        }
    }

    private String getModeDisplay(String mode) {
        if (TextUtils.isEmpty(mode)) {
            return getString(R.string.settings_quit_mode_empty);
        }
        if (QuitPlanEntity.MODE_GRADUAL.equals(mode)) {
            return getString(R.string.onboarding_mode_gradual);
        }
        return getString(R.string.onboarding_mode_cold_turkey);
    }

    private void checkCelebration() {
        QuitBuddyRepository.getInstance(this).checkPendingCelebration(celebration -> {
            if (celebration == null) {
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.achievements_celebration_title)
                    .setMessage(getString(R.string.achievements_celebration_message, celebration.milestoneDays))
                    .setPositiveButton(R.string.achievements_celebration_button, (dialog, which) ->
                            QuitBuddyRepository.getInstance(this).markCelebrated(celebration.milestoneDays))
                    .setOnDismissListener(dialog ->
                            QuitBuddyRepository.getInstance(this).markCelebrated(celebration.milestoneDays))
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSnapshot();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NotificationPermissionHelper.handleResult(this, requestCode, grantResults);
    }
}
