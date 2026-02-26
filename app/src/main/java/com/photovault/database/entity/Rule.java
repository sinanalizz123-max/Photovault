package com.photovault.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A single routing rule.
 * Rules are evaluated in priority_order (ascending) for every new media item.
 */
@Entity(tableName = "rules",
        foreignKeys = {
            @ForeignKey(entity = Account.class,
                        parentColumns = "id",
                        childColumns  = "account_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("account_id"), @Index("rule_type"), @Index("priority_order")})
public class Rule {

    // ── Rule type constants ──────────────────────────────────────────────────
    public static final int TYPE_MANUAL_TAG   = 0;
    public static final int TYPE_FACE         = 1;
    public static final int TYPE_FOLDER       = 2;
    public static final int TYPE_MEDIA_TYPE   = 3;
    public static final int TYPE_DEVICE       = 4;
    public static final int TYPE_SIZE         = 5;
    public static final int TYPE_RESOLUTION   = 6;
    public static final int TYPE_DATE         = 7;
    public static final int TYPE_APP_SOURCE   = 8;
    public static final int TYPE_LOCATION     = 9;
    public static final int TYPE_DEFAULT      = 10;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "rule_type")
    public int ruleType;

    @ColumnInfo(name = "rule_name")
    public String ruleName;

    /**
     * Condition value. Interpretation depends on ruleType:
     *  FACE         → face cluster id (String of long)
     *  FOLDER       → folder path
     *  MEDIA_TYPE   → "image" | "video" | "gif" | "raw"
     *  SIZE         → threshold bytes (String of long)
     *  RESOLUTION   → threshold pixels e.g. "3840"
     *  DATE         → ISO date string
     *  MANUAL_TAG   → tag string
     *  LOCATION     → "lat,lng,radiusMeters"
     *  DEVICE       → device model string
     *  APP_SOURCE   → folder path signature
     *  DEFAULT      → "" (empty)
     */
    @ColumnInfo(name = "condition_value")
    public String conditionValue;

    @ColumnInfo(name = "account_id")
    public long accountId;

    @ColumnInfo(name = "album_name")
    public String albumName;

    /** Comma-separated list of additional accountIds to replicate to. */
    @ColumnInfo(name = "replicate_to_account_ids")
    public String replicateToAccountIds;

    /** Lower = evaluated earlier. */
    @ColumnInfo(name = "priority_order")
    public int priorityOrder;

    @ColumnInfo(name = "is_enabled")
    public boolean isEnabled = true;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String ruleTypeLabel() {
        switch (ruleType) {
            case TYPE_MANUAL_TAG:  return "Manual Tag";
            case TYPE_FACE:        return "Face";
            case TYPE_FOLDER:      return "Folder";
            case TYPE_MEDIA_TYPE:  return "Media Type";
            case TYPE_DEVICE:      return "Device";
            case TYPE_SIZE:        return "Size";
            case TYPE_RESOLUTION:  return "Resolution";
            case TYPE_DATE:        return "Date";
            case TYPE_APP_SOURCE:  return "App Source";
            case TYPE_LOCATION:    return "Location";
            default:               return "Default";
        }
    }

    public String ruleTypeEmoji() {
        switch (ruleType) {
            case TYPE_MANUAL_TAG:  return "🏷️";
            case TYPE_FACE:        return "👤";
            case TYPE_FOLDER:      return "📁";
            case TYPE_MEDIA_TYPE:  return "🎬";
            case TYPE_DEVICE:      return "📱";
            case TYPE_SIZE:        return "📦";
            case TYPE_RESOLUTION:  return "📐";
            case TYPE_DATE:        return "📅";
            case TYPE_APP_SOURCE:  return "📲";
            case TYPE_LOCATION:    return "📍";
            default:               return "⚙️";
        }
    }

    public boolean hasReplication() {
        return replicateToAccountIds != null && !replicateToAccountIds.isEmpty();
    }
}
