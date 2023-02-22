package com.forecasty.view.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.*
import timber.log.Timber

abstract class BaseViewModel(
    private val manager: ForecastManager,
    protected val prefsHelper: PrefsHelper
) : ViewModel() {

    private val job = SupervisorJob()
    private val main: CoroutineDispatcher = Dispatchers.Main

    protected var _weatherData = MutableLiveData<Wrapper<CurrentDayForecast>>()
    val weatherData: LiveData<Wrapper<CurrentDayForecast>> = _weatherData

    private var _unitsState = SingleLiveEvent<MeasurementUnit>()
    val unitsState: LiveData<MeasurementUnit> = _unitsState

    val queryState: LiveData<QueryState> = manager.state

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
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

            else -> getLastSearchedLocation()
        }
    }

    fun getCurrentWeather(
        query: Map<String, String>,
        queryType: QueryType
    ) {
        viewModelScope.launch(main + job) {
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
                            "getCurrentWeather: " +
                                    "Failed to save location to SP! Err msg: ${e.message}"
                        )
                    }
                } else
                    _weatherData.postValue(
                        Wrapper(
                            Status.ERROR,
                            null,
                            NoSuchElementException("getCurrentWeather: Location not found")
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
    }

    fun refresh() {
        manager.refresh()
        getLastSearchedLocation()
    }

    private fun getLastSearchedLocation() {
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
                    NoSuchFieldException("getCurrentWeather: location is null in Shared Prefs")
                )
            )
    }
}