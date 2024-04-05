package com.example.weatherapp.view.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R

class HourWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTime: TextView
    var tvTemp: TextView
    var imgIcon: ImageView

    init {
        tvTime = itemView.findViewById(R.id.tvTime)
        tvTemp = itemView.findViewById(R.id.tvTemp)
        imgIcon = itemView.findViewById(R.id.imgIcon)
    }
}
