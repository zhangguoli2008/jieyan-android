package com.quitbuddy.ui.craving;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.preference.PreferenceManager;

import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.util.List;
import java.util.Objects;

public class CravingHistoryViewModel extends AndroidViewModel {

    private static final String KEY_TRIGGER = "history_trigger";
    private static final String KEY_SMOKED = "history_smoked";
    private static final String KEY_QUERY = "history_query";

    private final QuitBuddyRepository repository;
    private final SharedPreferences prefs;
    private final MutableLiveData<FilterState> filters = new MutableLiveData<>();
    private final LiveData<List<CravingEventEntity>> cravings;

    public CravingHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = QuitBuddyRepository.getInstance(application);
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        FilterState initial = new FilterState(
                prefs.getString(KEY_TRIGGER, "all"),
                prefs.contains(KEY_SMOKED) ? mapSmoked(prefs.getInt(KEY_SMOKED, -1)) : null,
                prefs.getString(KEY_QUERY, ""));
        filters.setValue(initial);
        cravings = Transformations.switchMap(filters, state -> repository.observeFilteredCravings(
                normalizeTrigger(state.triggerKey),
                state.didSmoke,
                normalizeQuery(state.query)));
    }

    public LiveData<List<CravingEventEntity>> getCravings() {
        return cravings;
    }

    public LiveData<FilterState> getFilters() {
        return filters;
    }

    public void setTrigger(String triggerKey) {
        FilterState current = filters.getValue();
        String normalized = triggerKey == null ? "all" : triggerKey;
        if (current != null && Objects.equals(current.triggerKey, normalized)) {
            return;
        }
        persistTrigger(normalized);
        updateState(new FilterState(normalized, current == null ? null : current.didSmoke,
                current == null ? "" : current.query));
    }

    public void setDidSmoke(Integer didSmoke) {
        FilterState current = filters.getValue();
        if (current != null && Objects.equals(current.didSmoke, didSmoke)) {
            return;
        }
        persistDidSmoke(didSmoke);
        updateState(new FilterState(current == null ? "all" : current.triggerKey, didSmoke,
                current == null ? "" : current.query));
    }

    public void setQuery(String query) {
        FilterState current = filters.getValue();
        String trimmed = query == null ? "" : query.trim();
        if (current != null && Objects.equals(current.query, trimmed)) {
            return;
        }
        persistQuery(trimmed);
        updateState(new FilterState(current == null ? "all" : current.triggerKey,
                current == null ? null : current.didSmoke, trimmed));
    }

    private void updateState(FilterState newState) {
        filters.setValue(newState);
    }

    private void persistTrigger(String triggerKey) {
        prefs.edit().putString(KEY_TRIGGER, triggerKey).apply();
    }

    private void persistDidSmoke(Integer didSmoke) {
        if (didSmoke == null) {
            prefs.edit().remove(KEY_SMOKED).apply();
        } else {
            prefs.edit().putInt(KEY_SMOKED, didSmoke).apply();
        }
    }

    private void persistQuery(String query) {
        prefs.edit().putString(KEY_QUERY, query).apply();
    }

    private static String normalizeTrigger(String triggerKey) {
        if (triggerKey == null || "all".equals(triggerKey)) {
            return null;
        }
        return triggerKey;
    }

    private static String normalizeQuery(String query) {
        return query == null || query.isEmpty() ? null : query;
    }

    private static Integer mapSmoked(int value) {
        if (value < 0) {
            return null;
        }
        return value;
    }

    public static class FilterState {
        public final String triggerKey;
        public final Integer didSmoke;
        public final String query;

        public FilterState(String triggerKey, Integer didSmoke, String query) {
            this.triggerKey = triggerKey;
            this.didSmoke = didSmoke;
            this.query = query;
        }
    }
}
