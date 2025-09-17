package com.quitbuddy.ui.intervention;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MicroInterventionViewModel extends ViewModel {

    public static final long BREATHING_DURATION = 60000L;
    public static final long DELAY_DURATION = 180000L;

    private final MutableLiveData<Long> breathingRemaining = new MutableLiveData<>(BREATHING_DURATION);
    private final MutableLiveData<Boolean> breathingRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Long> delayRemaining = new MutableLiveData<>(DELAY_DURATION);
    private final MutableLiveData<Boolean> delayRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> delayFinished = new MutableLiveData<>(false);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long breathingEndTime;
    private long delayEndTime;

    private final Runnable breathingTick = new Runnable() {
        @Override
        public void run() {
            if (!Boolean.TRUE.equals(breathingRunning.getValue())) {
                return;
            }
            long remaining = Math.max(0, breathingEndTime - SystemClock.elapsedRealtime());
            breathingRemaining.setValue(remaining);
            if (remaining <= 0) {
                breathingRunning.setValue(false);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private final Runnable delayTick = new Runnable() {
        @Override
        public void run() {
            if (!Boolean.TRUE.equals(delayRunning.getValue())) {
                return;
            }
            long remaining = Math.max(0, delayEndTime - SystemClock.elapsedRealtime());
            delayRemaining.setValue(remaining);
            if (remaining <= 0) {
                delayRunning.setValue(false);
                delayRemaining.setValue(DELAY_DURATION);
                delayFinished.setValue(true);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    public LiveData<Long> getBreathingRemaining() {
        return breathingRemaining;
    }

    public LiveData<Boolean> isBreathingRunning() {
        return breathingRunning;
    }

    public LiveData<Long> getDelayRemaining() {
        return delayRemaining;
    }

    public LiveData<Boolean> isDelayRunning() {
        return delayRunning;
    }

    public LiveData<Boolean> getDelayFinished() {
        return delayFinished;
    }

    public void startBreathing() {
        long remaining = getBreathingRemainingValue();
        if (remaining <= 0 || remaining > BREATHING_DURATION) {
            remaining = BREATHING_DURATION;
            breathingRemaining.setValue(BREATHING_DURATION);
        }
        breathingEndTime = SystemClock.elapsedRealtime() + remaining;
        breathingRunning.setValue(true);
        handler.removeCallbacks(breathingTick);
        handler.post(breathingTick);
    }

    public void resetBreathing() {
        breathingRunning.setValue(false);
        breathingRemaining.setValue(BREATHING_DURATION);
        handler.removeCallbacks(breathingTick);
    }

    public void startDelay() {
        delayEndTime = SystemClock.elapsedRealtime() + getDelayRemainingValue();
        delayRunning.setValue(true);
        handler.removeCallbacks(delayTick);
        handler.post(delayTick);
    }

    public void pauseDelay() {
        if (Boolean.TRUE.equals(delayRunning.getValue())) {
            long remaining = Math.max(0, delayEndTime - SystemClock.elapsedRealtime());
            delayRemaining.setValue(remaining);
        }
        delayRunning.setValue(false);
        handler.removeCallbacks(delayTick);
    }

    public void consumeDelayFinished() {
        delayFinished.setValue(false);
    }

    private long getBreathingRemainingValue() {
        Long value = breathingRemaining.getValue();
        return value == null ? BREATHING_DURATION : value;
    }

    private long getDelayRemainingValue() {
        Long value = delayRemaining.getValue();
        return value == null ? DELAY_DURATION : value;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(breathingTick);
        handler.removeCallbacks(delayTick);
    }
}
