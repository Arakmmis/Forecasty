package com.forecasty.view.home

import com.forecasty.domain.ForecastManager
import com.forecasty.prefs.PrefsHelper
import com.forecasty.view.common.BaseCurrentWeatherViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    manager: ForecastManager,
    prefsHelper: PrefsHelper
) : BaseCurrentWeatherViewModel(manager, prefsHelper)