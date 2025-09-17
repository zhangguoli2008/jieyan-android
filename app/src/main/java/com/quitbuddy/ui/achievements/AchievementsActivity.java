package com.quitbuddy.ui.achievements;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quitbuddy.R;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.quitbuddy.ui.ThemeManager;

public class AchievementsActivity extends AppCompatActivity {

    private AchievementsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        ThemeManager.tintToolbar(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        RecyclerView list = findViewById(R.id.listAchievements);
        adapter = new AchievementsAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        QuitBuddyRepository.getInstance(this).loadAchievements(adapter::submitList);
    }
}
