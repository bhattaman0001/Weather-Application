package com.example.weatherapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.network.WeatherModel
import com.example.weatherapp.view.viewholder.HourWeatherViewHolder
import com.squareup.picasso.Picasso

class HourWeatherAdapter(private val weatherModelArrayList: ArrayList<WeatherModel>) : RecyclerView.Adapter<HourWeatherViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourWeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_hour_weather, parent, false)
        return HourWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourWeatherViewHolder, position: Int) {
        val weatherModel = weatherModelArrayList[position]
        holder.tvTime.text = weatherModel.time
        holder.tvTemp.text = weatherModel.temp
        val iconUrl = "http://openweathermap.org/img/w/" + weatherModel.imgIcon + ".png"
        Picasso.get().load(iconUrl).into(holder.imgIcon)
    }

    override fun getItemCount(): Int {
        return weatherModelArrayList.size
    }
}
