package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.domain.model.TradeStatistics
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportThemeTemplate(val titleArabic: String) {
    LUXURY_DARK("الوضع الفخم الداكن (كحلي وذهب)"),
    EMERALD_TRADER("الوضع الزمردي الملكي"),
    MODERN_LIGHT("الوضع المودرن الفاتح")
}

object ReportImageHelper {

    fun generateReportBitmap(
        accountName: String,
        periodLabel: String,
        stats: TradeStatistics,
        currency: String = "USD",
        template: ReportThemeTemplate = ReportThemeTemplate.LUXURY_DARK
    ): Bitmap {
        val width = 1080
        val height = 1480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette setup
        val isDark = template != ReportThemeTemplate.MODERN_LIGHT
        val bgStart = when (template) {
            ReportThemeTemplate.LUXURY_DARK -> AndroidColor.parseColor("#080D1A")
            ReportThemeTemplate.EMERALD_TRADER -> AndroidColor.parseColor("#061A14")
            ReportThemeTemplate.MODERN_LIGHT -> AndroidColor.parseColor("#F8FAFC")
        }
        val bgEnd = when (template) {
            ReportThemeTemplate.LUXURY_DARK -> AndroidColor.parseColor("#0F172A")
            ReportThemeTemplate.EMERALD_TRADER -> AndroidColor.parseColor("#0B2A20")
            ReportThemeTemplate.MODERN_LIGHT -> AndroidColor.parseColor("#E2E8F0")
        }
        val cardBg = if (isDark) AndroidColor.parseColor("#141E33") else AndroidColor.WHITE
        val cardBorder = if (isDark) AndroidColor.parseColor("#28354D") else AndroidColor.parseColor("#CBD5E1")
        val textPrimary = if (isDark) AndroidColor.WHITE else AndroidColor.parseColor("#0F172A")
        val textSecondary = if (isDark) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#475569")
        val goldColor = AndroidColor.parseColor("#FFB703")
        val greenColor = AndroidColor.parseColor("#10B981")
        val redColor = AndroidColor.parseColor("#EF4444")

        // Draw background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), bgStart, bgEnd, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Decorative background top glow
        val glowPaint = Paint().apply {
            shader = LinearGradient(
                width / 2f, 0f, width / 2f, 300f,
                if (stats.netPnl >= 0) AndroidColor.argb(50, 16, 185, 129) else AndroidColor.argb(50, 239, 68, 68),
                AndroidColor.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 300f, glowPaint)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header - App Name
        paint.color = goldColor
        paint.textSize = 52f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("« دفتر تداول »", width / 2f, 90f, paint)

        // Slogan
        paint.color = textSecondary
        paint.textSize = 28f
        paint.isFakeBoldText = false
        canvas.drawText("راقب صفقاتك، افهم أداءك، وطوّر تداولك", width / 2f, 135f, paint)

        // Subheader - Account & Period Pill
        val pillRect = RectF(140f, 165f, (width - 140).toFloat(), 225f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) AndroidColor.parseColor("#1E293B") else AndroidColor.parseColor("#E2E8F0")
        }
        canvas.drawRoundRect(pillRect, 30f, 30f, pillPaint)

        paint.color = textPrimary
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("$accountName  •  الفترة: $periodLabel", width / 2f, 204f, paint)

        // Net P&L Hero Card
        val heroRect = RectF(60f, 260f, (width - 60).toFloat(), 450f)
        val heroPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cardBg
        }
        canvas.drawRoundRect(heroRect, 32f, 32f, heroPaint)

        val heroBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cardBorder
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(heroRect, 32f, 32f, heroBorderPaint)

        paint.color = textSecondary
        paint.textSize = 28f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("صافي الأرباح / الخسائر", width / 2f, 315f, paint)

        val isProfitable = stats.netPnl >= 0
        paint.color = if (isProfitable) greenColor else redColor
        paint.textSize = 66f
        paint.isFakeBoldText = true
        val sign = if (isProfitable) "+" else ""
        canvas.drawText("$sign${String.format(Locale.US, "%,.2f", stats.netPnl)} $currency", width / 2f, 395f, paint)

        // Profit & Loss Split Bar
        val barTop = 480f
        val barHeight = 44f
        val barLeft = 60f
        val barRight = (width - 60).toFloat()
        val barWidth = barRight - barLeft

        val total = stats.winningTradesCount + stats.losingTradesCount
        val winWeight = when {
            total == 0 -> 0.5f
            stats.winRatePercent <= 0.0 -> 0.03f
            stats.lossRatePercent <= 0.0 -> 0.97f
            else -> (stats.winRatePercent / (stats.winRatePercent + stats.lossRatePercent)).toFloat().coerceIn(0.05f, 0.95f)
        }
        val winWidth = barWidth * winWeight

