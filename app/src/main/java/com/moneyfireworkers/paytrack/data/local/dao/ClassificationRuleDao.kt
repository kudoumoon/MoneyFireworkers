package com.moneyfireworkers.paytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyfireworkers.paytrack.data.local.entity.ClassificationRuleEntity

@Dao
interface ClassificationRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClassificationRuleEntity>)

    @Query("SELECT COUNT(*) FROM classification_rules")
    suspend fun count(): Int

    @Query("SELECT * FROM classification_rules WHERE isEnabled = 1 ORDER BY priority DESC")
    suspend fun getAllEnabled(): List<ClassificationRuleEntity>
}
