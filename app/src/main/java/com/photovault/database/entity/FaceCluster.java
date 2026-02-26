package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A locally-computed face cluster.
 * Face embeddings are stored in face_photo_map; the cluster is the identity node.
 */
@Entity(tableName = "face_clusters",
        indices = {@Index("person_name")})
public class FaceCluster {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Null means "Unknown / Unnamed" */
    @ColumnInfo(name = "person_name")
    public String personName;

    /** Number of photos in this cluster. */
    @ColumnInfo(name = "photo_count")
    public int photoCount;

    /** Average embedding stored as comma-separated floats. */
    @ColumnInfo(name = "representative_embedding")
    public String representativeEmbedding;

    /** Local path of the best representative photo (for avatar display). */
    @ColumnInfo(name = "cover_photo_path")
    public String coverPhotoPath;

    /** Has the user confirmed this cluster's identity? */
    @ColumnInfo(name = "is_confirmed")
    public boolean isConfirmed;

    /** Confidence 0.0–1.0 of the clustering. Low → prompt user to confirm. */
    @ColumnInfo(name = "confidence")
    public float confidence;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isNamed() {
        return personName != null && !personName.isEmpty();
    }

    public boolean isLowConfidence() {
        return confidence < 0.75f;
    }

    public String displayName() {
        return isNamed() ? personName : "Unknown";
    }

    public String initials() {
        if (!isNamed()) return "?";
        String[] parts = personName.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}
