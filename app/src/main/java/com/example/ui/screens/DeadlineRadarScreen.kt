package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ProjectWithTasks
import com.example.data.local.TaskEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.DeadlineBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.ProjectViewModel
import com.example.util.DateTimeUtils
import com.example.util.DeadlineUrgency

@Composable
fun DeadlineRadarScreen(
    viewModel: ProjectViewModel,
    onProjectClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()

    // Categorize projects into urgency buckets
    val overdueProjects = remember(allProjects) {
        allProjects.filter {
            it.project.status != "COMPLETED" &&
                    DateTimeUtils.calculateDeadlineInfo(it.project.deadlineMillis).urgency == DeadlineUrgency.OVERDUE
        }
    }

    val todayTomorrowProjects = remember(allProjects) {
        allProjects.filter {
            it.project.status != "COMPLETED" &&
                    (DateTimeUtils.calculateDeadlineInfo(it.project.deadlineMillis).urgency == DeadlineUrgency.DUE_TODAY ||
                            DateTimeUtils.calculateDeadlineInfo(it.project.deadlineMillis).urgency == DeadlineUrgency.DUE_TOMORROW)
        }
    }

    val thisWeekProjects = remember(allProjects) {
        allProjects.filter {
            it.project.status != "COMPLETED" &&
                    DateTimeUtils.calculateDeadlineInfo(it.project.deadlineMillis).urgency == DeadlineUrgency.DUE_THIS_WEEK
        }
    }

    val futureProjects = remember(allProjects) {
        allProjects.filter {
            it.project.status != "COMPLETED" &&
                    DateTimeUtils.calculateDeadlineInfo(it.project.deadlineMillis).urgency == DeadlineUrgency.FUTURE
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("deadline_radar_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)
    ) {
        item {
            DeadlineRadarHeader(
                overdueCount = overdueProjects.size,
                todayTomorrowCount = todayTomorrowProjects.size,
                thisWeekCount = thisWeekProjects.size
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. OVERDUE (Critical)
        if (overdueProjects.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Terlambat / Butuh Tindakan Cepat",
                    count = overdueProjects.size,
                    color = StatusError,
                    icon = Icons.Default.Error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(overdueProjects, key = { "overdue_${it.project.id}" }) { item ->
                DeadlineProjectCard(
                    projectWithTasks = item,
                    onProjectClick = { onProjectClick(item.project.id) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 2. TODAY & TOMORROW
        if (todayTomorrowProjects.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Hari Ini & Besok",
                    count = todayTomorrowProjects.size,
                    color = StatusWarning,
                    icon = Icons.Default.Today
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(todayTomorrowProjects, key = { "today_${it.project.id}" }) { item ->
                DeadlineProjectCard(
                    projectWithTasks = item,
                    onProjectClick = { onProjectClick(item.project.id) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 3. THIS WEEK
        if (thisWeekProjects.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Minggu Ini (2 - 7 Hari)",
                    count = thisWeekProjects.size,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Alarm
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(thisWeekProjects, key = { "this_week_${it.project.id}" }) { item ->
                DeadlineProjectCard(
                    projectWithTasks = item,
                    onProjectClick = { onProjectClick(item.project.id) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 4. FUTURE
        if (futureProjects.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Mendatang (> 7 Hari)",
                    count = futureProjects.size,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = Icons.Default.CalendarMonth
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(futureProjects, key = { "future_${it.project.id}" }) { item ->
                DeadlineProjectCard(
                    projectWithTasks = item,
                    onProjectClick = { onProjectClick(item.project.id) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (overdueProjects.isEmpty() && todayTomorrowProjects.isEmpty() && thisWeekProjects.isEmpty() && futureProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Semua Tenggat Aman!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tidak ada proyek aktif dengan tenggat yang tertunda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeadlineRadarHeader(
    overdueCount: Int,
    todayTomorrowCount: Int,
    thisWeekCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Radar & Linimasa Tenggat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pantau dan kelola prioritas penyelesaian tugas sebelum jatuh tempo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UrgencySummaryBadge(
                    label = "Terlambat",
                    count = overdueCount,
                    color = StatusError,
                    modifier = Modifier.weight(1f)
                )
                UrgencySummaryBadge(
                    label = "Mendesak",
                    count = todayTomorrowCount,
                    color = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                UrgencySummaryBadge(
                    label = "Minggu Ini",
                    count = thisWeekCount,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun UrgencySummaryBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DeadlineProjectCard(
    projectWithTasks: ProjectWithTasks,
    onProjectClick: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit
) {
    val project = projectWithTasks.project
    val dlInfo = DateTimeUtils.calculateDeadlineInfo(project.deadlineMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onProjectClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(category = project.category)
                DeadlineBadge(deadlineInfo = dlInfo)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progres: ${(projectWithTasks.progressPercent * 100).toInt()}% • ${projectWithTasks.completedTasks}/${projectWithTasks.totalTasks} Selesai",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PriorityBadge(priority = project.priority)
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { projectWithTasks.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (dlInfo.urgency == DeadlineUrgency.OVERDUE) StatusError else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Direct Milestone Tasks preview with quick checkbox toggle
            if (projectWithTasks.tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tugas Terkait:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                projectWithTasks.tasks.take(3).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) },
                            colors = CheckboxDefaults.colors(checkedColor = StatusSuccess),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (projectWithTasks.tasks.size > 3) {
                    Text(
                        text = "+ ${projectWithTasks.tasks.size - 3} tugas lainnya...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Buka Proyek",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
