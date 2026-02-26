package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Tracks individual API calls per account for daily quota management.
 */
@Entity(tableName = "quota_log",
        foreignKeys = {
            @ForeignKey(entity = Account.class,
                        parentColumns = "id",
                        childColumns  = "account_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("account_id"), @Index("timestamp")})
public class QuotaLog {

    public static final int CALL_UPLOAD_INIT   = 0;
    public static final int CALL_MEDIA_CREATE  = 1;
    public static final int CALL_ALBUM_ADD     = 2;
    public static final int CALL_ALBUM_CREATE  = 3;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "account_id")
    public long accountId;

    @ColumnInfo(name = "call_type")
    public int callType;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "success")
    public boolean success;

    @ColumnInfo(name = "http_code")
    public int httpCode;
}
