package com.quitbuddy.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quitbuddy.R;
import com.quitbuddy.notifications.SyncScheduler;
import com.quitbuddy.ui.ThemeManager;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference sync = findPreference("pref_sync");
        Preference theme = findPreference("pref_theme");
        Preference export = findPreference("pref_export");
        if (sync != null) {
            sync.setOnPreferenceChangeListener(this);
        }
        if (theme instanceof ListPreference) {
            theme.setOnPreferenceChangeListener(this);
        }
        if (export != null) {
            export.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("pref_sync".equals(preference.getKey())) {
            boolean enabled = Boolean.parseBoolean(String.valueOf(newValue));
            SyncScheduler.toggleCloudSync(requireContext(), enabled);
            return true;
        } else if ("pref_theme".equals(preference.getKey())) {
            ThemeManager.applyTheme(requireContext());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("pref_export".equals(preference.getKey())) {
            startActivity(new Intent(requireContext(), com.quitbuddy.ui.export.ExportDataActivity.class));
            return true;
        }
        return false;
    }
}
