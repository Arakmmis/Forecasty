package com.forecasty.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.forecasty.R
import com.forecasty.databinding.ViewWeatherIconBinding
import com.forecasty.util.loadImage

class WeatherIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewWeatherIconBinding

    init {
        binding = ViewWeatherIconBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun bind(weatherId: Int) {
        with(binding) {
            when {
                weatherId < WeatherType.THUNDERSTORMS.maxId ->
                    ivIcon.loadImage(WeatherType.THUNDERSTORMS.iconId)

                weatherId < WeatherType.DRIZZLE.maxId ->
                    ivIcon.loadImage(WeatherType.DRIZZLE.iconId)

                weatherId < WeatherType.RAIN.maxId ->
                    ivIcon.loadImage(WeatherType.RAIN.iconId)

                weatherId < WeatherType.SNOW.maxId ->
                    ivIcon.loadImage(WeatherType.SNOW.iconId)

                weatherId < WeatherType.CLEAR.maxId ->
                    ivIcon.loadImage(WeatherType.CLEAR.iconId)

                weatherId < WeatherType.CLOUDS.maxId ->
                    ivIcon.loadImage(WeatherType.CLOUDS.iconId)

                else ->
                    ivIcon.loadImage(WeatherType.CLEAR.iconId)
            }
        }
    }

    enum class WeatherType(minId: Int, val maxId: Int, @DrawableRes val iconId: Int) {
        THUNDERSTORMS(200, 300, R.drawable.ic_thunderstorm),
        DRIZZLE(300, 400, R.drawable.ic_drizzle),
        RAIN(500, 600, R.drawable.ic_rainy),
        SNOW(600, 700, R.drawable.ic_snow),
        CLEAR(800, 801, R.drawable.ic_clear),
        CLOUDS(801, 810, R.drawable.ic_cloudy)
    }
}