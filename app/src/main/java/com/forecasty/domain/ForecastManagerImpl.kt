package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.ForecastManager.QueryTag
import com.forecasty.domain.local.ForecastDao
import com.forecasty.domain.remote.ForecastRepository
import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
import com.forecasty.util.SingleLiveEvent
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ForecastManagerImpl @Inject constructor(
    private val repo: ForecastRepository,
    private val dao: ForecastDao
) : ForecastManager {

    private var state: SingleLiveEvent<QueryState> = SingleLiveEvent()
    private var job = SupervisorJob()
    private val io: CoroutineContext = Dispatchers.IO
    private val scope = CoroutineScope(getJobErrorHandler() + io + job)
    private var retryQuery: (suspend () -> Any?)? = null

    override fun getQueryState(): LiveData<QueryState> = state

    override fun refresh() {
        job.cancelChildren()
    }

    override fun retryFailedQuery() {
        val prevQuery = retryQuery
        retryQuery = null

        scope.launch { prevQuery?.invoke() }
    }

    override suspend fun getCurrentWeather(
        query: Map<String, String>,
        queryTag: QueryTag
    ): CurrentDayForecast? {
        updateState(QueryState.LOADING)

        return when (queryTag) {
            QueryTag.CITY_NAME -> executeDbQuery(query) ?: executeApiQuery(query)

            else -> repo.getCurrentWeather(query)
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
        retryQuery = { executeDbQuery(query) }

        val cityName = query[CITY_NAME]

        val currentTime = System.currentTimeMillis()

        return runBlocking {
            val weatherForCity = dao.getCurrentWeather(cityName ?: "")

            val diffInMins = Duration
                .ofMillis(weatherForCity?.calculatedWeatherTimeStamp ?: 0L)
                .minus(Duration.ofMillis(currentTime))
                .toMinutes()

            Timber.d(
                ForecastManagerImpl::class.java.simpleName,
                "Diff between recorded weather time and now: $diffInMins mins"
            )

            if (diffInMins <= 5 && diffInMins < 0) {
                retryQuery = null
                updateState(QueryState.DONE)

                weatherForCity
            } else
                null
        }
    }

    private suspend fun executeApiQuery(query: Map<String, String>): CurrentDayForecast? {
        retryQuery = { executeApiQuery(query) }

        return runBlocking {
            val currentWeatherForCity = repo.getCurrentWeather(query)

            if (currentWeatherForCity != null) {
                retryQuery = null

                val list = dao.getAllWeather()

                val forecast = list.firstOrNull {
                    it.locationName == currentWeatherForCity.locationName
                }

                if (forecast != null)
                    dao.removeForecast(forecast)
                else if (list.size == 5)
                    dao.removeForecast(list.first())

                dao.insertForecast(currentWeatherForCity)

                updateState(QueryState.DONE)
            } else {
                updateState(QueryState.ERROR, "currentWeatherForCity is null")
            }

            currentWeatherForCity
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Timber.e(ForecastManagerImpl::class.simpleName, "An error happened: $e")
        updateState(QueryState.ERROR)
    }

    private fun updateState(state: QueryState, errMsg: String? = null) {
        errMsg?.let {
            Timber.e(ForecastManagerImpl::class.simpleName, errMsg)
        }

        this.state.postValue(state)
    }
}