package com.example.weather

import com.example.weather.data.GeocodingService
import com.example.weather.data.WeatherRepository
import com.example.weather.model.Clouds
import com.example.weather.model.Location
import com.example.weather.model.Weather
import com.example.weather.model.WeatherCondition
import com.example.weather.model.WeatherCoord
import com.example.weather.model.WeatherMain
import com.example.weather.model.Wind
import com.example.weather.view.WeatherViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class TestWeatherViewModel {
    private lateinit var viewModel: WeatherViewModel

    private lateinit var mockGeocodingService: GeocodingService
    private lateinit var mockWeatherRepository: WeatherRepository

    private lateinit var testScheduler: TestScheduler

    private val fakeWeather = Weather(
        WeatherCoord(1.0, 2.0),
        listOf(WeatherCondition(500, "Rain", "light rain", "10n")),
        WeatherMain(68.0, 67.0, 65.0, 69.0, 100, 50),
        10000,
        Wind(10.0, 180),
        Clouds(2),
        "Palo Alto"
    )
    private val fakeStoredWeather = fakeWeather.copy(name="Los Angelos")
    private val fakeLocation = Location(
        "San Francisco",
        3.0,
        4.0,
        "US",
        "CA"
    )

    @Before
    fun setup() {
        RxJavaPlugins.reset()

        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        mockGeocodingService = object : GeocodingService {
            override fun locations(query: String?): Single<List<Location>> {
                return Single.just(listOf(fakeLocation))
            }
        }
        mockWeatherRepository = mock<WeatherRepository>().apply {
            whenever(weather(any(), any()))
                .thenReturn(Single.just(fakeWeather))
            whenever(storedWeather)
                .thenReturn(fakeStoredWeather)
        }

        viewModel = WeatherViewModel(mockGeocodingService, mockWeatherRepository)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun test_weather_initialized_from_storage() {
        val weatherObserver = viewModel.weather.test()
        weatherObserver.assertValue(fakeStoredWeather)
    }

    @Test
    fun test_update_text_input() {
        val textInputObserver = viewModel.textInput.test()
        val locationsObserver = viewModel.locations.test()
        val showLocationsObserver = viewModel.showLocations.test()

        val input = "San Mateo"
        viewModel.updateTextInput(input)
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        textInputObserver.assertValue(input)
        locationsObserver.assertValue(listOf(fakeLocation))
        showLocationsObserver.assertValue(true)
    }

    @Test
    fun test_update_with_empty_input() {
        val textInputObserver = viewModel.textInput.test()
        val locationsObserver = viewModel.locations.test()

        viewModel.updateTextInput("")
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        textInputObserver.assertValue("")
        locationsObserver.assertValue(emptyList())
    }

    @Test
    fun test_update_input_twice() {
        val textInputObserver = viewModel.textInput.test()
        val locationsObserver = viewModel.locations.test()
        val showLocationsObserver = viewModel.showLocations.test()

        val input = "San Mateo"
        val input2 = "Palo Alto"
        viewModel.updateTextInput(input)
        viewModel.updateTextInput(input2)
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        textInputObserver.assertValues(input, input2)
        locationsObserver.assertValue(listOf(fakeLocation))
        showLocationsObserver.assertValue(true)
    }

    @Test
    fun test_click_location() {
        val weatherObserver = viewModel.weather.test()
        val showLocationsObserver = viewModel.showLocations.test()

        viewModel.clickLocation(fakeLocation)
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        weatherObserver.assertValues(fakeStoredWeather, fakeWeather)
        showLocationsObserver.assertValue(false)
    }

    @Test
    fun test_click_location_twice() {
        val weatherObserver = viewModel.weather.test()
        val showLocationsObserver = viewModel.showLocations.test()

        viewModel.clickLocation(fakeLocation)
        viewModel.clickLocation(fakeLocation)
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        verify(mockWeatherRepository, times(1)).weather(any(), any())
        weatherObserver.assertValues(fakeStoredWeather, fakeWeather)
        showLocationsObserver.assertValue(false)
    }

    @Test
    fun test_use_device_location() {
        val weatherObserver = viewModel.weather.test()
        val showLocationsObserver = viewModel.showLocations.test()

        viewModel.useDeviceLocation(android.location.Location(""))
        testScheduler.advanceTimeBy(WeatherViewModel.DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)

        weatherObserver.assertValues(fakeStoredWeather, fakeWeather)
        showLocationsObserver.assertValue(false)
    }
}