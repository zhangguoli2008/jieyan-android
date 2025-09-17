package com.quitbuddy.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.ui.dashboard.DashboardActivity;
import com.quitbuddy.ui.onboarding.OnboardingActivity;

public class MainActivity extends AppCompatActivity {

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
        QuitBuddyRepository.getInstance(this).observePlan().observe(this, planObserver);
    }
}
