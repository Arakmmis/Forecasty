package com.forecasty.view.home

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.forecasty.R
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.databinding.FragHomeBinding
import com.forecasty.domain.QueryState
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.MeasurementUnit
import com.forecasty.view.MainActivity
import com.forecasty.view.common.BaseFragment
import com.forecasty.view.common.ErrorView
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(), OnRefreshListener {

    private var _binding: FragHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    @Inject
    lateinit var prefsHelper: PrefsHelper

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupMenu()
        observeData(vm, binding.errorView)
    }

    private fun setupViews() {
        with(binding) {
            (requireActivity() as MainActivity).setSupportActionBar(toolbar)
            (requireActivity() as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

            errorView.bind(ErrorView.StateType.NO_ERROR)
            swipe.setOnRefreshListener(this@HomeFragment)

            layoutWeather.tvNextDays.setOnClickListener {
                // Navigate to forecast screen
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_go_to_current_weather -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToCurrentWeatherFragment()
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun updateUi(forecast: CurrentDayForecast) {
        with(binding.layoutWeather) {
            tvDesc.text = forecast.weather?.firstOrNull()?.description

            ivIcon.bind(forecast.weather?.firstOrNull()?.id ?: -1)

            tvLocation.text =
                String.format("%s, %s", forecast.locationName, forecast.countryInfo?.name)

            tvTemp.text =
                String.format("%d", forecast.temp?.temp?.toInt())

            tvFeelsLike.text =
                String.format(
                    "%s %d",
                    getString(R.string.feels_like),
                    forecast.temp?.feelsLike?.toInt()
                )

            tvDayOfWeek.text = LocalDate.now().dayOfWeek.name
            tvDay.text = LocalDate.now().dayOfMonth.toString()
            tvMonth.text = LocalDate.now().month.name

            tvWindValue.text = String.format("%.1f", forecast.wind?.speed)
            tvHumidityValue.text = String.format("%d%%", forecast.temp?.humidity)
        }
    }

    override fun updateUnits(unit: MeasurementUnit) {
        with(binding.layoutWeather) {
            tvTempUnit.text = unit.temp
            tvFeelsLikeUnit.text = unit.temp
            tvWindUnit.text = unit.velocity
        }
    }

    override fun updateQueryState(state: QueryState) {
        with(binding) {
            when (state) {
                QueryState.LOADING -> {
                    swipe.isRefreshing = true
                    layoutWeather.clContainer.visibility = View.GONE
                    errorView.visibility = View.GONE
                }

                QueryState.ERROR -> {
                    swipe.isRefreshing = false
                    layoutWeather.clContainer.visibility = View.GONE
                    errorView.visibility = View.VISIBLE
                    errorView.bind(ErrorView.StateType.CONNECTION) {
                        onRefresh()
                    }
                }

                QueryState.DONE -> {
                    swipe.isRefreshing = false
                    layoutWeather.clContainer.visibility = View.VISIBLE
                    errorView.visibility = View.GONE
                    errorView.bind(ErrorView.StateType.NO_ERROR)
                }
            }
        }
    }

    override fun onRefresh() {
        vm.refresh()
    }
}