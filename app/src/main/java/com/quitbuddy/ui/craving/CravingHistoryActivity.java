package com.quitbuddy.ui.craving;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quitbuddy.R;
import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.util.List;

public class CravingHistoryActivity extends AppCompatActivity {

    private CravingHistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_craving_history);
        RecyclerView list = findViewById(R.id.listCravings);
        adapter = new CravingHistoryAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        observeData();
    }

    private void observeData() {
        QuitBuddyRepository.getInstance(this).observeCravings().observe(this, new Observer<List<CravingEventEntity>>() {
            @Override
            public void onChanged(List<CravingEventEntity> cravingEventEntities) {
                adapter.submitList(cravingEventEntities);
            }
        });
    }
}
