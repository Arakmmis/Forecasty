package com.forecasty.view.current_weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView.*
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forecasty.R
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.databinding.FragCurrentWeatherBinding
import com.forecasty.domain.QueryState
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.*
import com.forecasty.view.MainActivity
import com.forecasty.view.common.BaseCurrentWeatherFragment
import com.forecasty.view.common.BaseCurrentWeatherViewModel
import com.forecasty.view.common.ErrorView
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class CurrentWeatherFragment : BaseCurrentWeatherFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragCurrentWeatherBinding? = null
    private val binding get() = _binding!!

    private val vm: CurrentWeatherViewModel by viewModels()

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

    override fun onResume() {
        super.onResume()
        vm.getCurrentWeather()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragCurrentWeatherBinding.inflate(inflater, container, false)

        locationPermissionRequestLauncher = requestLocationPermission(locationResultBlock)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupMenu()
        observeData(vm, binding.errorView)
        setupBackPressedBehavior()
    }

    private fun setupViews() {
        with(binding) {
            with((requireActivity() as MainActivity)) {
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayShowTitleEnabled(
                    false
                )
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left)
            }

            toolbar.setNavigationOnClickListener { onBackPressed() }

            errorView.bind(ErrorView.StateType.NO_ERROR)
            swipe.setOnRefreshListener(this@CurrentWeatherFragment)

            layoutWeather.tvNextDays.setOnClickListener {
                findNavController().navigate(
                    CurrentWeatherFragmentDirections.actionCurrentWeatherFragmentToForecastFragment()
                )
            }

            searchView.bind(
                onSearchCompleted = { searchQuery ->
                    setSearchViewVisibility(false)
                    vm.getCurrentWeather(searchQuery)
                },
                onGetLocationClicked = {
                    setSearchViewVisibility(false)
                    findUserLocation()
                }
            ) {
                setSearchViewVisibility(false)
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            @SuppressLint("RestrictedApi")
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.current_weather_menu, menu)

                if (menu is MenuBuilder)
                    menu.setOptionalIconsVisible(true)

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
                        vm.getPreviousSearches()
                        setSearchViewVisibility(true)
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

    private fun findUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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

    private fun setSearchViewVisibility(isVisible: Boolean) =
        with(binding) {
            if (isVisible) {
                searchView.visibility = VISIBLE
                toolbar.visibility = GONE

                if (vm.queryState.value != null
                    && vm.queryState.value != QueryState.ERROR
                )
                    swipe.visibility = GONE
                else
                    errorView.visibility = GONE
            } else {
                hideSoftKeyboard()

                searchView.visibility = GONE
                toolbar.visibility = VISIBLE

                if (vm.queryState.value != null
                    && vm.queryState.value != QueryState.ERROR
                )
                    swipe.visibility = VISIBLE
                else
                    errorView.visibility = VISIBLE
            }
        }

    private fun setupBackPressedBehavior() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.searchView.visibility == VISIBLE)
                setSearchViewVisibility(false)
            else
                findNavController().navigateUp()
        }
    }

    override fun observeData(vm: BaseCurrentWeatherViewModel, errorView: ErrorView) {
        super.observeData(vm, errorView)

        with(vm as CurrentWeatherViewModel) {
            previousSearches.observe(viewLifecycleOwner) {
                binding.searchView.setupSearchAdapter(it.reversed())
            }
        }
    }

    override fun updateUi(forecast: CurrentDayForecast) {
        with(binding.layoutWeather) {
            tvDesc.text = forecast.weather?.firstOrNull()?.description

            ivIcon.bind(forecast.weather?.firstOrNull()?.id ?: -1)

            if (forecast.locationName != null && forecast.countryInfo?.name != null)
                tvLocation.text =
                    String.format("%s, %s", forecast.locationName, forecast.countryInfo.name)
            else
                tvLocation.visibility = GONE

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