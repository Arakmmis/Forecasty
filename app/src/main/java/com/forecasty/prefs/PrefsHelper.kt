package com.forecasty.prefs

import android.content.Context
import androidx.core.content.edit
import com.forecasty.data.pojos.Coordinates
import com.forecasty.util.GsonUtils
import com.forecasty.util.MeasurementUnit
import com.forecasty.util.QueryType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.forecasty.prefs.PrefsKeys as Keys

@Singleton
class PrefsHelper @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(Keys.PREFS_NAME, Context.MODE_PRIVATE)

    var measurementUnit: MeasurementUnit
        set(value) =
            prefs.edit {
                putString(Keys.MEASUREMENT_UNIT, GsonUtils.unitToJson(value))
            }
        get() =
            GsonUtils.getUnitFromString(
                prefs.getString(Keys.MEASUREMENT_UNIT, "") ?: ""
            ) ?: MeasurementUnit.METRIC

    var coordinates: Coordinates?
        set(value) =
            prefs.edit {
                putString(Keys.LAST_SEARCHED_LOCATION, GsonUtils.coordinatesToJson(value!!))
            }
        get() =
            GsonUtils.getCoordinatesFromString(
                prefs.getString(Keys.LAST_SEARCHED_LOCATION, "") ?: ""
            )

    var lastQuery: Pair<Map<String, String>, QueryType>?
        set(value) =
            prefs.edit {
                putString(Keys.LAST_QUERY, GsonUtils.query(value!!))
            }
        get() =
            GsonUtils.getQueryFromString(
                prefs.getString(Keys.LAST_QUERY, "") ?: ""
            )
}