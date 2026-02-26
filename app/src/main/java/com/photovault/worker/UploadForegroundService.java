package com.photovault.worker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Foreground service declaration required by the manifest.
 * WorkManager handles the actual foreground promotion via ForegroundInfo.
 */
public class UploadForegroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
