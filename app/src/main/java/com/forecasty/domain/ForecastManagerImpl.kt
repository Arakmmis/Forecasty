package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.local.DbConfig.Constants.DB_SIZE_LIMIT
import com.forecasty.domain.local.ForecastDao
import com.forecasty.domain.remote.ForecastRepository
import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
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
        queryTag: QueryType
    ): CurrentDayForecast? {
        updateState(QueryState.LOADING)

        return when (queryTag) {
            // TODO: Change requests to all go through DB before executing API request
            QueryType.CITY_NAME -> executeDbQuery(query) ?: executeApiQuery(query)

            else -> executeApiQuery(query)
        }
    }

    override suspend fun getForecast(
        query: Map<String, String>
    ): ExtendedForecast? = repo.getForecast(query)

    override suspend fun addForecast(forecast: CurrentDayForecast) =
        dao.insertForecast(forecast)

    override suspend fun removeForecast(forecast: CurrentDayForecast) =
        dao.removeForecast(forecast)

    private suspend fun executeDbQuery(query: Map<String, String>): CurrentDayForecast? {
        val cityName = query[CITY_NAME]

        val weatherForCity = dao.getCurrentWeather(cityName ?: "")

        return if (weatherForCity != null) {
            val diffInMins = Duration.between(
                weatherForCity.receivedWeatherDateTime,
                LocalDateTime.now()
            ).toMinutes()

            Timber.d(
                "${ForecastManagerImpl::class.java.simpleName} " +
                        "Diff between recorded weather time and now: $diffInMins mins"
            )

            if (diffInMins in 0..5)
                weatherForCity
            else
                null
        } else
            null
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