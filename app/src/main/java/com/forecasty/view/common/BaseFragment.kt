package com.forecasty.view.common

import androidx.fragment.app.Fragment
import com.forecasty.R
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.domain.QueryState
import com.forecasty.util.MeasurementUnit

abstract class BaseFragment : Fragment() {

    fun observeData(vm: BaseViewModel, errorView: ErrorView) {
        vm.weatherData.observe(viewLifecycleOwner) {
            if (it.data != null) {
                updateQueryState(QueryState.DONE)
                updateUi(it.data)
            } else {
                updateQueryState(QueryState.ERROR)

                when (it.error) {
                    is NoSuchFieldException -> {
                        errorView.bind(ErrorView.StateType.EMPTY, e = it.error)
                    }
                    is NoSuchElementException -> {
                        errorView.bind(
                            ErrorView.StateType.OPERATIONAL,
                            desc = getString(R.string.err_location_not_found),
                            e = it.error
                        ) {
                            vm.getCurrentWeather()
                        }
                    }
                    else -> {
                        errorView.bind(ErrorView.StateType.OPERATIONAL, e = it.error) {
                            vm.getCurrentWeather()
                        }
                    }
                }
            }
        }

        vm.queryState.observe(viewLifecycleOwner) {
            updateQueryState(it)
        }

        vm.unitsState.observe(viewLifecycleOwner) {
            updateUnits(it)
        }
    }

    abstract fun updateQueryState(state: QueryState)

    abstract fun updateUi(forecast: CurrentDayForecast)

    abstract fun updateUnits(unit: MeasurementUnit)
}