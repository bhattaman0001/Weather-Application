package com.example.weatherapp.network

data class WeatherModel(
    @JvmField var time: String,
    @JvmField var temp: String,
    @JvmField var imgIcon: String
)
