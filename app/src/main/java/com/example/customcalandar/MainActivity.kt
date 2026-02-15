package com.example.customcalandar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.customcalandar.ui.theme.CustomCalandarTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun rememberCalendarState(initialMonth: YearMonth = YearMonth.now()): CalendarState {
    return remember { CalendarState(initialMonth) }
}

@RequiresApi(Build.VERSION_CODES.O)
class CalendarState(initialMonth: YearMonth) {
    var currentMonth by mutableStateOf(initialMonth)

    fun moveToNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    fun moveToPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
    }

}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomCalandarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalendarScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CustomCalandarTheme {
        Greeting("Android")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonthClick) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month")
        }
        Text(
            text = currentMonth.format(monthFormatter),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNextMonthClick) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    hasEvent: Boolean,
    isCurrentMonth: Boolean
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Makes the cell square
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isCurrentMonth) Color.Black else Color.Gray
            )
            if (hasEvent) {
                // Event indicator
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.Red, CircleShape)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    // A Map of dates to a boolean indicating if an event exists.
    // Replace Boolean with your own event data class.
    events: Map<LocalDate, Boolean> = emptyMap()
) {
    val currentMonth = state.currentMonth
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 for Monday, 7 for Sunday
    val daysInMonth = currentMonth.lengthOfMonth()

    // Calculate the days to show from the previous month
    val prevMonth = currentMonth.minusMonths(1)
    val prevMonthDays = prevMonth.lengthOfMonth()
    val leadingEmptyDays = (firstDayOfWeek - 1) // Assuming Monday is the first day

    val calendarDays = mutableListOf<LocalDate>()

    // Add days from the previous month
    for (i in 0 until leadingEmptyDays) {
        calendarDays.add(0, prevMonth.atDay(prevMonthDays - i))
    }

    // Add day of the current month
    for (day in 1..daysInMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }

    // Add days from the next month to fill the grid
    val remainingCells = 42 - calendarDays.size
    val nextMonth = currentMonth.plusMonths(1)
    for (day in 1..remainingCells) {
        calendarDays.add(nextMonth.atDay(day))
    }

    Column(modifier = modifier) {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonthClick = { state.moveToPreviousMonth() },
            onNextMonthClick = { state.moveToNextMonth() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday Header
        Row(modifier = Modifier.fillMaxWidth()) {
            // Assuming Monday is the first day of the week
            val days = DayOfWeek.entries.toTypedArray()
            // Shift array so Monday is first: [MON, TUE, ..., SUN]
            val weekDays = days.copyOfRange(0, 7)
            for (day in weekDays) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            content = {
                items(calendarDays.size) { index ->
                    val date = calendarDays[index]
                    DayCell(
                        date = date,
                        hasEvent = events[date] ?: false,
                        isCurrentMonth = date.month == currentMonth.month
                    )
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    // Example event date. In a real app, you'd get this from a ViewModel.
    val events = mapOf(
        LocalDate.now().withDayOfMonth(5) to true,
        LocalDate.now().withDayOfMonth(7) to true,
        LocalDate.now().withDayOfMonth(8) to true,
        LocalDate.now().withDayOfMonth(15) to true,
        LocalDate.now().withDayOfMonth(25) to true,
        LocalDate.now().withDayOfMonth(27) to true,
    )

    CustomCalendar(
        modifier = modifier.padding(16.dp),
        events = events
    )
}