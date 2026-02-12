package com.dp.dawalo.data.local.dao

import androidx.room.*
import com.dp.dawalo.data.local.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long
    
    @Update
    suspend fun update(user: User)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?
}
