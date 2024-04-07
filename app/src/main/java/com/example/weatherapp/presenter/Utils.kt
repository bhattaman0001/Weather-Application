package com.example.weatherapp.presenter

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Utils {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    lateinit var sharedPreferences: SharedPreferences

    const val REQUEST_LOCATION = 1
    const val WEATHER_DATA = "WEATHER_DATA"
    const val CURRENT_CITY = "CURRENT_CITY"
    const val LATEST_CURRENT_WEATHER_DATA = "LATEST_CURRENT_WEATHER_DATA"
    const val LATEST_CURRENT_HOUR_WEATHER_DATA = "LATEST_HOUR_WEATHER_DATA"

}
