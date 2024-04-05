package com.example.weatherapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.network.WeatherModel
import com.example.weatherapp.view.viewholder.CityWeatherViewHolder
import com.squareup.picasso.Picasso

class CityWeatherAdapter(private val weatherModelArrayList: ArrayList<WeatherModel>) : RecyclerView.Adapter<CityWeatherViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityWeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_city_weather, parent, false)
        return CityWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityWeatherViewHolder, position: Int) {
        val weatherModel = weatherModelArrayList[position]
        holder.tvCity.text = weatherModel.time
        holder.tvTemp.text = weatherModel.temp
        val iconUrl = "http://openweathermap.org/img/w/" + weatherModel.imgIcon + ".png"
        Picasso.get().load(iconUrl).into(holder.imgIcon)
    }

    override fun getItemCount(): Int {
        return weatherModelArrayList.size
    }
}
