package com.example.weatherapp.presenter

import com.example.weatherapp.network.ApiHelper
import com.example.weatherapp.network.WeatherModel
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil

class MainPresenter(private val mainInterface: MainInterface) {
    private var hourWeatherData: JsonObject? = null
    private var weatherData: JsonObject? = null
    fun getCurrentWeather(latitude: String?, longitude: String?) {
        val call = ApiHelper.apiHelper.getCurrentWeather(latitude, longitude, API_KEY)
        call!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    weatherData = response.body()
                    mainInterface.onGetCurrentWeatherSuccess(weatherData)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {}
        })
    }

    fun getHourWeather(latitude: String?, longitude: String?) {
        val call = ApiHelper.apiHelper.getHourWeather(latitude, longitude, API_KEY)
        call!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    hourWeatherData = response.body()
                    mainInterface.onGetHourWeatherSuccess(hourWeatherData)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {}
        })
    }

    fun getCityWeather(city: String?) {
        val call = ApiHelper.apiHelper.getWeatherByCity(city, API_KEY)
        call!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful && !call.isCanceled) {
                    val item = response.body()
                    val temp = ceil(item!!.asJsonObject["main"].asJsonObject["temp"].asFloat - 275.25).toString()
                    val imgIcon = item.asJsonObject["weather"].asJsonArray[0].asJsonObject["icon"].asString
                    mainInterface.setCurrentCity(WeatherModel(city!!, temp, imgIcon))
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {}
        })
    }

    fun createHourWeatherList(hourWeatherData: JsonObject): ArrayList<WeatherModel> {
        val hourWeatherModelArrayList = ArrayList<WeatherModel>()
        for (item in hourWeatherData["list"].asJsonArray) {
            val temp = ceil(item.asJsonObject["main"].asJsonObject["temp"].asFloat - 275.25).toString()
            val times = item.asJsonObject["dt_txt"].asString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val time: String = if (times[1] == "00:00:00") Utils.convertDateFormat(times[0]) else times[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0] + ":" + times[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val imgIcon = item.asJsonObject["weather"].asJsonArray[0].asJsonObject["icon"].asString
            val weatherModel = WeatherModel(time, temp, imgIcon)
            hourWeatherModelArrayList.add(weatherModel)
        }
        return hourWeatherModelArrayList
    }

    companion object {
        private const val API_KEY = "5f8622a9fbd391a7b6d40777bdd9790b"
    }
}
