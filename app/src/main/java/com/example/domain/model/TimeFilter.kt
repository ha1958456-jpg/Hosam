package com.example.domain.model

import java.util.Calendar

enum class TimePeriod(val titleArabic: String) {
    TODAY("اليوم"),
    YESTERDAY("أمس"),
    LAST_7_DAYS("آخر 7 أيام"),
    LAST_30_DAYS("آخر 30 يوماً"),
    THIS_WEEK("هذا الأسبوع"),
    LAST_WEEK("الأسبوع الماضي"),
    THIS_MONTH("هذا الشهر"),
    LAST_MONTH("الشهر الماضي"),
    LAST_3_MONTHS("آخر 3 أشهر"),
    LAST_6_MONTHS("آخر 6 أشهر"),
    THIS_YEAR("هذه السنة"),
    ALL("الكل"),
    CUSTOM("تخصيص فترة")
}

data class TimeFilter(
    val period: TimePeriod = TimePeriod.ALL,
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null
) {
    fun getRange(): Pair<Long, Long>? {
        val now = Calendar.getInstance()
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        // Set end to end of today by default
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        // Set start to start of day
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        return when (period) {
            TimePeriod.ALL -> null

            TimePeriod.TODAY -> Pair(start.timeInMillis, end.timeInMillis)

            TimePeriod.YESTERDAY -> {
                start.add(Calendar.DAY_OF_YEAR, -1)
                end.add(Calendar.DAY_OF_YEAR, -1)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_7_DAYS -> {
                start.add(Calendar.DAY_OF_YEAR, -7)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_30_DAYS -> {
                start.add(Calendar.DAY_OF_YEAR, -30)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.THIS_WEEK -> {
                start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_WEEK -> {
                start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
                start.add(Calendar.WEEK_OF_YEAR, -1)
                end.timeInMillis = start.timeInMillis
                end.add(Calendar.DAY_OF_YEAR, 6)
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.THIS_MONTH -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_MONTH -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.add(Calendar.MONTH, -1)
                end.set(Calendar.DAY_OF_MONTH, 1)
                end.add(Calendar.MILLISECOND, -1)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_3_MONTHS -> {
                start.add(Calendar.MONTH, -3)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.LAST_6_MONTHS -> {
                start.add(Calendar.MONTH, -6)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.THIS_YEAR -> {
                start.set(Calendar.DAY_OF_YEAR, 1)
                Pair(start.timeInMillis, end.timeInMillis)
            }

            TimePeriod.CUSTOM -> {
                if (customStartMillis != null && customEndMillis != null) {
                    Pair(customStartMillis, customEndMillis)
                } else null
            }
        }
    }
}
