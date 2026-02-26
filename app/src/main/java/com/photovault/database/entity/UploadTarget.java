package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One upload target = one (media item → account + album) pair.
 * A single photo can have multiple targets (replication).
 */
@Entity(tableName = "upload_targets",
        foreignKeys = {
            @ForeignKey(entity = MediaItem.class,
                        parentColumns = "id",
                        childColumns  = "media_id",
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Account.class,
                        parentColumns = "id",
                        childColumns  = "account_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("media_id"),
            @Index("account_id"),
            @Index("status")
        })
public class UploadTarget {

    // ── Status constants ─────────────────────────────────────────────────────
    public static final int STATUS_WAITING   = 0;
    public static final int STATUS_UPLOADING = 1;
    public static final int STATUS_PAUSED    = 2;
    public static final int STATUS_FAILED    = 3;
    public static final int STATUS_DONE      = 4;
    public static final int STATUS_CANCELED  = 5;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "media_id")
    public long mediaId;

    @ColumnInfo(name = "account_id")
    public long accountId;

    @ColumnInfo(name = "album_name")
    public String albumName;

    /** Google Photos album ID (filled after album is created/found). */
    @ColumnInfo(name = "album_google_id")
    public String albumGoogleId;

    /** Upload token returned by the resumable-upload initiation. */
    @ColumnInfo(name = "upload_token")
    public String uploadToken;

    @ColumnInfo(name = "status")
    public int status = STATUS_WAITING;

    @ColumnInfo(name = "progress_percent")
    public int progressPercent;

    @ColumnInfo(name = "retry_count")
    public int retryCount;

    /** Lower = higher priority (drag-reorder changes this). */
    @ColumnInfo(name = "priority_index")
    public int priorityIndex;

    /** The rule that triggered this target (informational). */
    @ColumnInfo(name = "matched_rule_id")
    public long matchedRuleId = -1;

    /** Human-readable reason for routing (shown in conflict UI). */
    @ColumnInfo(name = "routing_reason")
    public String routingReason;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "completed_at")
    public long completedAt;

    @ColumnInfo(name = "error_message")
    public String errorMessage;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String statusLabel() {
        switch (status) {
            case STATUS_UPLOADING: return "Uploading";
            case STATUS_PAUSED:   return "Paused";
            case STATUS_FAILED:   return "Failed";
            case STATUS_DONE:     return "Done";
            case STATUS_CANCELED: return "Canceled";
            default:              return "Waiting";
        }
    }

    public boolean isActive() {
        return status == STATUS_WAITING || status == STATUS_UPLOADING || status == STATUS_PAUSED;
    }
}
