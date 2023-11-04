package com.example.weather.data;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.example.weather.WeatherApplication;
import com.example.weather.model.Weather;
import com.google.gson.Gson;

public class WeatherStorage {
    private final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherApplication.getAppContext());
    private final Gson gson = new Gson();

    private final String KEY_WEATHER = "KEY_WEATHER";

    public void putWeather(Weather weather) {
        String json = gson.toJson(weather);
        sharedPreferences.edit()
                .putString(KEY_WEATHER, json)
                .apply();
    }

    @Nullable
    public Weather getWeather() {
        String json = sharedPreferences.getString(KEY_WEATHER, null);
        if (json == null) {
            return null;
        } else {
            return gson.fromJson(json, Weather.class);
        }
    }
}
