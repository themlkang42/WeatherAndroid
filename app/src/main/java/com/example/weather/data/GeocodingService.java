package com.example.weather.data;

import com.example.weather.model.Location;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

// TODO: Possibly add support for zip code API
public interface GeocodingService {
    @GET("geo/1.0/direct?limit=10&appid=" + WeatherService.API_KEY)
    Single<List<Location>> locations(
            @Query("q") String query
    );
}