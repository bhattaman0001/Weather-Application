package com.example.weatherapp.network

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiHelper {
    @GET("weather")
    fun getCurrentWeather(@Query("lat") lat: String?, @Query("lon") lon: String?, @Query("appid") apikey: String?): Call<JsonObject?>?

    @GET("weather")
    fun getWeatherByCity(@Query("q") city: String?, @Query("appid") apikey: String?): Call<JsonObject?>?

    @GET("forecast")
    fun getHourWeather(@Query("lat") lat: String?, @Query("lon") lon: String?, @Query("appid") apikey: String?): Call<JsonObject?>?

    companion object {
        private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val builder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
        val apiHelper: ApiHelper = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(builder.build())
            .build()
            .create(ApiHelper::class.java)
    }
}
