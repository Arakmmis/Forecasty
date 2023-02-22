package com.forecasty.view.current_weather

import com.forecasty.data.helpers.Status
import com.forecasty.data.helpers.Wrapper
import com.forecasty.domain.ForecastManager
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.MeasurementUnit
import com.forecasty.view.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    manager: ForecastManager,
    prefsHelper: PrefsHelper
) : BaseViewModel(manager, prefsHelper) {

    init {
        getCurrentWeather()
    }

    fun getCurrentWeather(latlon: String) {
        val separated = latlon.trim().split(regex = Regex(" *, *"))

        if (separated.isEmpty() || separated.size != 2) {
            _weatherData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    IllegalArgumentException("getCurrentWeather 0: latlon string is ill-formatted")
                )
            )

            return
        }

        try {
            getCurrentWeather(
                lat = separated[0].toDouble(),
                lon = separated[1].toDouble()
            )
        } catch (e: Exception) {
            _weatherData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    IllegalArgumentException("getCurrentWeather: latlon string is ill-formatted")
                )
            )
        }
    }

    fun switchMeasurementUnit() {
        when (prefsHelper.measurementUnit) {
            MeasurementUnit.METRIC -> {
                prefsHelper.measurementUnit = MeasurementUnit.IMPERIAL

            }
            MeasurementUnit.IMPERIAL -> {
                prefsHelper.measurementUnit = MeasurementUnit.METRIC
            }
        }

        getCurrentWeather(
            lat = prefsHelper.coordinates?.lat,
            lon = prefsHelper.coordinates?.lon
        )
    }

    // TODO: Request previous searches from ForecastManager
    fun getPreviousSearches(): List<String> =
        emptyList()
}