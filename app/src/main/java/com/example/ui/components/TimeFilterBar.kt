package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TimeFilter
import com.example.domain.model.TimePeriod
import com.example.ui.theme.GoldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterBar(
    currentFilter: TimeFilter,
    onFilterChanged: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = currentFilter.customStartMillis ?: System.currentTimeMillis() - 7 * 86400000L,
        initialSelectedEndDateMillis = currentFilter.customEndMillis ?: System.currentTimeMillis()
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimePeriod.values().forEach { period ->
                val isSelected = currentFilter.period == period
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (period == TimePeriod.CUSTOM) {
                            showDatePickerDialog = true
                        } else {
                            onFilterChanged(TimeFilter(period = period))
                        }
                    },
                    label = {
                        Text(
                            text = period.titleArabic,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (period == TimePeriod.CUSTOM) {
                        {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = GoldPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = GoldPrimary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.5.dp
                    ),
                    modifier = Modifier.testTag("filter_chip_${period.name.lowercase()}")
                )
            }
        }

        // If custom filter selected, display active custom dates badge
        if (currentFilter.period == TimePeriod.CUSTOM &&
            currentFilter.customStartMillis != null &&
            currentFilter.customEndMillis != null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "من ${dateFormatter.format(Date(currentFilter.customStartMillis))} إلى ${dateFormatter.format(Date(currentFilter.customEndMillis))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldPrimary,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            onFilterChanged(
                                TimeFilter(
                                    period = TimePeriod.CUSTOM,
                                    customStartMillis = minOf(start, end),
                                    customEndMillis = maxOf(start, end) + 86399999L
                                )
                            )
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("تطبيق", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "حدد تاريخ البداية والنهاية",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(480.dp)
            )
        }
    }
}
