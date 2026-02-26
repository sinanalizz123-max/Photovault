package com.photovault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.photovault.database.entity.Rule;

import java.util.List;

@Dao
public interface RuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);

    @Query("SELECT * FROM rules WHERE is_enabled = 1 ORDER BY priority_order ASC")
    LiveData<List<Rule>> getAllEnabled();

    @Query("SELECT * FROM rules WHERE is_enabled = 1 ORDER BY priority_order ASC")
    List<Rule> getAllEnabledSync();

    @Query("SELECT * FROM rules ORDER BY priority_order ASC")
    LiveData<List<Rule>> getAll();

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    Rule getByIdSync(long id);

    @Query("SELECT * FROM rules WHERE rule_type = :type AND is_enabled = 1")
    List<Rule> getByType(int type);

    @Query("SELECT * FROM rules WHERE account_id = :accountId")
    List<Rule> getByAccount(long accountId);

    @Query("UPDATE rules SET priority_order = :order WHERE id = :id")
    void updatePriority(long id, int order);

    @Query("UPDATE rules SET is_enabled = :enabled WHERE id = :id")
    void setEnabled(long id, boolean enabled);

    @Query("SELECT MAX(priority_order) FROM rules")
    int getMaxPriority();

    /** Check for potential conflicts: same type + condition but different target */
    @Query("SELECT * FROM rules WHERE rule_type = :type AND condition_value = :condition AND id != :excludeId")
    List<Rule> findConflicting(int type, String condition, long excludeId);
}
