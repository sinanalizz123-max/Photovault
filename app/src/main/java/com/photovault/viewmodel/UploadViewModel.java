package com.photovault.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.photovault.PhotoVaultApp;
import com.photovault.database.AppDatabase;
import com.photovault.database.entity.Account;
import com.photovault.database.entity.Rule;
import com.photovault.database.entity.UploadTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for upload queue management and conflict resolution.
 */
public class UploadViewModel extends AndroidViewModel {

    public enum ConflictResolution { SELECTED, PRIORITY, ALL, CANCEL }

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public UploadViewModel(Application app) {
        super(app);
        db = PhotoVaultApp.getInstance().getDatabase();
    }

    // ── Queue ─────────────────────────────────────────────────────────────────

    public LiveData<List<UploadTarget>> getActiveQueue() {
        return db.uploadTargetDao().getActiveQueue();
    }

    public LiveData<Integer> getUploadingCount() {
        return db.uploadTargetDao().countByStatus(UploadTarget.STATUS_UPLOADING);
    }

    public void cancelTarget(long targetId) {
        executor.execute(() ->
                db.uploadTargetDao().updateProgress(
                        targetId, UploadTarget.STATUS_CANCELED, 0));
    }

    public void retryTarget(long targetId) {
        executor.execute(() ->
                db.uploadTargetDao().updateProgress(
                        targetId, UploadTarget.STATUS_WAITING, 0));
    }

    public void pauseAll() {
        executor.execute(() -> {
            List<UploadTarget> active = db.uploadTargetDao().getByStatus(
                    UploadTarget.STATUS_UPLOADING);
            for (UploadTarget t : active) {
                db.uploadTargetDao().updateProgress(t.id, UploadTarget.STATUS_PAUSED, t.progressPercent);
            }
        });
    }

    public void resumeAll() {
        executor.execute(() -> {
            List<UploadTarget> paused = db.uploadTargetDao().getByStatus(
                    UploadTarget.STATUS_PAUSED);
            for (UploadTarget t : paused) {
                db.uploadTargetDao().updateProgress(t.id, UploadTarget.STATUS_WAITING, t.progressPercent);
            }
        });
    }

    public void reorderQueue(List<UploadTarget> ordered) {
        executor.execute(() -> {
            for (int i = 0; i < ordered.size(); i++) {
                db.uploadTargetDao().updatePriority(ordered.get(i).id, i);
            }
        });
    }

    // ── Conflict resolution ───────────────────────────────────────────────────

    /**
     * Returns the rules that matched for a given media item (conflict situation).
     * In production this would be stored when routing detects multiple matches.
     */
    public LiveData<List<Rule>> getConflictRules(long mediaId) {
        MutableLiveData<List<Rule>> result = new MutableLiveData<>();
        executor.execute(() -> {
            // Stub — in production: query a conflicts table
            result.postValue(new ArrayList<>());
        });
        return result;
    }

    public LiveData<MediaInfo> getMediaInfo(long mediaId) {
        MutableLiveData<MediaInfo> result = new MutableLiveData<>();
        executor.execute(() -> {
            var item = db.mediaItemDao().getByIdSync(mediaId);
            if (item == null) { result.postValue(null); return; }
            MediaInfo info = new MediaInfo();
            info.filename  = item.localPath.substring(item.localPath.lastIndexOf('/') + 1);
            info.sizeLabel = formatSize(item.fileSizeBytes);
            result.postValue(info);
        });
        return result;
    }

    public void resolveConflict(long mediaId,
                                Set<Long> selectedRuleIds,
                                ConflictResolution resolution) {
        executor.execute(() -> {
            List<UploadTarget> targets = db.uploadTargetDao().getByMediaId(mediaId);
            switch (resolution) {
                case SELECTED:
                    for (UploadTarget t : targets) {
                        if (selectedRuleIds == null ||
                                !selectedRuleIds.contains(t.matchedRuleId)) {
                            db.uploadTargetDao().updateProgress(
                                    t.id, UploadTarget.STATUS_CANCELED, 0);
                        } else {
                            db.uploadTargetDao().updateProgress(
                                    t.id, UploadTarget.STATUS_WAITING, 0);
                        }
                    }
                    break;
                case PRIORITY:
                    // Keep only the highest-priority target
                    if (!targets.isEmpty()) {
                        UploadTarget keep = targets.get(0); // already sorted by priority
                        db.uploadTargetDao().updateProgress(
                                keep.id, UploadTarget.STATUS_WAITING, 0);
                        for (int i = 1; i < targets.size(); i++) {
                            db.uploadTargetDao().updateProgress(
                                    targets.get(i).id, UploadTarget.STATUS_CANCELED, 0);
                        }
                    }
                    break;
                case ALL:
                    for (UploadTarget t : targets) {
                        db.uploadTargetDao().updateProgress(
                                t.id, UploadTarget.STATUS_WAITING, 0);
                    }
                    break;
                case CANCEL:
                    cancelMediaUpload(mediaId);
                    break;
            }
        });
    }

    public void cancelMediaUpload(long mediaId) {
        executor.execute(() -> {
            List<UploadTarget> targets = db.uploadTargetDao().getByMediaId(mediaId);
            for (UploadTarget t : targets) {
                db.uploadTargetDao().updateProgress(t.id, UploadTarget.STATUS_CANCELED, 0);
            }
        });
    }

    public void setAlwaysFollowPriority(boolean always) {
        // Persist in SharedPreferences
        PhotoVaultApp.getInstance()
                .getSharedPreferences("photovault_settings", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("conflict_behavior", "priority")
                .apply();
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    public LiveData<List<Account>> getAccounts() {
        return db.accountDao().getAllActive();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // ── Data class ────────────────────────────────────────────────────────────

    public static class MediaInfo {
        public String filename;
        public String sizeLabel;
    }
}
