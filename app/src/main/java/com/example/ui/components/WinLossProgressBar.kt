package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBright
import java.util.Locale

@Composable
fun WinLossProgressBar(
    winRate: Double,
    lossRate: Double,
    winningCount: Int,
    losingCount: Int,
    modifier: Modifier = Modifier
) {
    val total = winningCount + losingCount
    // If no closed trades, display 50/50 neutral appearance
    val targetWinWeight = when {
        total == 0 -> 0.5f
        winRate == 0.0 && lossRate == 0.0 -> 0.5f
        winRate <= 0.0 -> 0.02f // Keep tiny sliver for visual balance
        lossRate <= 0.0 -> 0.98f
        else -> (winRate / (winRate + lossRate)).toFloat().coerceIn(0.05f, 0.95f)
    }

    val animatedWinWeight by animateFloatAsState(
        targetValue = targetWinWeight,
        animationSpec = tween(durationMillis = 650),
        label = "winLossSplit"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("win_loss_split_bar")
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Winning Side Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProfitGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "ربح",
                            tint = ProfitGreenBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الصفقات الرابحة ($winningCount)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%%", winRate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreenBright
                        )
                    }
                }

                // Losing Side Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "الصفقات الخاسرة ($losingCount)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%%", lossRate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LossRedBright
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LossRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "خسارة",
                            tint = LossRedBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Split Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Green Section (Win Rate)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedWinWeight)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF059669),
                                        ProfitGreenBright
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (animatedWinWeight > 0.18f) {
                            Text(
                                text = String.format(Locale.US, "%.0f%%", winRate),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Red Section (Loss Rate)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - animatedWinWeight)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LossRedBright,
                                        Color(0xFFB91C1C)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if ((1f - animatedWinWeight) > 0.18f) {
                            Text(
                                text = String.format(Locale.US, "%.0f%%", lossRate),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
