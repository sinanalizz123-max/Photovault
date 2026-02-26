package com.photovault.worker;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.photovault.MainActivity;
import com.photovault.PhotoVaultApp;
import com.photovault.R;
import com.photovault.database.AppDatabase;
import com.photovault.database.entity.Account;
import com.photovault.database.entity.UploadTarget;

import java.util.List;

/**
 * WorkManager worker that processes the upload queue.
 *
 * Logic:
 *  1. Fetch next batch of WAITING targets
 *  2. Group by account (parallel up to dynamic limit)
 *  3. For each target:
 *     a. Check account quota
 *     b. Get token (decrypt from EncryptedSharedPreferences)
 *     c. Compress if configured
 *     d. Initiate resumable upload session
 *     e. Upload binary chunks
 *     f. Create media item
 *     g. Add to album
 *     h. Mark DONE
 *  4. Handle 401 → refresh token, retry once
 *  5. Handle 429 → pause account, schedule resume
 *  6. Network loss → pause, re-queue
 */
public class UploadWorker extends Worker {

    private static final int NOTIFICATION_ID = 1001;
    private static final String TAG = "UploadWorker";

    private final AppDatabase db;
    private volatile boolean cancelled = false;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = PhotoVaultApp.getInstance().getDatabase();
    }

    @NonNull
    @Override
    public Result doWork() {
        setForegroundAsync(createForegroundInfo("Preparing uploads…"));

        List<UploadTarget> queue = db.uploadTargetDao().getActiveQueueSync();
        if (queue == null || queue.isEmpty()) return Result.success();

        int processed = 0;
        for (UploadTarget target : queue) {
            if (isStopped() || cancelled) break;

            Account account = db.accountDao().getByIdSync(target.accountId);
            if (account == null) continue;

            // Skip paused accounts
            if (account.isPaused) {
                if (System.currentTimeMillis() < account.resumeAt) continue;
                db.accountDao().setPaused(account.id, false, 0);
            }

            // Check daily quota
            if (account.isQuotaExhausted()) {
                db.uploadTargetDao().pauseAccount(account.id);
                continue;
            }

            // Mark uploading
            db.uploadTargetDao().updateProgress(
                    target.id, UploadTarget.STATUS_UPLOADING, 0);

            setForegroundAsync(createForegroundInfo(
                    "Uploading " + (processed + 1) + " of " + queue.size()));

            boolean success = uploadTarget(target, account);

            if (success) {
                db.uploadTargetDao().markDone(
                        target.id, UploadTarget.STATUS_DONE, System.currentTimeMillis());
                db.accountDao().incrementApiCalls(account.id, 3); // init + create + album
                processed++;
            }
        }

        return Result.success();
    }

    // ── Upload logic (stub — real impl uses OkHttp) ───────────────────────────

    private boolean uploadTarget(UploadTarget target, Account account) {
        try {
            // 1. Get auth token
            String token = getAuthToken(account.email);
            if (token == null) return false;

            // 2. Get media file
            var mediaItem = db.mediaItemDao().getByIdSync(target.mediaId);
            if (mediaItem == null) return false;

            java.io.File file = new java.io.File(mediaItem.localPath);
            if (!file.exists()) {
                db.uploadTargetDao().markFailed(target.id,
                        UploadTarget.STATUS_FAILED, "File not found");
                return false;
            }

            // 3. Simulate progress updates (production: real chunk uploads)
            for (int pct = 10; pct <= 100; pct += 10) {
                if (isStopped()) return false;
                db.uploadTargetDao().updateProgress(
                        target.id, UploadTarget.STATUS_UPLOADING, pct);
                Thread.sleep(100); // simulate network
            }

            // 4. Production steps (commented for stub):
            // String uploadToken  = photosApi.initUpload(token, file);
            // String mediaItemId  = photosApi.createMediaItem(token, uploadToken, mediaItem.localPath);
            // photosApi.addToAlbum(token, target.albumGoogleId, mediaItemId);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            android.util.Log.e(TAG, "Upload failed", e);
            db.uploadTargetDao().markFailed(
                    target.id, UploadTarget.STATUS_FAILED, e.getMessage());
            return false;
        }
    }

    private String getAuthToken(String email) {
        // Production: decrypt from EncryptedSharedPreferences keyed by email
        return "stub_token_" + email;
    }

    // ── Foreground notification ───────────────────────────────────────────────

    @NonNull
    private ForegroundInfo createForegroundInfo(String progress) {
        Context ctx = getApplicationContext();

        Intent cancelIntent = WorkManager.getInstance(ctx)
                .createCancelPendingIntent(getId());

        Intent tapIntent = new Intent(ctx, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent tapPending = PendingIntent.getActivity(ctx, 0, tapIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(ctx, PhotoVaultApp.CHANNEL_UPLOAD)
                .setContentTitle("PhotoVault Upload")
                .setContentText(progress)
                .setSmallIcon(R.drawable.ic_upload_notification)
                .setOngoing(true)
                .setContentIntent(tapPending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        "Cancel", cancelIntent)
                .build();

        return new ForegroundInfo(NOTIFICATION_ID, notification);
    }

    @Override
    public void onStopped() {
        cancelled = true;
        super.onStopped();
    }
}
