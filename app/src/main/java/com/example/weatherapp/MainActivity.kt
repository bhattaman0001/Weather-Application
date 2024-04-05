package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.network.WeatherModel
import com.example.weatherapp.presenter.MainInterface
import com.example.weatherapp.presenter.MainPresenter
import com.example.weatherapp.presenter.Utils
import com.example.weatherapp.view.adapter.CityWeatherAdapter
import com.example.weatherapp.view.adapter.HourWeatherAdapter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlin.math.ceil

class MainActivity : AppCompatActivity(), MainInterface {


    private var latitude: String? = null
    private var longitude: String? = null
    private var locationManager: LocationManager? = null
    private var binding: ActivityMainBinding? = null
    private var weatherData: JsonObject? = null
    private var hourWeatherData: JsonObject? = null
    private var hourWeatherModelArrayList: ArrayList<WeatherModel>? = null
    private var cityWeatherModelArrayList: ArrayList<WeatherModel>? = null
    private var latestUpdated: String? = null
    private var progressDialog: ProgressDialog? = null
    private var mainPresenter: MainPresenter? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)



        mainPresenter = MainPresenter(this)
        progressDialog = ProgressDialog(this, R.style.CustomProgressDialog)
        progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        val gson = Gson()
        weatherData = gson.fromJson(getSharedPreferences(WEATHER_DATA, MODE_PRIVATE).getString(LATEST_CURRENT_WEATHER_DATA, null), JsonObject::class.java)
        latestUpdated = getSharedPreferences(WEATHER_DATA, MODE_PRIVATE).getString(LATEST_CURRENT_WEATHER_DATA, null)
        val type = object : TypeToken<ArrayList<WeatherModel?>?>() {}.type
        hourWeatherModelArrayList = gson.fromJson(getSharedPreferences(WEATHER_DATA, MODE_PRIVATE).getString(LATEST_HOUR_WEATHER_DATA, null), type)
        cityWeatherModelArrayList = gson.fromJson(getSharedPreferences(WEATHER_DATA, MODE_PRIVATE).getString(LATEST_CITIES_WEATHER_DATA, null), type)
        if (cityWeatherModelArrayList == null) cityWeatherModelArrayList = ArrayList()
        if (hourWeatherModelArrayList == null) hourWeatherModelArrayList = ArrayList()
        if (latestUpdated == null) latestUpdated = "Latest update: ..."
        if (!Utils.isNetworkAvailable(this)) {
            if (weatherData != null) loadUI()
        } else {
            cityWeatherModelArrayList = ArrayList()
            hourWeatherModelArrayList = ArrayList()
            loadData()
        }
        binding!!.btnGet.setOnClickListener {
            if (Utils.isNetworkAvailable(applicationContext)) loadData() else Toast.makeText(
                applicationContext, "No internet connection", Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.isNetworkAvailable(applicationContext)) loadData()
    }

    private fun loadData() {
        progressDialog!!.show()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGPS()
        } else {
            location
        }
    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes") { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No") { dialog, which -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private val location: Unit
        get() {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                binding!!.main.visibility = View.GONE
                binding!!.allowPermission.visibility = View.VISIBLE
                progressDialog!!.dismiss()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            } else {
                binding!!.main.visibility = View.VISIBLE
                binding!!.allowPermission.visibility = View.GONE
                val locationGPS = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (locationGPS != null) {
                    val lat = locationGPS.latitude
                    val lon = locationGPS.longitude
                    latitude = lat.toString()
                    longitude = lon.toString()
                    mainPresenter!!.getCurrentWeather(latitude, longitude)
                } else {
                    progressDialog!!.dismiss()
                }
            }
        }

    override fun onGetCurrentWeatherSuccess(weatherData: JsonObject?) {
        this.weatherData = weatherData
        val gson = Gson()
        val jsonString = gson.toJson(this.weatherData)
        val sharedPreferences = applicationContext.getSharedPreferences(WEATHER_DATA, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LATEST_CURRENT_WEATHER_DATA, jsonString)
        editor.apply()
        mainPresenter!!.getHourWeather(latitude, longitude)
    }

    override fun onGetHourWeatherSuccess(hourWeatherData: JsonObject?) {
        this.hourWeatherData = hourWeatherData
        hourWeatherModelArrayList = this.hourWeatherData?.let { mainPresenter!!.createHourWeatherList(it) }
        val gson = Gson()
        val json = gson.toJson(hourWeatherModelArrayList)
        val sharedPreferences = getSharedPreferences(WEATHER_DATA, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LATEST_HOUR_WEATHER_DATA, json)
        editor.apply()
        for (city in cities) {
            mainPresenter!!.getCityWeather(city)
        }
        loadUI()
    }

    override fun setCurrentCity(currentCity: WeatherModel?) {
        var isExists = false
        if (cityWeatherModelArrayList!!.size > 0) for (weatherModel in cityWeatherModelArrayList!!) {
            if (weatherModel.time == currentCity?.time) {
                isExists = true
                break
            }
        }
        if (!isExists) currentCity?.let { cityWeatherModelArrayList!!.add(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUI() {
        val iconUrl = "http://openweathermap.org/img/w/" + weatherData!!["weather"].asJsonArray[0].asJsonObject["icon"].asString + ".png"
        Picasso.get().load(iconUrl).into(binding!!.imgIcon)
        val location = weatherData!!["name"].asString + ", " + weatherData!!["sys"].asJsonObject["country"].asString
        val mainWeather = weatherData!!["weather"].asJsonArray[0].asJsonObject["main"].asString
        val subWeather = weatherData!!["weather"].asJsonArray[0].asJsonObject["description"].asString
        val temp = ceil(weatherData!!["main"].asJsonObject["temp"].asFloat - 272.15).toString() + "\u00B0" + "C"
        val feel = "Feels like " + ceil(weatherData!!["main"].asJsonObject["feels_like"].asFloat - 272.15) + "\u00B0" + "C"
        val max = "Max: " + ceil((if (weatherData!!["main"].asJsonObject["temp_max"] == null) 0 else weatherData!!["main"].asJsonObject["temp_max"].asFloat - 272.15) as Double) + "\u00B0" + "C"
        val min = "Min: " + ceil((if (weatherData!!["main"].asJsonObject["temp_min"] == null) 0 else weatherData!!["main"].asJsonObject["temp_min"].asFloat - 272.15) as Double) + "\u00B0" + "C"
        val wind = "Wind: " + ceil((if (weatherData!!["wind"].asJsonObject["speed"] == null) 0 else weatherData!!["wind"].asJsonObject["speed"].asFloat * 3.6) as Double) + "km/h"
        val humidity = "Humidity: " + (if (weatherData!!["main"].asJsonObject["humidity"] == null) "" else weatherData!!["main"].asJsonObject["humidity"].asString) + "%"
        val visibility = "Visibility: " + (if (weatherData!!["visibility"] == null) 0 else weatherData!!["visibility"].asInt / 1000) + "km"
        val pressure = "Pressure: " + (if (weatherData!!["main"].asJsonObject["humidity"] == null) "" else weatherData!!["main"].asJsonObject["humidity"].asString) + "hPa"
        val seaLevel = "Sea: " + (if (weatherData!!["main"].asJsonObject["sea_level"] == null) "" else weatherData!!["main"].asJsonObject["sea_level"].asString) + "hPa"
        val groundLevel = "Ground: " + (if (weatherData!!["main"].asJsonObject["grnd_level"] == null) "" else weatherData!!["main"].asJsonObject["grnd_level"].asString) + "hPa"
        binding!!.tvLocation.text = location
        binding!!.tvMainWeather.text = mainWeather
        binding!!.tvSubWeather.text = subWeather
        binding!!.tvTemp.text = temp
        binding!!.tvFeel.text = feel
        binding!!.tvMax.text = max
        binding!!.tvMin.text = min
        binding!!.tvWind.text = wind
        binding!!.tvHumidity.text = humidity
        binding!!.tvVisibility.text = visibility
        binding!!.tvPressure.text = pressure
        binding!!.tvSeaLevel.text = seaLevel
        binding!!.tvGroundLevel.text = groundLevel
        val hourWeatherAdapter = hourWeatherModelArrayList?.let { HourWeatherAdapter(it) }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
        binding!!.rvHourWeather.layoutManager = layoutManager
        binding!!.rvHourWeather.adapter = hourWeatherAdapter
        val gson = Gson()
        val json = gson.toJson(cityWeatherModelArrayList)
        val sharedPreferences = getSharedPreferences(WEATHER_DATA, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LATEST_CITIES_WEATHER_DATA, json)
        editor.apply()
        val cityWeatherAdapter = cityWeatherModelArrayList?.let { CityWeatherAdapter(it) }
        val layoutManager1: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        binding!!.rvCity.layoutManager = layoutManager1
        binding!!.rvCity.adapter = cityWeatherAdapter
        latestUpdated = "Latest update: " + Utils.currentDateTimeFormatted
        editor.putString(LATEST_TIME_UPDATE, latestUpdated)
        editor.apply()
        binding!!.tvLatest.text = latestUpdated
        if (Utils.isNetworkAvailable(this)) progressDialog!!.dismiss()
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private val cities = arrayOf("New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne")
        private const val WEATHER_DATA = "WEATHER_DATA"
        private const val LATEST_CURRENT_WEATHER_DATA = "LATEST_CURRENT_WEATHER_DATA"
        private const val LATEST_HOUR_WEATHER_DATA = "LATEST_HOUR_WEATHER_DATA"
        private const val LATEST_CITIES_WEATHER_DATA = "LATEST_CITIES_WEATHER_DATA"
        private const val LATEST_TIME_UPDATE = "LATEST_TIME_UPDATE"
    }
}