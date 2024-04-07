package com.example.weatherapp.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ChildWeatherLayoutBinding
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class WeatherAdapter(private val context: Context) {

    private lateinit var binding: ChildWeatherLayoutBinding

    private fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        return current.format(formatter)
    }

    @SuppressLint("SetTextI18n")
    fun bind(cardView: CardView) {
        binding = ChildWeatherLayoutBinding.inflate(LayoutInflater.from(context), cardView, false)
        cardView.addView(binding.root)
    }

    @SuppressLint("SetTextI18n")
    fun updateData(weatherData: JsonObject, hourWeatherData: JsonObject) {
        binding.progressBar.visibility = View.GONE
        binding.constraintLayout.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            val url = "http://openweathermap.org/img/w/" + weatherData["weather"].asJsonArray[0].asJsonObject["icon"].asString + ".png"
            Glide.with(context)
                .load(url)
                .dontAnimate()
                .into(binding.weatherImg)
        }

        // temperature of place
        val temperatureKelvin = weatherData.getAsJsonObject("main").get("temp").asDouble
        val temperatureCelsius = temperatureKelvin - 273.15
        binding.temperature.text = String.format("%.2f", temperatureCelsius) + " °C"

        // content and description
        binding.content.text = "${weatherData["weather"].asJsonArray[0].asJsonObject["main"].asString}\n${weatherData["weather"].asJsonArray[0].asJsonObject["description"].asString}"

        // place
        binding.place.text = "${weatherData["name"].asString}, ${weatherData["sys"].asJsonObject["country"].asString}"

        // min max temperature
        val tempMaxKelvin = weatherData.getAsJsonObject("main").get("temp_max").asDouble
        val tempMinKelvin = weatherData.getAsJsonObject("main").get("temp_min").asDouble
        val tempKelvin = weatherData.getAsJsonObject("main").get("temp").asDouble
        val tempMaxCelsius = tempMaxKelvin - 273.15
        val tempMinCelsius = tempMinKelvin - 273.15
        val tempCelsius = tempKelvin - 273.15
        binding.feelsLike.text = "${String.format("%.2f", tempMaxCelsius)}/${String.format("%.2f", tempMinCelsius)} feels like ${String.format("%.2f", tempCelsius)} °C"

        // day date time
        val calendar = Calendar.getInstance()
        val day = getDayName(calendar.get(Calendar.DAY_OF_WEEK))
        val dateTime = getCurrentDateTime().split(" ")
        binding.dayTime.text = "$day, ${dateTime[0]}, ${dateTime[1]}"

        // Pressure
        val pressure = weatherData["main"].asJsonObject["pressure"]?.asInt ?: 0
        binding.pressureVal.text = "$pressure hPa"

        // Humidity
        val humidity = weatherData["main"].asJsonObject["humidity"]?.asInt ?: 0
        binding.humidityVal.text = "$humidity%"

        // Sea Level
        val seaLevel = weatherData["main"].asJsonObject["sea_level"]?.asDouble ?: 0.0
        binding.seaLevelVal.text = String.format("%.2f", seaLevel) + " m"

        // Ground Level
        val groundLevel = weatherData["main"].asJsonObject["grnd_level"]?.asDouble ?: 0.0
        binding.grndLevelVal.text = String.format("%.2f", groundLevel) + " m"

        // Speed
        val speed = weatherData["wind"].asJsonObject["speed"]?.asDouble ?: 0.0
        binding.speedVal.text = String.format("%.2f", speed) + " m/s"

        // Gust
        val gust = weatherData["wind"].asJsonObject["gust"]?.asDouble ?: 0.0
        binding.gustVal.text = String.format("%.2f", gust) + " m/s"


        val adapter = HourWeatherAdapter(context, hourWeatherData)
        binding.getHourWeather.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.getHourWeather.adapter = adapter

    }

    private fun getDayName(get: Int): String {
        val dayName: String = when (get) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
        return dayName
    }

}