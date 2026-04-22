package com.hisabbook.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hisabbook.app.data.db.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE personId = :personId ORDER BY createdAt DESC")
    fun observeByPerson(personId: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<EntryEntity>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM entries WHERE type = :type AND createdAt BETWEEN :from AND :to")
    fun sumForTypeBetween(type: String, from: Long, to: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
