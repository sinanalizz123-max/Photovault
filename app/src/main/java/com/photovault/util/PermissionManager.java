package com.photovault.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.photovault.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * PermissionManager — centralised runtime permission handling.
 *
 * Supports:
 *  - Media permissions (images/video) for API 26–33+
 *  - Location permission (for location routing rules)
 *  - Notification permission (API 33+)
 *  - Background execution exemption
 */
public class PermissionManager {

    // ── Permission groups ────────────────────────────────────────────────────

    public static String[] getMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            // Android 8–12
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }

    public static String[] getLocationPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    public static String[] getNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.POST_NOTIFICATIONS};
        }
        return new String[]{};
    }

    // ── Check helpers ────────────────────────────────────────────────────────

    public static boolean hasMediaPermissions(Context ctx) {
        for (String p : getMediaPermissions()) {
            if (ContextCompat.checkSelfPermission(ctx, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasLocationPermission(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasNotificationPermission(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // ── Denied permissions list ──────────────────────────────────────────────

    public static String[] getDeniedPermissions(Context ctx, String[] permissions) {
        List<String> denied = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(ctx, p) != PackageManager.PERMISSION_GRANTED) {
                denied.add(p);
            }
        }
        return denied.toArray(new String[0]);
    }

    // ── Rational dialog ──────────────────────────────────────────────────────

    public static void showRationaleDialog(Context ctx,
                                            String title,
                                            String message,
                                            Runnable onGrant,
                                            Runnable onDeny) {
        new MaterialAlertDialogBuilder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant Permission", (d, w) -> onGrant.run())
                .setNegativeButton("Not Now", (d, w) -> onDeny.run())
                .setCancelable(false)
                .show();
    }

    // ── All required permissions ─────────────────────────────────────────────

    public static String[] getAllRequiredPermissions() {
        List<String> all = new ArrayList<>();
        for (String p : getMediaPermissions()) all.add(p);
        for (String p : getNotificationPermissions()) all.add(p);
        return all.toArray(new String[0]);
    }
}
