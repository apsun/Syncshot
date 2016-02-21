package com.crossbowffs.syncshot.util

import java.text.SimpleDateFormat
import java.util.*


private fun formatUnixTime(format: String, time: Long): String {
    val cal = Calendar.getInstance()
    val fmt = SimpleDateFormat(format)
    cal.timeInMillis = time * 1000
    return fmt.format(cal.time)
}

fun unixTimeToDate(time: Long) = formatUnixTime("yyyy-MM-dd", time)
fun unixTimeToTime(time: Long) = formatUnixTime("hh:mm a", time)
