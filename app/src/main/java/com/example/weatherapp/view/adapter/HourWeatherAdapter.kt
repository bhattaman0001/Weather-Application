package com.example.weatherapp.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.HourLayoutRvBinding
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class HourWeatherAdapter(private val context: Context, private val hourWeatherData: JsonObject?) : RecyclerView.Adapter<HourWeatherAdapter.HourWeatherViewHolder>() {

    private lateinit var binding: HourLayoutRvBinding

    inner class HourWeatherViewHolder(private val binding: HourLayoutRvBinding) : RecyclerView.ViewHolder(binding
        .root) {
        @SuppressLint("SetTextI18n")
        fun bind(hourWeatherDataElement: JsonElement) {
            val dateTime = hourWeatherDataElement.asJsonObject["dt_txt"].asString.split(" ")
            binding.date.text = dateTime[0]
            binding.time.text = dateTime[1]

            val temperatureJsonElement = hourWeatherDataElement.asJsonObject["main"]?.asJsonObject?.get("temp")
            // Convert temperature from Kelvin to Celsius
            val temperatureKelvin = temperatureJsonElement?.asDouble ?: 0.0
            val temperatureCelsius = temperatureKelvin - 273.15
            // Set the converted temperature to the TextView using data binding
            binding.temp.text = String.format("%.2f", temperatureCelsius) + " °C"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourWeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = HourLayoutRvBinding.inflate(layoutInflater, parent, false)
        return HourWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourWeatherViewHolder, position: Int) {
        if (position >= 0) {
            hourWeatherData?.get("list")?.asJsonArray?.get(position)?.let { holder.bind(it) }
        }
    }

    override fun getItemCount(): Int {
        return hourWeatherData!!["list"].asJsonArray.size()
    }
}
