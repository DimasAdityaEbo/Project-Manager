package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class DeadlineUrgency {
    OVERDUE,
    DUE_TODAY,
    DUE_TOMORROW,
    DUE_THIS_WEEK,
    FUTURE,
    COMPLETED
}

data class DeadlineInfo(
    val urgency: DeadlineUrgency,
    val label: String,
    val daysDiff: Long
)

object DateTimeUtils {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val shortDateFormatter = SimpleDateFormat("dd MMM", Locale("id", "ID"))

    fun formatDate(millis: Long): String {
        return dateFormatter.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormatter.format(Date(millis))
    }

    fun calculateDeadlineInfo(deadlineMillis: Long, isCompleted: Boolean = false): DeadlineInfo {
        if (isCompleted) {
            return DeadlineInfo(DeadlineUrgency.COMPLETED, "Selesai", 0)
        }

        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = deadlineMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = targetCal.timeInMillis - nowCal.timeInMillis
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffDays < 0 -> {
                val overdueDays = -diffDays
                DeadlineInfo(
                    DeadlineUrgency.OVERDUE,
                    "Terlambat $overdueDays hari",
                    diffDays
                )
            }
            diffDays == 0L -> {
                DeadlineInfo(
                    DeadlineUrgency.DUE_TODAY,
                    "Hari Ini!",
                    diffDays
                )
            }
            diffDays == 1L -> {
                DeadlineInfo(
                    DeadlineUrgency.DUE_TOMORROW,
                    "Besok",
                    diffDays
                )
            }
            diffDays in 2..7 -> {
                DeadlineInfo(
                    DeadlineUrgency.DUE_THIS_WEEK,
                    "$diffDays hari lagi",
                    diffDays
                )
            }
            else -> {
                DeadlineInfo(
                    DeadlineUrgency.FUTURE,
                    "$diffDays hari lagi",
                    diffDays
                )
            }
        }
    }
}
