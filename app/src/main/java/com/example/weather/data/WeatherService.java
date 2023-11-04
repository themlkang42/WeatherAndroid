package com.example.weather.data;

import com.example.weather.model.Weather;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("data/2.5/weather?units=imperial&appid=" + API_KEY)
    Single<Weather> weather(
            @Query("lat") Double lat,
            @Query("lon") Double lon
    );

    String ROOT_URL = "https://api.openweathermap.org";
    String API_KEY = "ad7ca150620ec70ca2a0b582c6a69ba3";
}