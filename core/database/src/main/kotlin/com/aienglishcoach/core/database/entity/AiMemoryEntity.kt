package com.aienglishcoach.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_memories",
    indices = [Index(value = ["kind"])],
)
data class AiMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Name of [com.aienglishcoach.core.domain.model.MemoryKind]. */
    val kind: String,
    val content: String,
    /** Embedding vector serialized as little-endian float32 bytes. */
    val embedding: ByteArray? = null,
    /** 1–5. */
    val importance: Int,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AiMemoryEntity) return false
        return id == other.id &&
            kind == other.kind &&
            content == other.content &&
            importance == other.importance &&
            createdAtEpochMillis == other.createdAtEpochMillis &&
            updatedAtEpochMillis == other.updatedAtEpochMillis &&
            (embedding contentEquals other.embedding)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + importance
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}
