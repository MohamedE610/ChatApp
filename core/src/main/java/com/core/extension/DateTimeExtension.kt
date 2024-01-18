package com.core.extension

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatDateToHhMm(): String {
    Log.e("date-----", "" + this)
    return formatDateTo(inputPattern = "hh:mm a")
}

fun Long.formatDateTo(inputPattern: String = "hh:mm a"): String {
    Log.e("date-----", "" + this)
    return try {
        val date = Date(this)
        val formattedDateAsDigitMonth = SimpleDateFormat(inputPattern, Locale.getDefault())
        formattedDateAsDigitMonth.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}