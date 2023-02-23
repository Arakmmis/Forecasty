package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.local.DbConfig.Constants.CURRENT_WEATHER_TABLE_SIZE_LIMIT
import com.forecasty.domain.local.DbConfig.Constants.FORECASTS_TABLE_SIZE_LIMIT
import com.forecasty.domain.local.ForecastDao
import com.forecasty.domain.remote.ForecastRepository
import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
import com.forecasty.domain.remote.QueryHelper.Keys.LAT
import com.forecasty.domain.remote.QueryHelper.Keys.LON
import com.forecasty.util.QueryType
import com.forecasty.util.SingleLiveEvent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class ForecastManagerImpl @Inject constructor(
    private val repo: ForecastRepository,
    private val dao: ForecastDao
) : ForecastManager {

    override var state: SingleLiveEvent<QueryState> = SingleLiveEvent()

    private var job = SupervisorJob()

    override fun getQueryState(): LiveData<QueryState> = state

    override fun refresh() {
        job.cancelChildren()
    }

    private fun isTimeDiffValid(receivedWeatherDateTime: LocalDateTime?): Boolean {
        val diffInMins = Duration.between(
            receivedWeatherDateTime,
            LocalDateTime.now()
        ).toMinutes()

        Timber.d(
            "${ForecastManagerImpl::class.java.simpleName} " +
                    "Diff between recorded weather time and now: $diffInMins mins"
        )

        return diffInMins in 0..5
    }

    private fun updateState(state: QueryState, errMsg: String? = null) {
        errMsg?.let {
            Timber.e("${ForecastManagerImpl::class.simpleName} $errMsg")
        }

        this.state.postValue(state)
    }

    /**
     * CURRENT WEATHER
     */

    override suspend fun getCurrentWeather(
        query: Map<String, String>,
        queryType: QueryType,
        isUnitChanged: Boolean
    ): CurrentDayForecast? {
        updateState(QueryState.LOADING)

        return if (isUnitChanged)
            executeCurrentWeatherApiQuery(query)
        else
            when (queryType) {
                QueryType.ZIP_CODE -> executeCurrentWeatherApiQuery(query)
                else -> executeCurrentWeatherDbQuery(query, queryType)
                    ?: executeCurrentWeatherApiQuery(query)
            }
    }

    override suspend fun getCurrentWeatherLastSearchesList(limit: Int) =
        dao.getAllWeather().sortedBy { it.receivedWeatherDateTime }.takeLast(limit)

    private suspend fun executeCurrentWeatherDbQuery(
        query: Map<String, String>,
        queryType: QueryType
    ): CurrentDayForecast? {
        val weatherForCity: CurrentDayForecast?

        when (queryType) {
            QueryType.CITY_NAME ->
                weatherForCity = dao.getCurrentWeather(query[CITY_NAME] ?: "")

            QueryType.LAT_LON -> {
                val lat = query[LAT]
                val lon = query[LON]

                weatherForCity =
                    dao.getCurrentWeather(
                        lat = lat?.toDouble() ?: 0.0,
                        lon = lon?.toDouble() ?: 0.0
                    )
            }

            else -> return null
        }

        return if (weatherForCity != null) {
            if (isTimeDiffValid(weatherForCity.receivedWeatherDateTime))
                weatherForCity
            else
                null
        } else
            null
    }

    private suspend fun executeCurrentWeatherApiQuery(query: Map<String, String>): CurrentDayForecast? {
        val currentWeatherForCity = repo.getCurrentWeather(query)

        if (currentWeatherForCity != null) {
            val list = dao.getAllWeather()

            val forecast = list.firstOrNull {
                it.id == currentWeatherForCity.id
            }

            if (forecast != null)
                dao.removeCurrentWeather(forecast)
            else if (list.size == CURRENT_WEATHER_TABLE_SIZE_LIMIT)
                dao.removeCurrentWeather(list.sortedBy { it.receivedWeatherDateTime }.first())

            dao.insertCurrentWeather(currentWeatherForCity)
        } else {
            updateState(QueryState.ERROR, "currentWeatherForCity is null")
        }

        return currentWeatherForCity
    }

    /**
     * FORECASTS
     */

    override suspend fun getForecast(
        query: Map<String, String>,
        queryType: QueryType
    ): ExtendedForecast? {
        updateState(QueryState.LOADING)
        return executeForecastApiQuery(query)
    }

    override suspend fun getForecastsLastSearchesList(limit: Int) =
        dao.getAllForecasts().sortedBy { it.receivedWeatherDateTime }.takeLast(limit)

    private suspend fun executeForecastApiQuery(query: Map<String, String>): ExtendedForecast? {
        val extendedForecast = repo.getForecast(query)

        if (extendedForecast != null) {
            val list = dao.getAllForecasts()

            val forecast = list.firstOrNull {
                it.city?.id == extendedForecast.city?.id
            }

            if (forecast != null)
                dao.removeForecast(forecast)
            else if (list.size == FORECASTS_TABLE_SIZE_LIMIT)
                dao.removeForecast(list.sortedBy { it.receivedWeatherDateTime }.first())

            dao.insertForecast(extendedForecast)
        } else {
            updateState(QueryState.ERROR, "extendedForecast is null")
        }

        return extendedForecast
    }
}