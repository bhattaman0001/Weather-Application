package com.example.weatherapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.presenter.MainInterface
import com.example.weatherapp.presenter.MainPresenter
import com.example.weatherapp.presenter.Utils.CURRENT_CITY
import com.example.weatherapp.presenter.Utils.LATEST_CURRENT_HOUR_WEATHER_DATA
import com.example.weatherapp.presenter.Utils.LATEST_CURRENT_WEATHER_DATA
import com.example.weatherapp.presenter.Utils.REQUEST_LOCATION
import com.example.weatherapp.presenter.Utils.WEATHER_DATA
import com.example.weatherapp.presenter.Utils.isNetworkAvailable
import com.example.weatherapp.presenter.Utils.sharedPreferences
import com.example.weatherapp.view.adapter.WeatherAdapter
import com.google.android.material.navigation.NavigationView
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MainInterface {

    private var latitude: String? = null
    private val scale = 0.5f
    private var longitude: String? = null
    private var locationManager: LocationManager? = null
    private lateinit var binding: ActivityMainBinding
    private var mainPresenter: MainPresenter? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var toolbar: Toolbar? = null
    private var labelView: TextView? = null
    private var contentView: LinearLayout? = null
    private lateinit var weatherAdapter: WeatherAdapter
    private var weatherData: JsonObject? = null
    private var hourWeatherData: JsonObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        toolbar = binding.toolbar
        contentView = binding.content
        sharedPreferences = applicationContext.getSharedPreferences(WEATHER_DATA, MODE_PRIVATE)

        weatherAdapter = WeatherAdapter(this)
        weatherAdapter.bind(binding.cardView)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close)

        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        toolbar?.navigationIcon = DrawerArrowDrawable(this)
        toolbar?.setNavigationOnClickListener {
            if (navigationView?.let { drawerLayout?.isDrawerOpen(it) } == true) {
                navigationView?.let { drawerLayout?.closeDrawer(it) }
            } else {
                navigationView?.let { drawerLayout?.openDrawer(it) }
            }
        }

        drawerLayout?.setScrimColor(Color.TRANSPARENT)
        drawerLayout?.addDrawerListener(object : SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                labelView?.visibility = if (slideOffset > 0) View.VISIBLE else View.GONE
                val diffScaledOffset: Float = slideOffset * (1 - scale)
                val offsetScale = 1 - diffScaledOffset
                contentView?.scaleX = offsetScale
                contentView?.scaleY = offsetScale
                val xOffset = drawerView.width * slideOffset
                val xOffsetDiff: Float = contentView!!.width * diffScaledOffset / 2
                val xTranslation = xOffset - xOffsetDiff
                contentView?.translationX = xTranslation
            }

            override fun onDrawerClosed(drawerView: View) {
                labelView?.visibility = View.GONE
            }
        })

        mainPresenter = MainPresenter(this)

        binding.button.setOnClickListener { mainPresenter?.getCityWeather(binding.searchCity.text.toString()) }
    }

    private fun jsonStringToJsonObject(jsonString: String?): JsonObject? {
        if (jsonString.isNullOrEmpty()) {
            return null
        }
        return try {
            JsonParser.parseString(jsonString).asJsonObject
        } catch (e: IllegalStateException) {
            null
        }
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            if (isNetworkAvailable(this@MainActivity)) loadData()
            else {
                weatherData = jsonStringToJsonObject(sharedPreferences.getString(LATEST_CURRENT_WEATHER_DATA, ""))
                hourWeatherData = jsonStringToJsonObject(sharedPreferences.getString(LATEST_CURRENT_HOUR_WEATHER_DATA, ""))
                if (weatherData != null && hourWeatherData != null) weatherData?.let { hourWeatherData?.let { it1 -> weatherAdapter.updateData(it, it1) } }
                else Toast.makeText(this@MainActivity, "No Internet", Toast.LENGTH_SHORT).show()
            }
        }

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                loadData()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Toast.makeText(this@MainActivity, "No Internet", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadData() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            onGPS()
        } else {
            location
        }
    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(this)
        builder
            .setMessage("Enable GPS")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, which ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.cancel()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private val location: Unit
        get() {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            } else {
                val locationGPS = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (locationGPS != null) {
                    val lat = locationGPS.latitude
                    val lon = locationGPS.longitude
                    latitude = lat.toString()
                    longitude = lon.toString()
                    mainPresenter?.getCurrentWeather(latitude, longitude)
                } else {
                    // network not available
                    Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onGetCurrentWeatherSuccess(weatherData: JsonObject?) {
        val editor = sharedPreferences.edit()
        editor.putString(CURRENT_CITY, weatherData?.get("name")?.asString)
        editor.apply()
        mainPresenter?.getHourWeather(weatherData, latitude, longitude)
    }

    override fun onGetHourWeatherSuccess(weatherData: JsonObject?, hourWeatherData: JsonObject?) {

        val editor = sharedPreferences.edit()
        editor.putString(LATEST_CURRENT_WEATHER_DATA, weatherData.toString())
        editor.putString(LATEST_CURRENT_HOUR_WEATHER_DATA, hourWeatherData.toString())
        editor.apply()

        weatherData?.let { hourWeatherData?.let { it1 -> weatherAdapter.updateData(it, it1) } }

        navigationView?.setNavigationItemSelectedListener { menuItem ->
            var city = menuItem.toString()
            when (city) {
                "Home" -> {
                    city = sharedPreferences.getString(CURRENT_CITY, "").toString()
                    mainPresenter?.getCityWeather(city)
                }

                "Developer" -> {
                    val uri = Uri.parse("https://www.linkedin.com/in/iamamanbhatt/")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }

                "Apk" -> {
                    val uri = Uri.parse("https://drive.google.com/file/d/1PEjGMiF1icWnSzW6TK1Pdi3OoMSLadwU/view?usp=sharing.example.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }

                else -> {
                    mainPresenter?.getCityWeather(city)
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun setCurrentCity(weatherData: JsonObject?) {
        val longitude = weatherData?.get("coord")?.asJsonObject?.get("lon")?.asString
        val latitude = weatherData?.get("coord")?.asJsonObject?.get("lat")?.asString
        mainPresenter?.getHourWeather(weatherData, latitude, longitude)
    }
}