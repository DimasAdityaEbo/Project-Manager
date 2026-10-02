package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.DeadlineInfo
import com.example.util.DeadlineUrgency

@Composable
fun DeadlineBadge(
    deadlineInfo: DeadlineInfo,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor, icon) = when (deadlineInfo.urgency) {
        DeadlineUrgency.OVERDUE -> Triple(
            StatusError.copy(alpha = 0.15f),
            StatusError,
            Icons.Default.Error
        )
        DeadlineUrgency.DUE_TODAY -> Triple(
            StatusWarning.copy(alpha = 0.2f),
            Color(0xFFD97706),
            Icons.Default.Warning
        )
        DeadlineUrgency.DUE_TOMORROW -> Triple(
            StatusWarning.copy(alpha = 0.15f),
            Color(0xFFB45309),
            Icons.Default.AccessTime
        )
        DeadlineUrgency.DUE_THIS_WEEK -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            Icons.Default.AccessTime
        )
        DeadlineUrgency.FUTURE -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.AccessTime
        )
        DeadlineUrgency.COMPLETED -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            Icons.Default.CheckCircle
        )
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = deadlineInfo.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
