package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectRepository
import com.example.data.local.ProjectWithTasks
import com.example.data.local.TaskEntity
import com.example.util.DateTimeUtils
import com.example.util.DeadlineUrgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProjectFilter {
    ALL,
    IN_PROGRESS,
    URGENT,
    COMPLETED
}

enum class ProjectSort {
    DEADLINE_ASC,
    PROGRESS_DESC,
    PRIORITY_DESC,
    NAME_ASC
}

data class DashboardStats(
    val totalProjects: Int = 0,
    val activeProjects: Int = 0,
    val completedProjects: Int = 0,
    val overdueCount: Int = 0,
    val urgentCount: Int = 0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overallProgress: Float = 0f
)

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ProjectFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(ProjectSort.DEADLINE_ASC)
    val selectedSort = _selectedSort.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId = _selectedProjectId.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(dao)

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    val allProjects: StateFlow<List<ProjectWithTasks>> = repository.allProjectsWithTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentProjectDetail: StateFlow<ProjectWithTasks?> = combine(
        allProjects,
        _selectedProjectId
    ) { projects, selectedId ->
        if (selectedId == null) null else projects.find { it.project.id == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val dashboardStats: StateFlow<DashboardStats> = allProjects.combine(allTasks) { projects, tasks ->
        val total = projects.size
        val active = projects.count { it.project.status != "COMPLETED" }
        val completed = projects.count { it.project.status == "COMPLETED" }

        var overdue = 0
        var urgent = 0
        projects.forEach { item ->
            if (item.project.status != "COMPLETED") {
                val info = DateTimeUtils.calculateDeadlineInfo(item.project.deadlineMillis)
                if (info.urgency == DeadlineUrgency.OVERDUE) overdue++
                if (info.urgency == DeadlineUrgency.DUE_TODAY || info.urgency == DeadlineUrgency.DUE_TOMORROW || info.urgency == DeadlineUrgency.DUE_THIS_WEEK) {
                    urgent++
                }
            }
        }

        val totalT = tasks.size
        val completedT = tasks.count { it.isCompleted }
        val overall = if (totalT > 0) completedT.toFloat() / totalT else if (total > 0 && completed == total) 1f else 0f

        DashboardStats(
            totalProjects = total,
            activeProjects = active,
            completedProjects = completed,
            overdueCount = overdue,
            urgentCount = urgent,
            totalTasks = totalT,
            completedTasks = completedT,
            overallProgress = overall
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    val filteredProjects: StateFlow<List<ProjectWithTasks>> = combine(
        allProjects,
        _searchQuery,
        _selectedFilter,
        _selectedCategory,
        _selectedSort
    ) { projects, query, filter, category, sort ->
        projects
            .filter { item ->
                val p = item.project
                val matchQuery = query.isBlank() ||
                        p.title.contains(query, ignoreCase = true) ||
                        p.description.contains(query, ignoreCase = true) ||
                        p.clientOrTag.contains(query, ignoreCase = true)

                val matchCategory = category == "Semua" || p.category.equals(category, ignoreCase = true)

                val matchFilter = when (filter) {
                    ProjectFilter.ALL -> true
                    ProjectFilter.IN_PROGRESS -> p.status != "COMPLETED"
                    ProjectFilter.URGENT -> {
                        val info = DateTimeUtils.calculateDeadlineInfo(p.deadlineMillis, p.status == "COMPLETED")
                        info.urgency == DeadlineUrgency.OVERDUE ||
                                info.urgency == DeadlineUrgency.DUE_TODAY ||
                                info.urgency == DeadlineUrgency.DUE_TOMORROW ||
                                info.urgency == DeadlineUrgency.DUE_THIS_WEEK
                    }
                    ProjectFilter.COMPLETED -> p.status == "COMPLETED"
                }

                matchQuery && matchCategory && matchFilter
            }
            .sortedWith { a, b ->
                when (sort) {
                    ProjectSort.DEADLINE_ASC -> a.project.deadlineMillis.compareTo(b.project.deadlineMillis)
                    ProjectSort.PROGRESS_DESC -> b.progressPercent.compareTo(a.progressPercent)
                    ProjectSort.NAME_ASC -> a.project.title.compareTo(b.project.title, ignoreCase = true)
                    ProjectSort.PRIORITY_DESC -> {
                        val prioOrder = mapOf("HIGH" to 3, "MEDIUM" to 2, "LOW" to 1)
                        val pA = prioOrder[a.project.priority] ?: 0
                        val pB = prioOrder[b.project.priority] ?: 0
                        pB.compareTo(pA)
                    }
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: ProjectFilter) {
        _selectedFilter.value = filter
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun onSortChange(sort: ProjectSort) {
        _selectedSort.value = sort
    }

    fun selectProject(projectId: Long?) {
        _selectedProjectId.value = projectId
    }

    fun createProject(
        title: String,
        description: String,
        category: String,
        priority: String,
        deadlineMillis: Long,
        startDateMillis: Long = System.currentTimeMillis(),
        clientOrTag: String = "",
        initialTasks: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                status = "IN_PROGRESS",
                startDateMillis = startDateMillis,
                deadlineMillis = deadlineMillis,
                clientOrTag = clientOrTag.trim()
            )
            val newId = repository.insertProject(newProject)
            initialTasks.filter { it.isNotBlank() }.forEach { taskTitle ->
                repository.insertTask(
                    TaskEntity(
                        projectId = newId,
                        title = taskTitle.trim(),
                        isCompleted = false,
                        priority = priority,
                        deadlineMillis = deadlineMillis
                    )
                )
            }
        }
    }

    fun updateProject(
        id: Long,
        title: String,
        description: String,
        category: String,
        priority: String,
        status: String,
        startDateMillis: Long,
        deadlineMillis: Long,
        clientOrTag: String
    ) {
        viewModelScope.launch {
            val updated = ProjectEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                status = status,
                startDateMillis = startDateMillis,
                deadlineMillis = deadlineMillis,
                clientOrTag = clientOrTag.trim()
            )
            repository.updateProject(updated)
        }
    }

    fun updateProjectStatus(projectId: Long, newStatus: String) {
        viewModelScope.launch {
            val current = allProjects.value.find { it.project.id == projectId }?.project
            if (current != null) {
                repository.updateProject(current.copy(status = newStatus))
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProjectById(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAtMillis = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
        }
    }

    fun addTask(projectId: Long, title: String, priority: String = "MEDIUM", deadlineMillis: Long? = null) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    projectId = projectId,
                    title = title.trim(),
                    isCompleted = false,
                    priority = priority,
                    deadlineMillis = deadlineMillis
                )
            )
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }
}
