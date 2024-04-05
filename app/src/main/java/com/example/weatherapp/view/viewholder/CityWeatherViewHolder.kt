package com.example.weatherapp.view.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R

class CityWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imgIcon: ImageView
    var tvTemp: TextView
    var tvCity: TextView

    init {
        imgIcon = itemView.findViewById(R.id.imgIcon)
        tvCity = itemView.findViewById(R.id.tvCity)
        tvTemp = itemView.findViewById(R.id.tvTemp)
    }
}
