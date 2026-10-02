package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithTasks(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val tasks: List<TaskEntity> = emptyList()
) {
    val totalTasks: Int get() = tasks.size
    val completedTasks: Int get() = tasks.count { it.isCompleted }
    val progressPercent: Float
        get() = if (totalTasks == 0) {
            if (project.status == "COMPLETED") 1f else 0f
        } else {
            (completedTasks.toFloat() / totalTasks).coerceIn(0f, 1f)
        }
}
