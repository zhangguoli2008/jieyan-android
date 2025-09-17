package com.quitbuddy.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.quitbuddy.R;

public final class NotificationPermissionHelper {

    private static final int REQUEST_CODE = 1001;

    private NotificationPermissionHelper() {
    }

    public static void requestIfNeeded(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
    }

    public static void handleResult(@NonNull Activity activity, int requestCode, @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.msg_permission_required)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }
}
