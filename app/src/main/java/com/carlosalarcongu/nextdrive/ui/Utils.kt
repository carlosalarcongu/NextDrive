package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun generateIcs(title: String, desc: String, dateMillis: Long): String {
    val format = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    val dateStr = format.format(Date(dateMillis))
    return "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//NextDrive//ES\nBEGIN:VEVENT\nDTSTART:$dateStr\nDTEND:$dateStr\nSUMMARY:$title\nDESCRIPTION:$desc\nEND:VEVENT\nEND:VCALENDAR"
}

@Composable
fun GradientDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent))))
}