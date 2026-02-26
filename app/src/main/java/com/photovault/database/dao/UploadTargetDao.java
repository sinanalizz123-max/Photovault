package com.photovault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.photovault.database.entity.UploadTarget;

import java.util.List;

@Dao
public interface UploadTargetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UploadTarget target);

    @Update
    void update(UploadTarget target);

    @Delete
    void delete(UploadTarget target);

    @Query("SELECT * FROM upload_targets ORDER BY priority_index ASC, created_at ASC")
    LiveData<List<UploadTarget>> getAll();

    @Query("SELECT * FROM upload_targets WHERE status IN (0,1,2) ORDER BY priority_index ASC")
    LiveData<List<UploadTarget>> getActiveQueue();

    @Query("SELECT * FROM upload_targets WHERE status IN (0,1,2) ORDER BY priority_index ASC")
    List<UploadTarget> getActiveQueueSync();

    @Query("SELECT * FROM upload_targets WHERE status = :status ORDER BY priority_index ASC")
    List<UploadTarget> getByStatus(int status);

    @Query("SELECT * FROM upload_targets WHERE account_id = :accountId AND status IN (0,1,2) ORDER BY priority_index ASC LIMIT :limit")
    List<UploadTarget> getNextForAccount(long accountId, int limit);

    @Query("SELECT * FROM upload_targets WHERE media_id = :mediaId")
    List<UploadTarget> getByMediaId(long mediaId);

    @Query("UPDATE upload_targets SET status = :status, progress_percent = :progress WHERE id = :id")
    void updateProgress(long id, int status, int progress);

    @Query("UPDATE upload_targets SET status = :status, error_message = :error, retry_count = retry_count + 1 WHERE id = :id")
    void markFailed(long id, int status, String error);

    @Query("UPDATE upload_targets SET status = :status, completed_at = :completedAt, progress_percent = 100 WHERE id = :id")
    void markDone(long id, int status, long completedAt);

    @Query("UPDATE upload_targets SET priority_index = :index WHERE id = :id")
    void updatePriority(long id, int index);

    @Query("SELECT COUNT(*) FROM upload_targets WHERE status = :status")
    LiveData<Integer> countByStatus(int status);

    @Query("SELECT COUNT(*) FROM upload_targets WHERE status = :status")
    int countByStatusSync(int status);

    @Query("UPDATE upload_targets SET status = 2 WHERE account_id = :accountId AND status IN (0,1)")
    void pauseAccount(long accountId);

    @Query("UPDATE upload_targets SET status = 0 WHERE account_id = :accountId AND status = 2")
    void resumeAccount(long accountId);

    @Query("DELETE FROM upload_targets WHERE status IN (4,5) AND completed_at < :beforeTime")
    void purgeCompleted(long beforeTime);
}
