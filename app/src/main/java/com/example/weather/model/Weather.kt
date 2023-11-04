package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("coord") val coord: WeatherCoord,
    @SerializedName("weather") val conditions: List<WeatherCondition>,
    @SerializedName("main") val main: WeatherMain,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("name") @Deprecated("field is deprecated in the API") val name: String
    // TODO: @SerializedName("rain")
    // TODO: @SerializedName("snow")
)

data class WeatherCoord(
    @SerializedName("log") val log: Double,
    @SerializedName("lat") val lat: Double
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WeatherMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int
)

data class Clouds(
    @SerializedName("all") val all: Int
)