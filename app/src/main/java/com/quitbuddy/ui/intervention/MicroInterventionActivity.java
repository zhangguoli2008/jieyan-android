package com.quitbuddy.ui.intervention;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.quitbuddy.R;
import com.quitbuddy.ui.ThemeManager;

import java.util.Locale;
import java.util.Random;

public class MicroInterventionActivity extends AppCompatActivity {

    private TextView textBreathing;
    private TextView textTimer;
    private TextView textSuggestion;
    private CircularProgressIndicator indicatorBreathing;
    private LinearProgressIndicator indicatorDelay;
    private MicroInterventionViewModel viewModel;
    private final Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_micro_intervention);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        ThemeManager.tintToolbar(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        bindViews();
        viewModel = new ViewModelProvider(this).get(MicroInterventionViewModel.class);
        observeViewModel();
        refreshSuggestion();
    }

    private void bindViews() {
        textBreathing = findViewById(R.id.textBreathing);
        textTimer = findViewById(R.id.textTimer);
        textSuggestion = findViewById(R.id.textSuggestion);
        indicatorBreathing = findViewById(R.id.indicatorBreathing);
        indicatorDelay = findViewById(R.id.indicatorDelay);
        MaterialButton buttonBreathing = findViewById(R.id.buttonBreathing);
        MaterialButton buttonBreathingReset = findViewById(R.id.buttonBreathingReset);
        MaterialButton buttonTimerStart = findViewById(R.id.buttonTimerStart);
        MaterialButton buttonTimerPause = findViewById(R.id.buttonTimerPause);
        MaterialButton buttonSuggestion = findViewById(R.id.buttonSuggestion);

        buttonBreathing.setOnClickListener(v -> viewModel.startBreathing());
        buttonBreathingReset.setOnClickListener(v -> viewModel.resetBreathing());
        buttonTimerStart.setOnClickListener(v -> viewModel.startDelay());
        buttonTimerPause.setOnClickListener(v -> viewModel.pauseDelay());
        buttonSuggestion.setOnClickListener(v -> refreshSuggestion());
    }

    private void observeViewModel() {
        viewModel.getBreathingRemaining().observe(this, this::updateBreathingText);
        viewModel.getDelayRemaining().observe(this, this::updateDelayText);
        viewModel.getDelayFinished().observe(this, finished -> {
            if (Boolean.TRUE.equals(finished)) {
                Snackbar.make(textTimer, R.string.micro_intervention_delay_snackbar, Snackbar.LENGTH_LONG).show();
                viewModel.consumeDelayFinished();
            }
        });
    }

    private void updateBreathingText(long remaining) {
        if (remaining <= 0) {
            textBreathing.setText(R.string.micro_intervention_breath_complete);
            indicatorBreathing.setProgressCompat(100, true);
            return;
        }
        long elapsed = MicroInterventionViewModel.BREATHING_DURATION - remaining;
        int phase = (int) ((elapsed / 4000) % 4);
        switch (phase) {
            case 0:
                textBreathing.setText(R.string.micro_intervention_breath_inhale);
                break;
            case 1:
                textBreathing.setText(R.string.micro_intervention_breath_hold);
                break;
            case 2:
                textBreathing.setText(R.string.micro_intervention_breath_exhale);
                break;
            default:
                textBreathing.setText(R.string.micro_intervention_breath_rest);
                break;
        }
        int progress = (int) Math.min(100, Math.max(0,
                (MicroInterventionViewModel.BREATHING_DURATION - remaining) * 100 / MicroInterventionViewModel.BREATHING_DURATION));
        indicatorBreathing.setProgressCompat(progress, true);
    }

    private void updateDelayText(long remaining) {
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        String formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
        textTimer.setText(formatted);
        int progress = (int) Math.min(100, Math.max(0,
                (MicroInterventionViewModel.DELAY_DURATION - remaining) * 100 / MicroInterventionViewModel.DELAY_DURATION));
        indicatorDelay.setProgressCompat(progress, true);
    }

    private void refreshSuggestion() {
        String[] suggestions = getResources().getStringArray(R.array.substitution_suggestions);
        textSuggestion.setText(suggestions[random.nextInt(suggestions.length)]);
    }
}
