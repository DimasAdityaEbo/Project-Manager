package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Software", // "Software", "Desain", "Pemasaran", "Bisnis", "Pribadi", "Lainnya"
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val status: String = "IN_PROGRESS", // "PLANNING", "IN_PROGRESS", "REVIEW", "COMPLETED"
    val startDateMillis: Long = System.currentTimeMillis(),
    val deadlineMillis: Long,
    val clientOrTag: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