        // Green segment
        val greenBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = greenColor }
        canvas.drawRoundRect(RectF(barLeft, barTop, barLeft + winWidth, barTop + barHeight), 22f, 22f, greenBarPaint)

        // Red segment
        val redBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = redColor }
        canvas.drawRoundRect(RectF(barLeft + winWidth - 10f, barTop, barRight, barTop + barHeight), 22f, 22f, redBarPaint)

        // Split bar labels
        paint.color = textPrimary
        paint.textSize = 26f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("الربح: ${String.format(Locale.US, "%.1f%%", stats.winRatePercent)}", barLeft + 20f, barTop + 32f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("الخسارة: ${String.format(Locale.US, "%.1f%%", stats.lossRatePercent)}", barRight - 20f, barTop + 32f, paint)

        // Grid of 4 Stat Cards
        val gridTop = 560f
        val cardW = (width - 140f) / 2f
        val cardH = 150f

        drawMiniCard(canvas, 60f, gridTop, cardW, cardH, cardBg, cardBorder, "إجمالي الأرباح", "+${String.format(Locale.US, "%,.2f", stats.totalProfits)} $currency", greenColor, textSecondary)
        drawMiniCard(canvas, 80f + cardW, gridTop, cardW, cardH, cardBg, cardBorder, "إجمالي الخسائر", "-${String.format(Locale.US, "%,.2f", stats.totalLosses)} $currency", redColor, textSecondary)

        val gridTop2 = gridTop + cardH + 20f
        drawMiniCard(canvas, 60f, gridTop2, cardW, cardH, cardBg, cardBorder, "نسبة النجاح (Win Rate)", "${String.format(Locale.US, "%.1f%%", stats.winRatePercent)}", goldColor, textSecondary)
        drawMiniCard(canvas, 80f + cardW, gridTop2, cardW, cardH, cardBg, cardBorder, "عدد الصفقات", "${stats.totalTradesCount} (${stats.winningTradesCount} رابحة / ${stats.losingTradesCount} خاسرة)", textPrimary, textSecondary)

        val gridTop3 = gridTop2 + cardH + 20f
        drawMiniCard(canvas, 60f, gridTop3, cardW, cardH, cardBg, cardBorder, "أفضل صفقة", "+${String.format(Locale.US, "%,.2f", stats.bestTrade)} $currency", greenColor, textSecondary)
        drawMiniCard(canvas, 80f + cardW, gridTop3, cardW, cardH, cardBg, cardBorder, "أسوأ صفقة", "${String.format(Locale.US, "%,.2f", stats.worstTrade)} $currency", redColor, textSecondary)

        val gridTop4 = gridTop3 + cardH + 20f
        drawMiniCard(canvas, 60f, gridTop4, cardW, cardH, cardBg, cardBorder, "إجمالي اللوتات", String.format(Locale.US, "%.2f Lot", stats.totalLots), textPrimary, textSecondary)
        drawMiniCard(canvas, 80f + cardW, gridTop4, cardW, cardH, cardBg, cardBorder, "متوسط العائد/المخاطرة", if (stats.avgRiskReward > 0) "1 : ${String.format(Locale.US, "%.2f", stats.avgRiskReward)}" else "غير متاح", goldColor, textSecondary)

        // Footer Stamp
        val footerY = height - 50f
        paint.color = textSecondary
        paint.textSize = 22f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
        canvas.drawText("تم إنشاء التقرير بواسطة تطبيق «دفتر تداول»  •  ${dateFormatter.format(Date())}", width / 2f, footerY, paint)

        return bitmap
    }

    private fun drawMiniCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        bgColor: Int,
        borderColor: Int,
        title: String,
        value: String,
        valueColor: Int,
        labelColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(rect, 24f, 24f, p)

        p.color = borderColor
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        canvas.drawRoundRect(rect, 24f, 24f, p)

        p.style = Paint.Style.FILL
        p.color = labelColor
        p.textSize = 24f
        p.textAlign = Paint.Align.CENTER
        p.isFakeBoldText = false
        canvas.drawText(title, x + w / 2f, y + 45f, p)

        p.color = valueColor
        p.textSize = 32f
        p.isFakeBoldText = true
        canvas.drawText(value, x + w / 2f, y + 105f, p)
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "Trading_Report_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TradingJournal")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                    true
                } else false
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "TradingJournal")
                if (!appDir.exists()) appDir.mkdirs()
                val imageFile = File(appDir, filename)
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "trading_report.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير أداء التداول - دفتر تداول")
                putExtra(Intent.EXTRA_TEXT, "تقرير أداء الصفقات من تطبيق «دفتر تداول»")
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة صورة التقرير"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
