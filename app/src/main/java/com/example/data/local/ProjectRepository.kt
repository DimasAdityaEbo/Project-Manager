package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {

    val allProjectsWithTasks: Flow<List<ProjectWithTasks>> = dao.getAllProjectsWithTasks()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getProjectWithTasksById(id: Long): Flow<ProjectWithTasks?> {
        return dao.getProjectWithTasksById(id)
    }

    suspend fun insertProject(project: ProjectEntity): Long {
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        dao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: Long) {
        dao.deleteProjectById(id)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        dao.deleteTaskById(id)
    }

    suspend fun seedInitialDataIfNeeded() {
        if (dao.getProjectCount() == 0) {
            val now = System.currentTimeMillis()
            val oneDay = 24L * 60 * 60 * 1000

            // Project 1: Mobile App
            val p1Id = dao.insertProject(
                ProjectEntity(
                    title = "Aplikasi Mobile E-Commerce",
                    description = "Pengembangan aplikasi belanja online Android menggunakan Jetpack Compose & Clean Architecture.",
                    category = "Software",
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    startDateMillis = now - (5 * oneDay),
                    deadlineMillis = now + (3 * oneDay),
                    clientOrTag = "PT Niaga Maju"
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p1Id,
                    title = "Rancang UI Keranjang & Checkout",
                    isCompleted = true,
                    completedAtMillis = now - (2 * oneDay),
                    priority = "HIGH",
                    deadlineMillis = now - (1 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p1Id,
                    title = "Integrasi Payment Gateway API",
                    isCompleted = false,
                    priority = "HIGH",
                    deadlineMillis = now + (2 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p1Id,
                    title = "Pengujian User Acceptance (UAT)",
                    isCompleted = false,
                    priority = "MEDIUM",
                    deadlineMillis = now + (3 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p1Id,
                    title = "Rilis APK ke Google Play Console",
                    isCompleted = false,
                    priority = "HIGH",
                    deadlineMillis = now + (3 * oneDay)
                )
            )

            // Project 2: UI/UX Redesign
            val p2Id = dao.insertProject(
                ProjectEntity(
                    title = "Redesain UI/UX Dashboard Web",
                    description = "Pembaruan total visual antarmuka analitik dan sistem navigasi berbasis Material Design 3.",
                    category = "Desain",
                    priority = "MEDIUM",
                    status = "IN_PROGRESS",
                    startDateMillis = now - (3 * oneDay),
                    deadlineMillis = now + (6 * oneDay),
                    clientOrTag = "TechFlow Studio"
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p2Id,
                    title = "Audit pengalaman pengguna eksisting",
                    isCompleted = true,
                    completedAtMillis = now - (1 * oneDay),
                    priority = "LOW",
                    deadlineMillis = now - (2 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p2Id,
                    title = "Buat Design System & Typography tokens",
                    isCompleted = true,
                    completedAtMillis = now,
                    priority = "MEDIUM",
                    deadlineMillis = now + (1 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p2Id,
                    title = "High-Fidelity Mockup Dashboard Utama",
                    isCompleted = false,
                    priority = "MEDIUM",
                    deadlineMillis = now + (4 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p2Id,
                    title = "Presentasi prototipe interaktif ke klien",
                    isCompleted = false,
                    priority = "HIGH",
                    deadlineMillis = now + (6 * oneDay)
                )
            )

            // Project 3: Overdue audit project to show alert system
            val p3Id = dao.insertProject(
                ProjectEntity(
                    title = "Laporan Audit & Kepatuhan Q3",
                    description = "Verifikasi dokumen keuangan dan kepatuhan sistem operasional internal.",
                    category = "Bisnis",
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    startDateMillis = now - (10 * oneDay),
                    deadlineMillis = now - (1 * oneDay), // Overdue!
                    clientOrTag = "Internal Finance"
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p3Id,
                    title = "Kumpulkan rekapitulasi transaksi Q1-Q3",
                    isCompleted = true,
                    completedAtMillis = now - (2 * oneDay),
                    priority = "HIGH",
                    deadlineMillis = now - (4 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p3Id,
                    title = "Finalisasi dokumen verifikasi pajak",
                    isCompleted = false,
                    priority = "HIGH",
                    deadlineMillis = now - (1 * oneDay)
                )
            )

            // Project 4: Marketing Campaign
            val p4Id = dao.insertProject(
                ProjectEntity(
                    title = "Kampanye Peluncuran Digital Q4",
                    description = "Pelaksanaan strategi multi-channel marketing menyambut musim liburan akhir tahun.",
                    category = "Pemasaran",
                    priority = "LOW",
                    status = "PLANNING",
                    startDateMillis = now,
                    deadlineMillis = now + (14 * oneDay),
                    clientOrTag = "Brand Media"
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p4Id,
                    title = "Riset kata kunci dan audiens target",
                    isCompleted = false,
                    priority = "MEDIUM",
                    deadlineMillis = now + (7 * oneDay)
                )
            )
            dao.insertTask(
                TaskEntity(
                    projectId = p4Id,
                    title = "Produksi aset grafis & video reels promosi",
                    isCompleted = false,
                    priority = "LOW",
                    deadlineMillis = now + (11 * oneDay)
                )
            )
        }
    }
}
