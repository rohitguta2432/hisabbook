package com.hisabbook.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hisabbook.app.data.db.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE type = :type ORDER BY name ASC")
    fun observeByType(type: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(person: PersonEntity)

    @Update
    suspend fun update(person: PersonEntity)

    @Query("UPDATE persons SET balancePaise = balancePaise + :delta WHERE id = :id")
    suspend fun addToBalance(id: String, delta: Long)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun delete(id: String)
}
