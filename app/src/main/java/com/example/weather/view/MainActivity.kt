package com.example.weather.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weather.R
import com.example.weather.model.Location
import com.example.weather.model.Weather
import com.example.weather.ui.theme.WeatherTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels { WeatherViewModel.Factory }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object: LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                viewModel.useDeviceLocation(location)
                locationManager.removeUpdates(this)
            }
            override fun onLocationChanged(locations: MutableList<android.location.Location>) {
                super.onLocationChanged(locations)
            }
            override fun onFlushComplete(requestCode: Int) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, mainExecutor) {
                    viewModel.useDeviceLocation(it)
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            }
        } else if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, mainExecutor) {
                    viewModel.useDeviceLocation(it)
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Many options for improving location permissions request
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            WeatherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }

    @Composable
    fun WeatherScreen(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
        ) {
            val textState by viewModel.textInput.subscribeAsState("")
            TextField(
                value = textState,
                onValueChange = { viewModel.updateTextInput(it) },
                placeholder = { Text(stringResource(R.string.search_for_a_city)) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
            ) {
                val weather by viewModel.weather.subscribeAsState(null)
                weather?.let {
                    WeatherInformation(it)
                }
                LocationsDropdown()
            }
        }
    }

    @Composable
    fun WeatherInformation(weather: Weather, modifier: Modifier = Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
        ) {
            Text(
                weather.name,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            Text(weather.main.temp.roundToInt().toString() + "\u2109")
            weather.conditions.firstOrNull()?.let { condition ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                ) {
                    Text(condition.description.capitalize(Locale.current))
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${condition.icon}@2x.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun LocationsDropdown(modifier: Modifier = Modifier) {
        val locations by viewModel.locations.subscribeAsState(emptyList())
        val showLocations by viewModel.showLocations.subscribeAsState(false)
        if (showLocations) {
            LazyColumn(
                modifier = modifier
                    .background(Color.LightGray),
            ) {
                items(locations) { location ->
                    LocationItem(location)
                }
            }
        }
    }

    @Composable
    fun LocationItem(location: Location, modifier: Modifier = Modifier) {
        var text = location.name
        if (location.state != null) {
            text += ", " + location.state
        }
        text += ", " + location.country

        Text(
            text,
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.clickLocation(location)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}