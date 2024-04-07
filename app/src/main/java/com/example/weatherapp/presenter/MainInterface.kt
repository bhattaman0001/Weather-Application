package com.example.weatherapp.presenter

import com.example.weatherapp.network.WeatherModel
import com.google.gson.JsonObject

interface MainInterface {
    fun onGetHourWeatherSuccess(weatherData: JsonObject?, hourWeatherData: JsonObject?)
    fun onGetCurrentWeatherSuccess(weatherData: JsonObject?)
    fun setCurrentCity(weatherData: JsonObject?)
}
