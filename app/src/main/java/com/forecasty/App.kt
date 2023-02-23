package com.forecasty

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.time.LocalTime

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        setDarkMode()
    }

    private fun setDarkMode() {
        val now = LocalTime.now()

        val midnight = LocalTime
            .of(LocalTime.MIDNIGHT.hour, LocalTime.MIDNIGHT.minute)

        val noon = LocalTime
            .of(LocalTime.NOON.hour, LocalTime.NOON.minute)

        if (now.isAfter(midnight) && now.isBefore(noon))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}