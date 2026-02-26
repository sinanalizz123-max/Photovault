package com.photovault.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * ThemeManager — handles dark/light/system theme persistence and application.
 *
 * Modes:
 *   0 = Follow system default
 *   1 = Force Light
 *   2 = Force Dark  (default for PhotoVault)
 */
public class ThemeManager {

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT  = 1;
    public static final int MODE_DARK   = 2;

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME  = "theme_mode";

    // ── Apply saved theme (call from Application.onCreate) ───────────────────

    public static void applyTheme(Context context) {
        int mode = getSavedMode(context);
        applyMode(mode);
    }

    // ── Save + immediately apply ─────────────────────────────────────────────

    public static void setTheme(Context context, int mode) {
        getPrefs(context).edit().putInt(KEY_THEME, mode).apply();
        applyMode(mode);
    }

    // ── Current saved mode ───────────────────────────────────────────────────

    public static int getSavedMode(Context context) {
        // Default: dark mode (matches the app's aesthetic)
        return getPrefs(context).getInt(KEY_THEME, MODE_DARK);
    }

    public static boolean isDark(Context context) {
        return getSavedMode(context) == MODE_DARK;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private static void applyMode(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
