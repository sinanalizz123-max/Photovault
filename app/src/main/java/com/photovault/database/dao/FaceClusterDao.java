package com.photovault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.photovault.database.entity.FaceCluster;

import java.util.List;

@Dao
public interface FaceClusterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FaceCluster cluster);

    @Update
    void update(FaceCluster cluster);

    @Delete
    void delete(FaceCluster cluster);

    @Query("SELECT * FROM face_clusters ORDER BY photo_count DESC")
    LiveData<List<FaceCluster>> getAll();

    @Query("SELECT * FROM face_clusters WHERE person_name IS NOT NULL AND person_name != '' ORDER BY photo_count DESC")
    LiveData<List<FaceCluster>> getNamed();

    @Query("SELECT * FROM face_clusters WHERE (person_name IS NULL OR person_name = '') ORDER BY photo_count DESC")
    LiveData<List<FaceCluster>> getUnnamed();

    @Query("SELECT * FROM face_clusters WHERE id = :id LIMIT 1")
    FaceCluster getByIdSync(long id);

    @Query("SELECT * FROM face_clusters WHERE is_confirmed = 0 AND confidence < 0.75 ORDER BY photo_count DESC")
    List<FaceCluster> getLowConfidenceClusters();

    @Query("UPDATE face_clusters SET person_name = :name, is_confirmed = 1 WHERE id = :id")
    void setName(long id, String name);

    @Query("UPDATE face_clusters SET photo_count = photo_count + :delta WHERE id = :id")
    void incrementCount(long id, int delta);

    @Query("UPDATE face_clusters SET cover_photo_path = :path WHERE id = :id")
    void updateCoverPhoto(long id, String path);

    @Query("UPDATE face_clusters SET confidence = :confidence WHERE id = :id")
    void updateConfidence(long id, float confidence);
}
