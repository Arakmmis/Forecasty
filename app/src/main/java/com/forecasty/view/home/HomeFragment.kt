package com.forecasty.view.home

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.forecasty.R
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.databinding.FragHomeBinding
import com.forecasty.domain.QueryState
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.MeasurementUnit
import com.forecasty.util.queryUserLocation
import com.forecasty.util.requestLocationPermission
import com.forecasty.util.showPermissionDialog
import com.forecasty.view.MainActivity
import com.forecasty.view.common.ErrorView
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), OnRefreshListener {

    private var _binding: FragHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    @Inject
    lateinit var prefsHelper: PrefsHelper

    private lateinit var locationPermissionRequestLauncher: ActivityResultLauncher<String>

    private val locationResultBlock = { isGranted: Boolean ->
        if (isGranted) {
            queryUserLocation(
                onSuccess = {
                    vm.getCurrentWeather(lat = it?.latitude, lon = it?.longitude)
                },
                onError = {
                    binding.errorView.bind(
                        ErrorView.StateType.OPERATIONAL,
                        e = it
                    )
                }
            )
        } else {
            showPermissionDialog()

            updateQueryState(QueryState.ERROR)
            binding.errorView.bind(
                ErrorView.StateType.EMPTY,
                e = IllegalStateException("locationResultBlock: Location denied but not permanently")
            )
        }
    }

    override fun onDestroy() {
        locationPermissionRequestLauncher.unregister()
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragHomeBinding.inflate(inflater, container, false)

        locationPermissionRequestLauncher = requestLocationPermission(locationResultBlock)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupMenu()
        observeData()
        findUserLocation()
    }

    private fun setupViews() {
        with(binding) {
            (requireActivity() as MainActivity).setSupportActionBar(toolbar)
            (requireActivity() as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

            errorView.bind(ErrorView.StateType.NO_ERROR)
            swipe.setOnRefreshListener(this@HomeFragment)

            tvNextDays.setOnClickListener {
                // Navigate to forecast screen
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)

                val changeUnitItem = menu.findItem(R.id.action_change_unit)
                changeUnitItem.title = String.format(
                    "%s %s",
                    getString(R.string.switch_units),
                    prefsHelper.measurementUnit.otherValue
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_location -> {
                        findUserLocation()
                        true
                    }

                    R.id.action_search -> {
                        // TODO: Navigate to Current Weather Screen
                        true
                    }

                    R.id.action_change_unit -> {
                        vm.switchMeasurementUnit()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeData() {
        vm.weatherData.observe(viewLifecycleOwner) {
            if (it.data != null) {
                updateQueryState(QueryState.DONE)
                updateUi(it.data)
            } else {
                updateQueryState(QueryState.ERROR)

                when (it.error) {
                    is NoSuchFieldException -> {
                        binding.errorView.bind(ErrorView.StateType.EMPTY, e = it.error)
                    }
                    else -> {
                        binding.errorView.bind(ErrorView.StateType.OPERATIONAL, e = it.error) {
                            vm.getCurrentWeather()
                        }
                    }
                }
            }
        }

        vm.queryState.observe(viewLifecycleOwner) {
            updateQueryState(it)
        }

        vm.progressState.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                updateQueryState(QueryState.LOADING)
            } else
                updateQueryState(QueryState.DONE)
        }

        vm.unitsState.observe(viewLifecycleOwner) {
            updateUnits(it)
        }
    }

    private fun findUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            locationPermissionRequestLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            queryUserLocation(
                onSuccess = {
                    vm.getCurrentWeather(lat = it?.latitude, lon = it?.longitude)
                },
                onError = {
                    binding.errorView.bind(
                        ErrorView.StateType.OPERATIONAL,
                        e = it
                    )
                }
            )
        }
    }

    private fun updateUi(forecast: CurrentDayForecast) {
        with(binding) {
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

    private fun updateUnits(unit: MeasurementUnit) {
        with(binding) {
            tvTempUnit.text = unit.temp
            tvFeelsLikeUnit.text = unit.temp
            tvWindUnit.text = unit.velocity
        }
    }

    private fun updateQueryState(state: QueryState) {
        with(binding) {
            when (state) {
                QueryState.LOADING -> {
                    swipe.isRefreshing = true
                    clContainer.visibility = View.GONE
                    errorView.visibility = View.GONE
                }

                QueryState.ERROR -> {
                    swipe.isRefreshing = false
                    clContainer.visibility = View.GONE
                    errorView.visibility = View.VISIBLE
                    errorView.bind(ErrorView.StateType.CONNECTION) {
                        onRefresh()
                    }
                }

                QueryState.DONE -> {
                    swipe.isRefreshing = false
                    clContainer.visibility = View.VISIBLE
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