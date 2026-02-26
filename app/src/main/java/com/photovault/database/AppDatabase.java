package com.photovault.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.photovault.database.dao.AccountDao;
import com.photovault.database.dao.FaceClusterDao;
import com.photovault.database.dao.MediaItemDao;
import com.photovault.database.dao.RuleDao;
import com.photovault.database.dao.UploadTargetDao;
import com.photovault.database.entity.Account;
import com.photovault.database.entity.FaceCluster;
import com.photovault.database.entity.MediaItem;
import com.photovault.database.entity.QuotaLog;
import com.photovault.database.entity.Rule;
import com.photovault.database.entity.UploadTarget;

/**
 * Main Room database.
 * Version history:
 *   1 → initial schema
 */
@Database(entities = {
        Account.class,
        MediaItem.class,
        UploadTarget.class,
        Rule.class,
        FaceCluster.class,
        QuotaLog.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "photovault.db";

    // ── DAOs ─────────────────────────────────────────────────────────────────

    public abstract AccountDao accountDao();
    public abstract MediaItemDao mediaItemDao();
    public abstract UploadTargetDao uploadTargetDao();
    public abstract RuleDao ruleDao();
    public abstract FaceClusterDao faceClusterDao();

    // ── Singleton ─────────────────────────────────────────────────────────────

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
