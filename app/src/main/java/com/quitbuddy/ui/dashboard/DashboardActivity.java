package com.quitbuddy.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.quitbuddy.R;
import com.quitbuddy.data.model.DashboardSnapshot;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.notifications.NotificationPermissionHelper;
import com.quitbuddy.ui.achievements.AchievementsActivity;
import com.quitbuddy.ui.craving.CravingHistoryActivity;
import com.quitbuddy.ui.craving.CravingLogActivity;
import com.quitbuddy.ui.export.ExportDataActivity;
import com.quitbuddy.ui.intervention.MicroInterventionActivity;
import com.quitbuddy.ui.settings.SettingsActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView textSmokeFreeDays;
    private TextView textMoneySaved;
    private TextView textCigsAvoided;
    private TextView textMinutesRecovered;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bindViews();
        loadSnapshot();
        NotificationPermissionHelper.requestIfNeeded(this);
    }

    private void bindViews() {
        textSmokeFreeDays = findViewById(R.id.textSmokeFreeDays);
        textMoneySaved = findViewById(R.id.textMoneySaved);
        textCigsAvoided = findViewById(R.id.textCigsAvoided);
        textMinutesRecovered = findViewById(R.id.textMinutesRecovered);
        MaterialButton buttonLog = findViewById(R.id.buttonLogCraving);
        MaterialButton buttonIntervention = findViewById(R.id.buttonIntervention);
        MaterialButton buttonHistory = findViewById(R.id.buttonHistory);
        MaterialButton buttonSettings = findViewById(R.id.buttonSettings);
        MaterialButton buttonAchievements = findViewById(R.id.buttonAchievements);
        MaterialButton buttonExport = findViewById(R.id.buttonExport);

        buttonLog.setOnClickListener(v -> startActivity(new Intent(this, CravingLogActivity.class)));
        buttonIntervention.setOnClickListener(v -> startActivity(new Intent(this, MicroInterventionActivity.class)));
        buttonHistory.setOnClickListener(v -> startActivity(new Intent(this, CravingHistoryActivity.class)));
        buttonSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        buttonAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        buttonExport.setOnClickListener(v -> startActivity(new Intent(this, ExportDataActivity.class)));
    }

    private void loadSnapshot() {
        QuitBuddyRepository.getInstance(this).calculateSnapshot(snapshot -> {
            textSmokeFreeDays.setText(String.valueOf(snapshot.smokeFreeDays));
            textMoneySaved.setText(currencyFormat.format(snapshot.moneySaved));
            textCigsAvoided.setText(String.valueOf(snapshot.cigarettesAvoided));
            textMinutesRecovered.setText(String.valueOf(snapshot.minutesRecovered));
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
