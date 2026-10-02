package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProjectDialog(
    projectToEdit: ProjectEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: String,
        priority: String,
        deadlineMillis: Long,
        clientOrTag: String,
        tasks: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf(projectToEdit?.title ?: "") }
    var description by remember { mutableStateOf(projectToEdit?.description ?: "") }
    var category by remember { mutableStateOf(projectToEdit?.category ?: "Software") }
    var priority by remember { mutableStateOf(projectToEdit?.priority ?: "MEDIUM") }
    var clientOrTag by remember { mutableStateOf(projectToEdit?.clientOrTag ?: "") }

    val defaultDeadline = remember {
        projectToEdit?.deadlineMillis ?: (System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
    }
    var selectedDeadlineMillis by remember { mutableLongStateOf(defaultDeadline) }

    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val initialTasks = remember { mutableStateListOf<String>() }
    var currentTaskInput by remember { mutableStateOf("") }

    val categories = listOf("Software", "Desain", "Pemasaran", "Bisnis", "Pribadi", "Lainnya")
    val priorities = listOf(
        Triple("HIGH", "Tinggi", MaterialTheme.colorScheme.error),
        Triple("MEDIUM", "Sedang", MaterialTheme.colorScheme.tertiary),
        Triple("LOW", "Rendah", MaterialTheme.colorScheme.secondary)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (projectToEdit == null) "Tambah Proyek Baru" else "Edit Proyek",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Nama Proyek *") },
                    placeholder = { Text("Contoh: Redesain Aplikasi Mobile") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Nama proyek tidak boleh kosong") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Tujuan") },
                    placeholder = { Text("Penjelasan singkat lingkup proyek...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_description")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Client or Tag
                OutlinedTextField(
                    value = clientOrTag,
                    onValueChange = { clientOrTag = it },
                    label = { Text("Klien / Kategori Tag (Opsional)") },
                    placeholder = { Text("Contoh: Klien Internal, PT Maju...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Kategori Proyek",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selection
                Text(
                    text = "Prioritas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { (code, label, _) ->
                        FilterChip(
                            selected = priority == code,
                            onClick = { priority = code },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deadline Selection
                Text(
                    text = "Tenggat Waktu Proyek",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_select_deadline")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Tenggat: ${DateTimeUtils.formatDate(selectedDeadlineMillis)}",
                        fontWeight = FontWeight.Medium
                    )
                }

                // Quick presets
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val oneDay = 24L * 60 * 60 * 1000
                    val presets = listOf(
                        "+3 Hari" to 3 * oneDay,
                        "+1 Minggu" to 7 * oneDay,
                        "+2 Minggu" to 14 * oneDay,
                        "+1 Bulan" to 30 * oneDay
                    )
                    presets.forEach { (label, duration) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                selectedDeadlineMillis = System.currentTimeMillis() + duration
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                if (projectToEdit == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tugas / Milestone Awal (Opsional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentTaskInput,
                            onValueChange = { currentTaskInput = it },
                            placeholder = { Text("Contoh: Setup repositori git", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (currentTaskInput.isNotBlank()) {
                                    initialTasks.add(currentTaskInput.trim())
                                    currentTaskInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
                        }
                    }

                    if (initialTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        initialTasks.forEachIndexed { index, taskName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• $taskName",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { initialTasks.removeAt(index) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus tugas",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(
                            title,
                            description,
                            category,
                            priority,
                            selectedDeadlineMillis,
                            clientOrTag,
                            initialTasks.toList()
                        )
                    }
                },
                modifier = Modifier.testTag("button_save_project")
            ) {
                Text(if (projectToEdit == null) "Simpan Proyek" else "Perbarui")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDeadlineMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDeadlineMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
