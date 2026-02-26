package com.photovault.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives media scanner broadcasts to trigger auto-backup.
 */
public class MediaStoreReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        var prefs = context.getSharedPreferences("photovault_settings", Context.MODE_PRIVATE);
        if (prefs.getBoolean("auto_backup", true)) {
            BootReceiver.scheduleUploadWorker(context);
        }
    }
}
