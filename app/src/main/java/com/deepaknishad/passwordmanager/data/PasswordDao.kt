package com.deepaknishad.passwordmanager.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    companion object {
        private const val TAG = "PasswordDao"
    }

    @Query("SELECT * FROM passwords ORDER BY id DESC")
    fun getAllPasswords(): Flow<List<PasswordEntity>> {
        Log.d(TAG, "Fetching all passwords from database (sorted by ID DESC)")
        return _getAllPasswords()
    }

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    suspend fun getPasswordById(id: Long): PasswordEntity?

    @Insert
    suspend fun insertPassword(password: PasswordEntity): Long {
        Log.d(TAG, "Inserting password with initial ID: ${password.id}")
        val insertedId = _insertPassword(password)
        Log.d(TAG, "Insert completed, generated ID: $insertedId")
        return insertedId
    }

    @Update
    suspend fun updatePassword(password: PasswordEntity): Int {
        Log.d(TAG, "Updating password with ID: ${password.id}")
        val rowsAffected = _updatePassword(password)
        Log.d(TAG, "Update completed, rows affected: $rowsAffected")
        return rowsAffected
    }

    @Delete
    suspend fun deletePassword(password: PasswordEntity): Int {
        Log.d(TAG, "Deleting password with ID: ${password.id}")
        val rowsAffected = _deletePassword(password)
        Log.d(TAG, "Delete completed, rows affected: $rowsAffected")
        return rowsAffected
    }

    @Query("SELECT * FROM passwords ORDER BY id DESC")
    fun _getAllPasswords(): Flow<List<PasswordEntity>>

    @Insert
    suspend fun _insertPassword(password: PasswordEntity): Long

    @Update
    suspend fun _updatePassword(password: PasswordEntity): Int

    @Delete
    suspend fun _deletePassword(password: PasswordEntity): Int
}