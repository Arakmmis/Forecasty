package com.forecasty.view.forecast.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.forecasty.data.pojos.Forecast
import com.forecasty.databinding.ViewItemForecastBinding
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.getDayOfWeekAndTimeFromDate
import com.forecasty.util.getFormattedDate

class ForecastsAdapter(
    private var forecasts: List<Forecast> = emptyList(),
    private val prefsHelper: PrefsHelper
) : RecyclerView.Adapter<ForecastsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewItemForecastBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            prefsHelper
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(forecasts[position])
    }

    override fun getItemCount() = forecasts.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(forecasts: List<Forecast>) {
        this.forecasts = forecasts
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ViewItemForecastBinding,
        private val prefsHelper: PrefsHelper
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: Forecast) {
            with(binding) {
                val unit = prefsHelper.measurementUnit

                ivIcon.bind(forecast.weather?.firstOrNull()?.id ?: -1)

                tvTempMax.text =
                    String.format("%d%s", forecast.temp?.tempMax?.toInt(), unit.temp)

                tvTempMin.text =
                    String.format("%d%s", forecast.temp?.tempMin?.toInt(), unit.temp)

                tvDayTime.text = forecast.forecastDateTime?.getDayOfWeekAndTimeFromDate()
                tvDate.text = forecast.forecastDateTime?.getFormattedDate()

                tvWind.text = String.format("%.1f %s", forecast.wind?.speed, unit.velocity)
                tvHumidity.text = String.format("%d%%", forecast.temp?.humidity)
            }
        }
    }
}