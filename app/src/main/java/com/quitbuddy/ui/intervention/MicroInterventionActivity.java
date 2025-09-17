package com.quitbuddy.ui.intervention;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.quitbuddy.R;

import java.util.Random;

public class MicroInterventionActivity extends AppCompatActivity {

    private static final long BREATHING_DURATION = 60000L;
    private static final long DELAY_DURATION = 180000L;

    private TextView textBreathing;
    private TextView textTimer;
    private TextView textSuggestion;
    private CountDownTimer breathingTimer;
    private CountDownTimer delayTimer;
    private long breathingRemaining = BREATHING_DURATION;
    private long delayRemaining = DELAY_DURATION;
    private boolean breathingRunning = false;
    private boolean delayRunning = false;
    private final Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_micro_intervention);
        if (savedInstanceState != null) {
            breathingRemaining = savedInstanceState.getLong("breathingRemaining", BREATHING_DURATION);
            delayRemaining = savedInstanceState.getLong("delayRemaining", DELAY_DURATION);
            breathingRunning = savedInstanceState.getBoolean("breathingRunning", false);
            delayRunning = savedInstanceState.getBoolean("delayRunning", false);
        }
        bindViews();
        updateTimerText();
        if (breathingRunning) {
            startBreathing();
        }
        if (delayRunning) {
            startDelayTimer();
        }
    }

    private void bindViews() {
        textBreathing = findViewById(R.id.textBreathing);
        textTimer = findViewById(R.id.textTimer);
        textSuggestion = findViewById(R.id.textSuggestion);
        MaterialButton buttonBreathing = findViewById(R.id.buttonBreathing);
        MaterialButton buttonTimerStart = findViewById(R.id.buttonTimerStart);
        MaterialButton buttonTimerPause = findViewById(R.id.buttonTimerPause);
        MaterialButton buttonSuggestion = findViewById(R.id.buttonSuggestion);

        buttonBreathing.setOnClickListener(v -> {
            breathingRemaining = BREATHING_DURATION;
            startBreathing();
        });
        buttonTimerStart.setOnClickListener(v -> startDelayTimer());
        buttonTimerPause.setOnClickListener(v -> pauseDelayTimer());
        buttonSuggestion.setOnClickListener(v -> refreshSuggestion());
        refreshSuggestion();
    }

    private void startBreathing() {
        cancelBreathing();
        breathingRunning = true;
        breathingTimer = new CountDownTimer(breathingRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                breathingRemaining = millisUntilFinished;
                long elapsed = BREATHING_DURATION - breathingRemaining;
                int phase = (int) ((elapsed / 4000) % 4);
                switch (phase) {
                    case 0:
                        textBreathing.setText("吸气 4 秒");
                        break;
                    case 1:
                        textBreathing.setText("屏息 4 秒");
                        break;
                    case 2:
                        textBreathing.setText("呼气 4 秒");
                        break;
                    default:
                        textBreathing.setText("停留 4 秒");
                        break;
                }
            }

            @Override
            public void onFinish() {
                breathingRunning = false;
                textBreathing.setText("完成！为自己点赞");
            }
        }.start();
    }

    private void cancelBreathing() {
        if (breathingTimer != null) {
            breathingTimer.cancel();
            breathingTimer = null;
        }
    }

    private void startDelayTimer() {
        cancelDelay();
        delayRunning = true;
        delayTimer = new CountDownTimer(delayRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                delayRemaining = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                delayRunning = false;
                delayRemaining = DELAY_DURATION;
                updateTimerText();
                textTimer.setText("完成！做点开心的事吧");
            }
        }.start();
    }

    private void pauseDelayTimer() {
        cancelDelay();
        delayRunning = false;
    }

    private void cancelDelay() {
        if (delayTimer != null) {
            delayTimer.cancel();
            delayTimer = null;
        }
    }

    private void updateTimerText() {
        long seconds = delayRemaining / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        textTimer.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    private void refreshSuggestion() {
        String[] suggestions = getResources().getStringArray(R.array.substitution_suggestions);
        textSuggestion.setText(suggestions[random.nextInt(suggestions.length)]);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelBreathing();
        cancelDelay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("breathingRemaining", breathingRemaining);
        outState.putLong("delayRemaining", delayRemaining);
        outState.putBoolean("breathingRunning", breathingRunning);
        outState.putBoolean("delayRunning", delayRunning);
    }
}
