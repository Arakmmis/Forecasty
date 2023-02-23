package com.forecasty.view.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forecasty.data.helpers.Status
import com.forecasty.data.helpers.Wrapper
import com.forecasty.data.pojos.Coordinates
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.ForecastManager
import com.forecasty.domain.QueryState
import com.forecasty.domain.local.DbConfig
import com.forecasty.domain.remote.QueryHelper
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.QueryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val manager: ForecastManager,
    private val prefsHelper: PrefsHelper
) : ViewModel() {

    private val job = SupervisorJob()
    private val main: CoroutineDispatcher = Dispatchers.Main

    private var _forecastData = MutableLiveData<Wrapper<ExtendedForecast>>()
    val forecastData: LiveData<Wrapper<ExtendedForecast>> = _forecastData

    private val _previousSearches = MutableLiveData<List<Pair<String, String>>>()
    val previousSearches: LiveData<List<Pair<String, String>>> = _previousSearches

    val queryState: LiveData<QueryState> = manager.state

    private var _currentFilter = MutableLiveData(Filter.ALL)
    val currentFilter: LiveData<Filter> = _currentFilter

    fun getForecast(latlon: String) {
        val separated = latlon.trim().split(regex = Regex(" *, *"))

        if (separated.isEmpty() || separated.size != 2) {
            _forecastData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    IllegalArgumentException("getForecast: latlon string is ill-formatted")
                )
            )

            return
        }

        try {
            getForecast(
                lat = separated[0].toDouble(),
                lon = separated[1].toDouble()
            )
        } catch (e: Exception) {
            _forecastData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    IllegalArgumentException("getForecast: latlon string is ill-formatted")
                )
            )
        }
    }

    fun getForecast(
        lat: Double? = null,
        lon: Double? = null,
        cityName: String? = null,
        zipCode: String? = null
    ) {
        when {
            lat != null && lon != null -> {
                getForecast(
                    QueryHelper.byLatLon(
                        lat = lat,
                        lon = lon,
                        unit = prefsHelper.measurementUnit
                    ),
                    QueryType.LAT_LON
                )
            }

            cityName != null -> {
                getForecast(
                    QueryHelper.byCityName(
                        name = cityName,
                        unit = prefsHelper.measurementUnit
                    ),
                    QueryType.CITY_NAME
                )
            }

            zipCode != null -> {
                getForecast(
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

    private fun getForecast(
        query: Map<String, String>,
        queryType: QueryType
    ) {
        viewModelScope.launch(main + job) {
            try {
                prefsHelper.lastQuery = Pair(query, queryType)

                val response =
                    manager.getForecast(query, queryType)

                if (response != null) {
                    _forecastData.postValue(
                        Wrapper(
                            Status.SUCCESS,
                            response,
                            null
                        )
                    )

                    try {
                        response.city?.coordinates?.let {
                            prefsHelper.coordinates = Coordinates(
                                lat = it.lat!!,
                                lon = it.lon!!
                            )
                        }

                    } catch (e: Exception) {
                        Timber.e(
                            "getForecast: " +
                                    "Failed to save location to SP! Err msg: ${e.message}"
                        )
                    }
                } else
                    _forecastData.postValue(
                        Wrapper(
                            Status.ERROR,
                            null,
                            NoSuchElementException("getForecast: Location not found")
                        )
                    )

            } catch (e: Exception) {
                _forecastData.postValue(
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
        val lastQuery = prefsHelper.lastForecastQuery ?: prefsHelper.lastQuery

        if (lastQuery != null) {
            val (query, queryType) = lastQuery
            getForecast(query, queryType)

            return
        }

        val coordinates = prefsHelper.forecastCoordinates ?: prefsHelper.coordinates

        if (coordinates != null
            && coordinates.validateLatitude()
            && coordinates.validateLongitude()
        )
            getForecast(
                QueryHelper.byLatLon(
                    lat = coordinates.lat!!,
                    lon = coordinates.lon!!,
                    unit = prefsHelper.measurementUnit
                ),
                QueryType.LAT_LON
            )
        else
            _forecastData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    NoSuchFieldException("getLastSearchedLocation: location is null in Shared Prefs")
                )
            )
    }

    fun getPreviousSearches() {
        runBlocking {
            val list =
                manager.getForecastsLastSearchesList(DbConfig.Constants.FORECASTS_LAST_SEARCHED_LIMIT)
                    ?.map {
                        if (it.searchTermUsed == it.city?.name)
                            String.format("%s, %s", it.city?.name, it.city?.country) to ""
                        else
                            (it.city?.name ?: "") to (it.searchTermUsed ?: "")
                    } ?: emptyList()

            _previousSearches.postValue(list)
        }
    }

    fun filterResults() {
        _currentFilter.value = when (currentFilter.value) {
            Filter.ALL -> Filter.TWENTY_FOUR
            Filter.TWENTY_FOUR -> Filter.FORTY_EIGHT
            Filter.FORTY_EIGHT -> Filter.ALL
            else -> Filter.ALL
        }
    }

    enum class Filter(val count: Int) {
        ALL(Int.MAX_VALUE),
        TWENTY_FOUR(8),
        FORTY_EIGHT(16)
    }
}