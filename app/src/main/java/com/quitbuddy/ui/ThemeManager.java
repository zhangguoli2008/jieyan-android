package com.quitbuddy.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.quitbuddy.R;

public final class ThemeManager {

    private static final String KEY_THEME = "pref_theme";
    private static final String KEY_ACCENT = "pref_accent";

    private ThemeManager() {
    }

    public static void applyTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = prefs.getString(KEY_THEME, "system");
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static int resolveAccentColor(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String accentValue = prefs.getString(KEY_ACCENT, "teal");
        switch (accentValue) {
            case "sunrise":
                return context.getColor(R.color.accent_sunrise);
            case "ocean":
                return context.getColor(R.color.accent_ocean);
            default:
                return context.getColor(R.color.accent_teal);
        }
    }

    public static void tintToolbar(MaterialToolbar toolbar) {
        int color = resolveAccentColor(toolbar.getContext());
        toolbar.setBackgroundColor(color);
        int onColor = ColorUtils.calculateLuminance(color) > 0.5 ? Color.BLACK : Color.WHITE;
        toolbar.setTitleTextColor(onColor);
        toolbar.setSubtitleTextColor(onColor);
        toolbar.setNavigationIconTint(onColor);
    }
}
