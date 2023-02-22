package com.forecasty.view.current_weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.forecasty.data.helpers.Status
import com.forecasty.data.helpers.Wrapper
import com.forecasty.data.pojos.Coordinates
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.domain.ForecastManager
import com.forecasty.domain.QueryState
import com.forecasty.domain.remote.QueryHelper
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.MeasurementUnit
import com.forecasty.util.QueryType
import com.forecasty.util.SingleLiveEvent
import com.forecasty.view.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val manager: ForecastManager,
    private val prefsHelper: PrefsHelper
) : BaseViewModel() {

    private var _weatherData = MutableLiveData<Wrapper<CurrentDayForecast>>()
    val weatherData: LiveData<Wrapper<CurrentDayForecast>> = _weatherData

    val queryState: LiveData<QueryState> = manager.state

    private var _unitsState = SingleLiveEvent<MeasurementUnit>()
    val unitsState: LiveData<MeasurementUnit> = _unitsState

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
                    IllegalArgumentException("getCurrentWeather 0: latlon string is ill-formatted")
                )
            )
        }
    }

    fun getCurrentWeather(
        lat: Double? = null,
        lon: Double? = null,
        cityName: String? = null,
        zipCode: String? = null
    ) {
        when {
            lat != null && lon != null -> {
                getCurrentWeather(
                    QueryHelper.byLatLon(
                        lat = lat,
                        lon = lon,
                        unit = prefsHelper.measurementUnit
                    ),
                    QueryType.LAT_LON
                )
            }

            cityName != null -> {
                getCurrentWeather(
                    QueryHelper.byCityName(
                        name = cityName,
                        unit = prefsHelper.measurementUnit
                    ),
                    QueryType.CITY_NAME
                )
            }

            zipCode != null -> {
                getCurrentWeather(
                    QueryHelper.byZipCode(
                        zipCode = zipCode,
                        unit = prefsHelper.measurementUnit
                    ),
                    QueryType.ZIP_CODE
                )
            }

            else -> {
                val lastQuery = prefsHelper.lastQuery

                if (lastQuery != null) {
                    val (query, queryType) = lastQuery
                    getCurrentWeather(query, queryType)

                    return
                }

                val coordinates = prefsHelper.coordinates

                if (coordinates != null
                    && coordinates.validateLatitude()
                    && coordinates.validateLongitude()
                )
                    getCurrentWeather(
                        QueryHelper.byLatLon(
                            lat = coordinates.lat!!,
                            lon = coordinates.lon!!,
                            unit = prefsHelper.measurementUnit
                        ),
                        QueryType.LAT_LON
                    )
                else
                    _weatherData.postValue(
                        Wrapper(
                            Status.ERROR,
                            null,
                            NoSuchFieldException("getCurrentWeather 1: location is null in Shared Prefs")
                        )
                    )
            }
        }
    }

    private fun getCurrentWeather(
        query: Map<String, String>,
        queryType: QueryType
    ) = viewModelScope.launch(main + job) {
        try {
            prefsHelper.lastQuery = Pair(query, queryType)

            val response =
                manager.getCurrentWeather(query, queryType)

            if (response != null) {
                _unitsState.postValue(prefsHelper.measurementUnit)

                _weatherData.postValue(
                    Wrapper(
                        Status.SUCCESS,
                        response,
                        null
                    )
                )

                try {
                    response.coordinates?.let {
                        prefsHelper.coordinates = Coordinates(
                            lat = it.lat!!,
                            lon = it.lon!!
                        )
                    }

                } catch (e: Exception) {
                    Timber.e(
                        "getCurrentWeather 2: " +
                                "Failed to save location to SP! Err msg: ${e.message}"
                    )
                }
            } else
                _weatherData.postValue(
                    Wrapper(
                        Status.ERROR,
                        null,
                        NoSuchElementException("getCurrentWeather 2: Location not found")
                    )
                )

        } catch (e: Exception) {
            _weatherData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    e
                )
            )
        }
    }

    fun refresh() {
        manager.refresh()

        val coordinates = prefsHelper.coordinates

        if (coordinates != null
            && coordinates.validateLatitude()
            && coordinates.validateLongitude()
        )
            getCurrentWeather(
                lat = coordinates.lat,
                lon = coordinates.lon
            )
        else
            _weatherData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    NoSuchFieldException("onRefresh: location is null in Shared Prefs")
                )
            )
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

    fun getPreviousSearches(): List<String> =
        emptyList()
}