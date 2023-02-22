package com.forecasty.view.home

import com.forecasty.domain.ForecastManager
import com.forecasty.prefs.PrefsHelper
import com.forecasty.view.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    manager: ForecastManager,
    prefsHelper: PrefsHelper
) : BaseViewModel(manager, prefsHelper) {

    init {
        getCurrentWeather()
    }
}