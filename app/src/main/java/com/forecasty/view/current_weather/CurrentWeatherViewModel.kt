package com.forecasty.view.current_weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.forecasty.data.helpers.Status
import com.forecasty.data.helpers.Wrapper
import com.forecasty.domain.ForecastManager
import com.forecasty.domain.local.DbConfig.Constants.CURRENT_WEATHER_LAST_SEARCHED_LIMIT
import com.forecasty.domain.remote.QueryHelper
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.MeasurementUnit
import com.forecasty.util.QueryType
import com.forecasty.util.ValidationUtils.LAT_LON_REGEX
import com.forecasty.util.ValidationUtils.ZIP_CODE_REGEX
import com.forecasty.view.common.BaseCurrentWeatherViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    manager: ForecastManager,
    prefsHelper: PrefsHelper
) : BaseCurrentWeatherViewModel(manager, prefsHelper) {

    private val _previousSearches = MutableLiveData<List<Pair<String, String>>>()
    val previousSearches: LiveData<List<Pair<String, String>>> = _previousSearches

    fun getCurrentWeather(query: String) {
        try {
            val (searchTerm, queryType) = getQuery(query)

            getCurrentWeather(searchTerm, queryType)
        } catch (e: Exception) {
            _weatherData.postValue(
                Wrapper(
                    Status.ERROR,
                    null,
                    IllegalArgumentException("getCurrentWeather: err: ${e.message} query: $query")
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
            lon = prefsHelper.coordinates?.lon,
            isUnitChanged = true
        )
    }

    fun getPreviousSearches() {
        runBlocking {
            val list =
                manager.getCurrentWeatherLastSearchesList(CURRENT_WEATHER_LAST_SEARCHED_LIMIT)
                    ?.map {
                        if (it.searchTermUsed == it.locationName)
                            String.format("%s, %s", it.locationName, it.countryInfo?.name) to ""
                        else
                            (it.locationName ?: "") to (it.searchTermUsed ?: "")
                    } ?: emptyList()

            _previousSearches.postValue(list)
        }
    }

    private fun getQuery(query: String): Pair<Map<String, String>, QueryType> {
        return when {
            Pattern.matches(LAT_LON_REGEX, query) -> {
                val separated = query.trim().split(regex = Regex(" *, *"))

                if (separated.isEmpty() || separated.size != 2) {
                    _weatherData.postValue(
                        Wrapper(
                            Status.ERROR,
                            null,
                            IllegalArgumentException("getQuery: latlon is ill-formatted")
                        )
                    )
                }

                Pair(
                    QueryHelper.byLatLon(
                        lat = separated[0].toDouble(),
                        lon = separated[1].toDouble()
                    ),
                    QueryType.LAT_LON
                )
            }

            Pattern.matches(ZIP_CODE_REGEX, query) ->
                Pair(QueryHelper.byZipCode(query), QueryType.ZIP_CODE)

            else ->
                Pair(QueryHelper.byCityName(query), QueryType.CITY_NAME)
        }
    }
}