package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("name") val name: String,
    // TODO: @SerializedName("local_names")
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)