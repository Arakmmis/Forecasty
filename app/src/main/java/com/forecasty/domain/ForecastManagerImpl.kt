package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.local.DbConfig.Constants.DB_SIZE_LIMIT
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

    override suspend fun getCurrentWeather(
        query: Map<String, String>,
        queryType: QueryType
    ): CurrentDayForecast? {
        updateState(QueryState.LOADING)

        return when (queryType) {
            QueryType.ZIP_CODE -> executeApiQuery(query)
            else -> executeDbQuery(query, queryType) ?: executeApiQuery(query)
        }
    }

    override suspend fun getForecast(
        query: Map<String, String>
    ): ExtendedForecast? = repo.getForecast(query)

    override suspend fun addForecast(forecast: CurrentDayForecast) =
        dao.insertForecast(forecast)

    override suspend fun removeForecast(forecast: CurrentDayForecast) =
        dao.removeForecast(forecast)

    private suspend fun executeDbQuery(
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
            if (isTimeDiffValid(weatherForCity))
                weatherForCity
            else
                null
        } else
            null
    }

    private fun isTimeDiffValid(weather: CurrentDayForecast): Boolean {
        val diffInMins = Duration.between(
            weather.receivedWeatherDateTime,
            LocalDateTime.now()
        ).toMinutes()

        Timber.d(
            "${ForecastManagerImpl::class.java.simpleName} " +
                    "Diff between recorded weather time and now: $diffInMins mins"
        )

        return diffInMins in 0..5
    }

    private suspend fun executeApiQuery(query: Map<String, String>): CurrentDayForecast? {
        val currentWeatherForCity = repo.getCurrentWeather(query)

        if (currentWeatherForCity != null) {
            val list = dao.getAllWeather()

            val forecast = list.firstOrNull {
                it.id == currentWeatherForCity.id
            }

            if (forecast != null)
                removeForecast(forecast)
            else if (list.size == DB_SIZE_LIMIT)
                removeForecast(list.sortedBy { it.receivedWeatherDateTime }.first())

            addForecast(currentWeatherForCity)
        } else {
            updateState(QueryState.ERROR, "currentWeatherForCity is null")
        }

        return currentWeatherForCity
    }

    private fun updateState(state: QueryState, errMsg: String? = null) {
        errMsg?.let {
            Timber.e(ForecastManagerImpl::class.simpleName, errMsg)
        }

        this.state.postValue(state)
    }
}