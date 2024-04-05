package com.example.weatherapp.presenter

import android.content.Context
import android.net.ConnectivityManager
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utils {
    fun convertDateFormat(inputDate: String?): String {
        val date = LocalDate.parse(inputDate)
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        return date.format(outputFormatter)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    val currentDateTimeFormatted: String
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }
}
