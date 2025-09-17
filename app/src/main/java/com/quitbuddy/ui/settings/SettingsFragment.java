package com.quitbuddy.ui.settings;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.quitbuddy.R;
import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.notifications.SyncScheduler;
import com.quitbuddy.ui.ThemeManager;
import com.quitbuddy.ui.export.ExportDataActivity;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private QuitBuddyRepository repository;
    private Preference quitModePreference;
    private Preference notificationTimePreference;
    private String currentReminderTime;
    private boolean hasPlan;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        repository = QuitBuddyRepository.getInstance(requireContext());

        quitModePreference = findPreference("pref_quit_mode");
        notificationTimePreference = findPreference("pref_notification_time");

        ListPreference theme = findPreference("pref_theme");
        ListPreference accent = findPreference("pref_accent");
        SwitchPreferenceCompat notifications = findPreference("pref_notifications_enabled");
        Preference export = findPreference("pref_export");
        Preference clear = findPreference("pref_clear");
        Preference privacy = findPreference("pref_privacy");
        Preference support = findPreference("pref_support");

        if (theme != null) {
            theme.setOnPreferenceChangeListener(this);
        }
        if (accent != null) {
            accent.setOnPreferenceChangeListener(this);
        }
        if (notifications != null) {
            notifications.setOnPreferenceChangeListener(this);
        }
        if (notificationTimePreference != null) {
            notificationTimePreference.setOnPreferenceClickListener(this);
        }
        if (export != null) {
            export.setOnPreferenceClickListener(this);
        }
        if (clear != null) {
            clear.setOnPreferenceClickListener(this);
        }
        if (privacy != null) {
            privacy.setOnPreferenceClickListener(this);
        }
        if (support != null) {
            support.setOnPreferenceClickListener(this);
        }

        observePlan();
    }

    private void observePlan() {
        repository.observePlan().observe(getViewLifecycleOwner(), plan -> {
            hasPlan = plan != null;
            if (quitModePreference != null) {
                if (plan == null || TextUtils.isEmpty(plan.mode)) {
                    quitModePreference.setSummary(R.string.settings_quit_mode_empty);
                } else {
                    quitModePreference.setSummary(getModeLabel(plan.mode));
                }
            }
            if (plan != null && plan.reminderTimes != null && !plan.reminderTimes.isEmpty()) {
                currentReminderTime = plan.reminderTimes.get(0);
            } else {
                currentReminderTime = null;
            }
            if (notificationTimePreference != null) {
                notificationTimePreference.setEnabled(hasPlan);
            }
            updateReminderSummary();
        });
    }

    private void updateReminderSummary() {
        if (notificationTimePreference != null) {
            String display = currentReminderTime == null ? "--:--" : currentReminderTime;
            notificationTimePreference.setSummary(getString(R.string.settings_notifications_time_summary, display));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("pref_theme".equals(preference.getKey())) {
            ThemeManager.applyTheme(requireContext());
            requireActivity().recreate();
            return true;
        } else if ("pref_accent".equals(preference.getKey())) {
            requireActivity().recreate();
            return true;
        } else if ("pref_notifications_enabled".equals(preference.getKey())) {
            boolean enabled = Boolean.parseBoolean(String.valueOf(newValue));
            if (enabled) {
                SyncScheduler.scheduleDailyReminders(requireContext());
            } else {
                SyncScheduler.cancelDailyReminders(requireContext());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if ("pref_export".equals(key)) {
            startActivity(new Intent(requireContext(), ExportDataActivity.class));
            return true;
        } else if ("pref_notification_time".equals(key)) {
            showTimePicker();
            return true;
        } else if ("pref_clear".equals(key)) {
            confirmClear();
            return true;
        } else if ("pref_privacy".equals(key)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.settings_privacy_url)));
            startActivity(intent);
            return true;
        } else if ("pref_support".equals(key)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.settings_support_email)));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void showTimePicker() {
        if (!hasPlan) {
            Toast.makeText(requireContext(), R.string.settings_quit_mode_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(currentReminderTime)) {
            String[] parts = currentReminderTime.split(":");
            if (parts.length == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            }
        }
        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            repository.updateReminderTime(0, time, () -> {
                currentReminderTime = time;
                updateReminderSummary();
                Toast.makeText(requireContext(), getString(R.string.settings_notifications_time_summary, time), Toast.LENGTH_SHORT).show();
            });
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_data_clear_title)
                .setMessage(R.string.settings_clear_confirm)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> repository.clearCravings(() ->
                        Toast.makeText(requireContext(), R.string.settings_clear_success, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private String getModeLabel(String mode) {
        if (QuitPlanEntity.MODE_GRADUAL.equals(mode)) {
            return getString(R.string.onboarding_mode_gradual);
        }
        return getString(R.string.onboarding_mode_cold_turkey);
    }
}
