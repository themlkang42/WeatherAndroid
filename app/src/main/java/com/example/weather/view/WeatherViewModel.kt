package com.example.weather.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.weather.data.GeocodingService
import com.example.weather.data.WeatherRepository
import com.example.weather.data.WeatherService
import com.example.weather.data.WeatherStorage
import com.example.weather.model.Location
import com.example.weather.model.Weather
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherViewModel(
    private val geocodingService: GeocodingService,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val textInputSubject = BehaviorSubject.create<String>()
    val textInput: Observable<String> = textInputSubject.hide()

    private val locationsSubject = BehaviorSubject.create<List<Location>>()
    val locations: Observable<List<Location>> = locationsSubject.hide()

    private val showLocationsSubject = BehaviorSubject.create<Boolean>()
    val showLocations: Observable<Boolean> = showLocationsSubject.hide()

    private val weatherSubject = BehaviorSubject.create<Weather>()
    val weather: Observable<Weather> = weatherSubject.hide()

    private val clickLocationSubject = PublishSubject.create<Location>()

    private val disposables = CompositeDisposable()

    init {
        val storedWeather = weatherRepository.storedWeather
        if (storedWeather != null) {
            weatherSubject.onNext(storedWeather)
        }

        textInputSubject
            .debounce(DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
            .switchMap { text ->
                if (text.isEmpty()) {
                    Observable.just(Result.success(emptyList()))
                } else {
                    // Wrap Single.error with a Result so the stream isn't cancelled from an error
                    geocodingService.locations(text)
                        .map { Result.success(it) }
                        .onErrorReturn { Result.failure(it) }
                        .toObservable()
                }

            }
            .subscribeBy(
                onNext = { result ->
                    result.fold(
                        onSuccess = {
                            locationsSubject.onNext(it)
                            showLocationsSubject.onNext(true)
                        },
                        onFailure = {
                            logError(it)
                        }
                    )
                },
                onError = {
                    logError(it)
                }
            )
            .addTo(disposables)

        clickLocationSubject
            .debounce(DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
            .switchMap { location ->
                // Wrap Single.error with a Result so the stream isn't cancelled from an error
                weatherRepository.weather(location.lat, location.lon)
                    .map { Result.success(it) }
                    .onErrorReturn { Result.failure(it) }
                    .toObservable()
            }
            .subscribeBy(
                onNext = { result ->
                    result.fold(
                        onSuccess = {
                            weatherSubject.onNext(it)
                            showLocationsSubject.onNext(false)
                        },
                        onFailure = {
                            logError(it)
                        }
                    )
                },
                onError = {
                    logError(it)
                }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun updateTextInput(text: String) {
        textInputSubject.onNext(text)
    }

    fun clickLocation(location: Location) {
        clickLocationSubject.onNext(location)
    }

    fun useDeviceLocation(location: android.location.Location) {
        clickLocationSubject.onNext(Location(
            "",
            location.latitude,
            location.longitude,
            "",
            null
        ))
    }

    private fun logError(throwable: Throwable) {
        Log.e(WeatherViewModel::class.java.name, "Stream threw error: ${throwable.message}")
    }

    companion object {
        const val DEBOUNCE_DELAY = 300L

        // TODO: Use Dagger or Hilt
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val retrofit = Retrofit.Builder()
                    .baseUrl(WeatherService.ROOT_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                val geocodingService: GeocodingService =
                    retrofit.create(GeocodingService::class.java)
                val weatherService: WeatherService =
                    retrofit.create(WeatherService::class.java)
                val weatherStorage = WeatherStorage()
                val weatherRepository = WeatherRepository(weatherService, weatherStorage)
                return WeatherViewModel(geocodingService, weatherRepository) as T
            }
        }
    }
}