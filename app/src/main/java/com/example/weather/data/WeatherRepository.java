package com.example.weather.data;

import androidx.annotation.Nullable;

import com.example.weather.model.Weather;

import io.reactivex.rxjava3.core.Single;

public class WeatherRepository {
    private final WeatherService weatherService;
    private final WeatherStorage weatherStorage;

    public WeatherRepository(WeatherService weatherService, WeatherStorage weatherStorage) {
        this.weatherService = weatherService;
        this.weatherStorage = weatherStorage;
    }

    public Single<Weather> weather(Double lat, Double lon) {
        return weatherService.weather(lat, lon)
                .doOnSuccess(weatherStorage::putWeather);
    }

    @Nullable
    public Weather getStoredWeather() {
        return weatherStorage.getWeather();
    }
}
