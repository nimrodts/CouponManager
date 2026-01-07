package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nimroddayan.couponmanager.data.model.MonthlySpending
import com.nimroddayan.couponmanager.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val spendingByCategory by viewModel.spendingByCategory.collectAsState()
    val spendingByMonth by viewModel.spendingByMonth.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Wallet Overview", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricCard(title = "Total Balance", value = totalBalance, color = MaterialTheme.colorScheme.primary)
            MetricCard(title = "Total Spent", value = totalSpent, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("By Category", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("By Month", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                if (spendingByCategory.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Spending by Category", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        PieChart(data = spendingByCategory)
                    }
                }
            }

            1 -> {
                if (spendingByMonth.isNotEmpty()) {
                    SpendingByMonth(spendingByMonth)
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: Double, color: Color) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "₪%.2f".format(value), style = MaterialTheme.typography.headlineLarge, color = color)
        }
    }
}

@Composable
fun PieChart(data: List<com.nimroddayan.couponmanager.data.model.CategorySpending>) {
    val total = data.sumOf { it.totalSpent }
    val proportions = data.map { it.totalSpent / total }
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = 0f
                proportions.forEachIndexed { index, proportion ->
                    val sweep = (proportion * 360).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        data.forEachIndexed { index, categorySpending ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(colors[index % colors.size], CircleShape)
                )
                Text(text = "${categorySpending.name} - ₪%.2f".format(categorySpending.totalSpent), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun SpendingByMonth(spendingByMonth: List<MonthlySpending>) {
    var expanded by remember { mutableStateOf(false) }
    // Assuming spendingByMonth is sorted chronologically from the ViewModel
    val chartData = if (expanded) spendingByMonth else spendingByMonth.takeLast(6)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Spending by Month", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        SpendingByMonthChart(data = chartData)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (expanded) "Show Less" else "View Full History",
            modifier = Modifier.clickable { expanded = !expanded }
        )
        if (expanded) {
            MonthlySpendingTable(data = spendingByMonth.reversed())
        }
    }
}

@Composable
fun SpendingByMonthChart(data: List<MonthlySpending>) {
    val maxValue = data.maxOfOrNull { it.totalSpent } ?: 0.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val darkPrimaryColor = Color(0xFF004D40) // Example of a darker primary color
    val highestSpendingValue = data.maxOfOrNull { it.totalSpent }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 12.sp, color = Color.Black)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val yAxisLabels = (0..4).map { (it * maxValue / 4).toFloat() }

        // Y-Axis Labels
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween, // This will space out the labels evenly
            horizontalAlignment = Alignment.End
        ) {
            yAxisLabels.reversed().forEach { label ->
                Text(text = "₪${label.toInt()}", fontSize = 12.sp)
            }
        }

        // Bars and X-Axis Labels
        Canvas(modifier = Modifier.fillMaxSize()) { 
            val barWidth = size.width / (data.size * 2)
            val monthNameFormatter = SimpleDateFormat("MMM", Locale.getDefault())

            // Dashed grid lines
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            yAxisLabels.forEach { label ->
                val y = size.height - (label / maxValue.toFloat() * size.height)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
            }

            data.forEachIndexed { index, monthlySpending ->
                val barHeight = (monthlySpending.totalSpent / maxValue * size.height).toFloat()
                val startX = index * barWidth * 2 + barWidth / 2

                // Determine bar color
                val color = if (monthlySpending.totalSpent == highestSpendingValue) darkPrimaryColor else primaryColor

                // Draw rounded bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(startX, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                // Draw month label
                val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthlySpending.month)
                val monthName = date?.let { monthNameFormatter.format(it) } ?: ""
                val monthLayoutResult = textMeasurer.measure(AnnotatedString(monthName), style = labelStyle)
                drawText(
                    textLayoutResult = monthLayoutResult,
                    topLeft = Offset(
                        x = startX + barWidth / 2 - monthLayoutResult.size.width / 2,
                        y = size.height + 5.dp.toPx()
                    )
                )

                // Draw value label above the bar
                val valueText = "₪${monthlySpending.totalSpent.toInt()}"
                val valueLayoutResult = textMeasurer.measure(AnnotatedString(valueText), style = labelStyle)
                drawText(
                    textLayoutResult = valueLayoutResult,
                    topLeft = Offset(
                        x = startX + barWidth / 2 - valueLayoutResult.size.width / 2,
                        y = size.height - barHeight - valueLayoutResult.size.height - 5.dp.toPx()
                    )
                )
            }
        }
    }
}


@Composable
fun MonthlySpendingTable(data: List<MonthlySpending>) {
    LazyColumn {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Month", style = MaterialTheme.typography.titleMedium)
                Text("Total Spent", style = MaterialTheme.typography.titleMedium)
            }
        }
        items(data) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(it.month)
                Text("₪%.2f".format(it.totalSpent))
            }
        }
    }
}
