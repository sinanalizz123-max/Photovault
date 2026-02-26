package com.photovault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.photovault.database.entity.MediaItem;

import java.util.List;

@Dao
public interface MediaItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(MediaItem item);

    @Update
    void update(MediaItem item);

    @Delete
    void delete(MediaItem item);

    @Query("SELECT * FROM media_items ORDER BY created_at DESC")
    LiveData<List<MediaItem>> getAll();

    @Query("SELECT * FROM media_items ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<MediaItem> getPage(int limit, int offset);

    @Query("SELECT * FROM media_items WHERE folder_name = :folder ORDER BY created_at DESC")
    LiveData<List<MediaItem>> getByFolder(String folder);

    @Query("SELECT * FROM media_items WHERE face_cluster_id = :clusterId ORDER BY created_at DESC")
    LiveData<List<MediaItem>> getByFaceCluster(long clusterId);

    @Query("SELECT * FROM media_items WHERE manual_tag = :tag ORDER BY created_at DESC")
    List<MediaItem> getByTag(String tag);

    @Query("SELECT * FROM media_items WHERE media_type = :type ORDER BY created_at DESC")
    List<MediaItem> getByMediaType(int type);

    @Query("SELECT * FROM media_items WHERE local_path = :path LIMIT 1")
    MediaItem getByPath(String path);

    @Query("SELECT * FROM media_items WHERE perceptual_hash = :hash")
    List<MediaItem> getByHash(String hash);

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    MediaItem getByIdSync(long id);

    @Query("UPDATE media_items SET face_cluster_id = :clusterId WHERE id = :id")
    void updateFaceCluster(long id, long clusterId);

    @Query("UPDATE media_items SET manual_tag = :tag WHERE id = :id")
    void updateTag(long id, String tag);

    @Query("SELECT DISTINCT folder_name FROM media_items WHERE folder_name IS NOT NULL ORDER BY folder_name ASC")
    LiveData<List<String>> getAllFolders();

    @Query("SELECT COUNT(*) FROM media_items")
    int getTotalCount();
}
