package com.photovault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.photovault.database.entity.Account;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY added_at ASC")
    LiveData<List<Account>> getAllActive();

    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY added_at ASC")
    List<Account> getAllActiveSync();

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    LiveData<Account> getById(long id);

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    Account getByIdSync(long id);

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    Account getByEmailSync(String email);

    @Query("UPDATE accounts SET api_calls_today = api_calls_today + :count WHERE id = :id")
    void incrementApiCalls(long id, int count);

    @Query("UPDATE accounts SET api_calls_today = 0, api_quota_reset_time = :resetTime WHERE id = :id")
    void resetDailyQuota(long id, long resetTime);

    @Query("UPDATE accounts SET is_paused = :paused, resume_at = :resumeAt WHERE id = :id")
    void setPaused(long id, boolean paused, long resumeAt);

    @Query("UPDATE accounts SET storage_used_bytes = :bytes WHERE id = :id")
    void updateStorageUsed(long id, long bytes);

    @Query("SELECT COUNT(*) FROM accounts WHERE is_active = 1")
    int getActiveCount();
}
