package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a connected Gmail / Google account.
 * Tokens are stored separately in EncryptedSharedPreferences;
 * only the email and display data live here.
 */
@Entity(tableName = "accounts")
public class Account {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "profile_pic_url")
    public String profilePicUrl;

    /** Colour index 0-4 used to pick a colour from the palette. */
    @ColumnInfo(name = "color_index")
    public int colorIndex;

    /** Bytes used in Google Photos storage. Updated periodically. */
    @ColumnInfo(name = "storage_used_bytes")
    public long storageUsedBytes;

    /** Google Photos storage quota in bytes (default 15 GB). */
    @ColumnInfo(name = "storage_quota_bytes")
    public long storageQuotaBytes = 15_000_000_000L;

    /** API calls made today. Reset at midnight. */
    @ColumnInfo(name = "api_calls_today")
    public int apiCallsToday;

    /** Unix ms when apiCallsToday was last reset. */
    @ColumnInfo(name = "api_quota_reset_time")
    public long apiQuotaResetTime;

    /** Is this account currently paused due to a 429? */
    @ColumnInfo(name = "is_paused")
    public boolean isPaused;

    /** Unix ms when we should next attempt after a 429. */
    @ColumnInfo(name = "resume_at")
    public long resumeAt;

    @ColumnInfo(name = "added_at")
    public long addedAt;

    @ColumnInfo(name = "is_active")
    public boolean isActive = true;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public float getStoragePercent() {
        if (storageQuotaBytes == 0) return 0f;
        return (float) storageUsedBytes / storageQuotaBytes;
    }

    public boolean isQuotaExhausted() {
        return apiCallsToday >= 9000;
    }
}
