package com.forecasty.util

import androidx.annotation.IdRes
import com.forecasty.R

enum class QueryType(@IdRes val resId: Int) {
    CITY_NAME(R.id.rbCity),
    ZIP_CODE(R.id.rbZip),
    LAT_LON(R.id.rbLatLon)
}