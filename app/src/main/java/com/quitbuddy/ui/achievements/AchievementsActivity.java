package com.quitbuddy.ui.achievements;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quitbuddy.R;
import com.quitbuddy.data.repo.QuitBuddyRepository;

public class AchievementsActivity extends AppCompatActivity {

    private AchievementsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
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
