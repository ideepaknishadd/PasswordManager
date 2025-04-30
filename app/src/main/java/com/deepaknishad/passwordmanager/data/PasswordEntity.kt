package com.deepaknishad.passwordmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountType: String,
    val username: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val encryptedPassword: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PasswordEntity
        if (id != other.id) return false
        if (accountType != other.accountType) return false
        if (username != other.username) return false
        if (!encryptedPassword.contentEquals(other.encryptedPassword)) return false
        if (!iv.contentEquals(other.iv)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + accountType.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + encryptedPassword.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}