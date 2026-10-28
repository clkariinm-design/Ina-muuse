package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.Categories
import com.example.model.Currency
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.AppStrings
import com.example.util.CurrencyFormatter
import com.example.viewmodel.CategorySummary
import com.example.viewmodel.DaySpending
import com.example.viewmodel.MonthSpending

@Composable
fun DailySpendingBarChart(
    dailySpendings: List<DaySpending>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (dailySpendings.isEmpty() || dailySpendings.all { it.amountMinor == 0L }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "0 ${currency.symbol}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        return
    }

    val maxAmount = dailySpendings.maxOfOrNull { it.amountMinor }?.coerceAtLeast(1L) ?: 1L
    val barColor = ExpenseRed.copy(alpha = 0.75f)
    val maxBarColor = ExpenseRed

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            val count = dailySpendings.size
            if (count == 0) return@Canvas

            val availableWidth = size.width
            val barSpacing = 3.dp.toPx()
            val totalSpacing = barSpacing * (count - 1)
            val barWidth = ((availableWidth - totalSpacing) / count).coerceAtLeast(2.dp.toPx())
            val chartHeight = size.height - 20.dp.toPx()

            // Draw baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(0f, chartHeight),
                end = Offset(availableWidth, chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            dailySpendings.forEachIndexed { index, dayData ->
                val x = index * (barWidth + barSpacing)
                val barFraction = (dayData.amountMinor.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                val barHeight = (chartHeight * barFraction).coerceAtLeast(if (dayData.amountMinor > 0) 4.dp.toPx() else 0f)
                val y = chartHeight - barHeight

                val isMax = dayData.amountMinor == maxAmount && maxAmount > 0
                val color = if (isMax) maxBarColor else barColor

                if (barHeight > 0) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }

        // Days markers (1, 5, 10, 15, 20, 25, 30)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val markers = listOf(1, 5, 10, 15, 20, 25, dailySpendings.size)
            markers.forEach { day ->
                Text(
                    text = "$day",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendChart(
    monthlySpendings: List<MonthSpending>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (monthlySpendings.isEmpty()) return

    val maxVal = monthlySpendings.maxOfOrNull { maxOf(it.incomeMinor, it.expenseMinor) }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Dakhli / Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Kharash / Expense", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val groupCount = monthlySpendings.size
                val groupWidth = size.width / groupCount
                val barWidth = (groupWidth * 0.28f).coerceAtLeast(6.dp.toPx())
                val chartHeight = size.height - 20.dp.toPx()

                // Baseline
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, chartHeight),
                    end = Offset(size.width, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )

                monthlySpendings.forEachIndexed { index, data ->
                    val groupCenter = (index * groupWidth) + (groupWidth / 2)

                    // Income bar (left)
                    val incHeight = (chartHeight * (data.incomeMinor.toFloat() / maxVal.toFloat())).coerceIn(0f, chartHeight)
                    if (incHeight > 0) {
                        drawRoundRect(
                            color = IncomeGreen,
                            topLeft = Offset(groupCenter - barWidth - 2.dp.toPx(), chartHeight - incHeight),
                            size = Size(barWidth, incHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Expense bar (right)
                    val expHeight = (chartHeight * (data.expenseMinor.toFloat() / maxVal.toFloat())).coerceIn(0f, chartHeight)
                    if (expHeight > 0) {
                        drawRoundRect(
                            color = ExpenseRed,
                            topLeft = Offset(groupCenter + 2.dp.toPx(), chartHeight - expHeight),
                            size = Size(barWidth, expHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // Month Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                monthlySpendings.forEach { data ->
                    Text(
                        text = data.monthLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownList(
    categorySummaries: List<CategorySummary>,
    currency: Currency,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (categorySummaries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = AppStrings.noDataReport(language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categorySummaries.forEach { summary ->
            val category = Categories.getCategory(summary.categoryKey)
            val catColor = Color(category.colorHex)
            val catName = AppStrings.categoryName(category, language)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(categoryKey = summary.categoryKey, size = 36.dp, iconSize = 20.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.format(summary.totalAmountMinor, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f%%", summary.percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (summary.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = catColor,
                        trackColor = catColor.copy(alpha = 0.15f),
                    )
                }
            }
        }
    }
}
