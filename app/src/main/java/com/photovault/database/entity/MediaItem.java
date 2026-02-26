package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a single photo or video on the device.
 * Inserted when MediaStore picks up a new file.
 */
@Entity(tableName = "media_items",
        indices = {@Index("local_path"), @Index("face_cluster_id"), @Index("folder_name")})
public class MediaItem {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_GIF   = 2;
    public static final int TYPE_RAW   = 3;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "local_path")
    public String localPath;

    @ColumnInfo(name = "media_store_id")
    public long mediaStoreId;

    @ColumnInfo(name = "media_type")
    public int mediaType;   // TYPE_* constant

    @ColumnInfo(name = "folder_name")
    public String folderName;

    @ColumnInfo(name = "file_size_bytes")
    public long fileSizeBytes;

    @ColumnInfo(name = "width")
    public int width;

    @ColumnInfo(name = "height")
    public int height;

    /** Duration in ms (videos only). */
    @ColumnInfo(name = "duration_ms")
    public long durationMs;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "modified_at")
    public long modifiedAt;

    /** Latitude from EXIF/MediaStore, or 0 if unavailable. */
    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    /** FK to face_clusters.id; -1 if no face detected. */
    @ColumnInfo(name = "face_cluster_id")
    public long faceClusterId = -1;

    /** User-assigned tag, e.g. "Work", "Travel". */
    @ColumnInfo(name = "manual_tag")
    public String manualTag;

    /** Perceptual hash for deduplication. */
    @ColumnInfo(name = "perceptual_hash")
    public String perceptualHash;

    @ColumnInfo(name = "device_model")
    public String deviceModel;

    @ColumnInfo(name = "inserted_at")
    public long insertedAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isImage() { return mediaType == TYPE_IMAGE || mediaType == TYPE_RAW; }
    public boolean isVideo() { return mediaType == TYPE_VIDEO; }

    public String mediaTypeLabel() {
        switch (mediaType) {
            case TYPE_VIDEO: return "video";
            case TYPE_GIF:   return "gif";
            case TYPE_RAW:   return "raw";
            default:         return "image";
        }
    }
}
