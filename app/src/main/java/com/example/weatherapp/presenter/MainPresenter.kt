package com.example.weatherapp.presenter

import com.example.weatherapp.network.ApiHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainPresenter(private val mainInterface: MainInterface) {
    private var hourWeatherData: JsonObject? = null
    private var weatherData: JsonObject? = null
    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun getCurrentWeather(latitude: String?, longitude: String?) {
        firebaseAnalytics.logEvent("getCurrentWeather"){
            param("currentCity","current city weather")
        }
        val call = ApiHelper.apiHelper.getCurrentWeather(latitude, longitude, API_KEY)
        call?.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    weatherData = response.body()
                    mainInterface.onGetCurrentWeatherSuccess(weatherData)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
            }
        })
    }

    fun getHourWeather(weatherData: JsonObject?, latitude: String?, longitude: String?) {
        firebaseAnalytics.logEvent("getHourWeather"){
            param("hourWeather","hour weather data")
        }
        val call = ApiHelper.apiHelper.getHourWeather(latitude, longitude, API_KEY)
        call?.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    hourWeatherData = response.body()
                    mainInterface.onGetHourWeatherSuccess(weatherData, hourWeatherData)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
            }
        })
    }

    fun getCityWeather(city: String?) {
        firebaseAnalytics.logEvent("getCityWeather"){
            param("cityWeather","city weather")
        }
        val call = ApiHelper.apiHelper.getWeatherByCity(city, API_KEY)
        call?.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful && !call.isCanceled) {
                    val item = response.body()
                    mainInterface.setCurrentCity(item)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {}
        })
    }

    companion object {
        private const val API_KEY = "5f8622a9fbd391a7b6d40777bdd9790b"
    }
}
