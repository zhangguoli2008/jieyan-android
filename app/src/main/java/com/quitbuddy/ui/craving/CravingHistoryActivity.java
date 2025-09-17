package com.quitbuddy.ui.craving;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textview.MaterialTextView;
import com.quitbuddy.R;
import com.quitbuddy.ui.ThemeManager;

public class CravingHistoryActivity extends AppCompatActivity {

    private CravingHistoryAdapter adapter;
    private CravingHistoryViewModel viewModel;
    private ChipGroup groupTriggers;
    private ChipGroup groupSmoked;
    private MaterialTextView textEmpty;
    private boolean updatingFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_craving_history);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        ThemeManager.tintToolbar(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.listCravings);
        textEmpty = findViewById(R.id.textEmpty);
        groupTriggers = findViewById(R.id.groupTriggers);
        groupSmoked = findViewById(R.id.groupSmoked);
        adapter = new CravingHistoryAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CravingHistoryViewModel.class);
        observeFilters();
        observeCravings();
        setupChips();
    }

    private void observeFilters() {
        viewModel.getFilters().observe(this, state -> {
            updatingFilters = true;
            applyTriggerState(state.triggerKey);
            applySmokedState(state.didSmoke);
            adapter.setSearchQuery(state.query);
            updatingFilters = false;
        });
    }

    private void observeCravings() {
        viewModel.getCravings().observe(this, events -> {
            adapter.submitList(events);
            textEmpty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupChips() {
        groupTriggers.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (updatingFilters) {
                return;
            }
            if (checkedIds.isEmpty()) {
                viewModel.setTrigger("all");
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipTriggerStress) {
                viewModel.setTrigger(getString(R.string.craving_history_filter_stress));
            } else if (id == R.id.chipTriggerSocial) {
                viewModel.setTrigger(getString(R.string.craving_history_filter_social));
            } else if (id == R.id.chipTriggerAfterMeal) {
                viewModel.setTrigger(getString(R.string.craving_history_filter_after_meal));
            } else if (id == R.id.chipTriggerOther) {
                viewModel.setTrigger(getString(R.string.craving_history_filter_other));
            } else {
                viewModel.setTrigger("all");
            }
        });

        groupSmoked.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (updatingFilters) {
                return;
            }
            if (checkedIds.isEmpty() || checkedIds.get(0) == R.id.chipSmokedAll) {
                viewModel.setDidSmoke(null);
            } else if (checkedIds.get(0) == R.id.chipSmokedYes) {
                viewModel.setDidSmoke(1);
            } else if (checkedIds.get(0) == R.id.chipSmokedNo) {
                viewModel.setDidSmoke(0);
            }
        });
    }

    private void applyTriggerState(String triggerKey) {
        int chipId;
        if (TextUtils.equals(triggerKey, getString(R.string.craving_history_filter_stress))) {
            chipId = R.id.chipTriggerStress;
        } else if (TextUtils.equals(triggerKey, getString(R.string.craving_history_filter_social))) {
            chipId = R.id.chipTriggerSocial;
        } else if (TextUtils.equals(triggerKey, getString(R.string.craving_history_filter_after_meal))) {
            chipId = R.id.chipTriggerAfterMeal;
        } else if (TextUtils.equals(triggerKey, getString(R.string.craving_history_filter_other))) {
            chipId = R.id.chipTriggerOther;
        } else {
            chipId = R.id.chipTriggerAll;
        }
        if (groupTriggers.getCheckedChipId() != chipId) {
            groupTriggers.check(chipId);
        }
    }

    private void applySmokedState(Integer didSmoke) {
        int chipId;
        if (didSmoke == null) {
            chipId = R.id.chipSmokedAll;
        } else if (didSmoke == 1) {
            chipId = R.id.chipSmokedYes;
        } else {
            chipId = R.id.chipSmokedNo;
        }
        if (groupSmoked.getCheckedChipId() != chipId) {
            groupSmoked.check(chipId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_craving_history, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.craving_history_search_hint));
        CravingHistoryViewModel.FilterState state = viewModel.getFilters().getValue();
        if (state != null && !TextUtils.isEmpty(state.query)) {
            searchItem.expandActionView();
            searchView.setQuery(state.query, false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setQuery(newText);
                return true;
            }
        });
        return true;
    }
}
