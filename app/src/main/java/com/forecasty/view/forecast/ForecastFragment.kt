package com.forecasty.view.forecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.forecasty.R
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.databinding.FragForecastBinding
import com.forecasty.domain.QueryState
import com.forecasty.prefs.PrefsHelper
import com.forecasty.util.*
import com.forecasty.view.MainActivity
import com.forecasty.view.common.ErrorView
import com.forecasty.view.forecast.adapter.ForecastsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForecastFragment : Fragment(), OnRefreshListener {

    private var _binding: FragForecastBinding? = null
    private val binding get() = _binding!!

    private val vm: ForecastViewModel by viewModels()

    @Inject
    lateinit var prefsHelper: PrefsHelper

    private lateinit var adapter: ForecastsAdapter

    private lateinit var locationPermissionRequestLauncher: ActivityResultLauncher<String>
    private val locationResultBlock = { isGranted: Boolean ->
        if (isGranted) {
            queryUserLocation(
                onSuccess = {
                    vm.getForecast(lat = it?.latitude, lon = it?.longitude)
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
        vm.getForecast()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragForecastBinding.inflate(inflater, container, false)

        locationPermissionRequestLauncher = requestLocationPermission(locationResultBlock)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupMenu()
        observeData()
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
            swipe.setOnRefreshListener(this@ForecastFragment)

            searchView.bind(
                onSearchCompleted = { searchQuery ->
                    setSearchViewVisibility(false)
                    vm.getForecast(query = searchQuery)
                },
                onGetLocationClicked = {
                    setSearchViewVisibility(false)
                    findUserLocation()
                }
            ) {
                setSearchViewVisibility(false)
            }

            adapter = ForecastsAdapter(prefsHelper = prefsHelper)
            rvForecasts.adapter = adapter
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            @SuppressLint("RestrictedApi")
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.forecast_menu, menu)

                if (menu is MenuBuilder)
                    menu.setOptionalIconsVisible(true)
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

                    R.id.action_filter -> {
                        vm.filterResults()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeData() {
        vm.forecastData.observe(viewLifecycleOwner) {
            if (it.data != null) {
                updateQueryState(QueryState.DONE)
                updateUi(it.data)
            } else {
                updateQueryState(QueryState.ERROR)

                when (it.error) {
                    is NoSuchFieldException -> {
                        binding.errorView.bind(ErrorView.StateType.EMPTY, e = it.error)
                    }
                    is NoSuchElementException -> {
                        binding.errorView.bind(
                            ErrorView.StateType.OPERATIONAL,
                            desc = getString(R.string.err_location_not_found),
                            e = it.error
                        ) {
                            vm.getForecast()
                        }
                    }
                    else -> {
                        binding.errorView.bind(ErrorView.StateType.OPERATIONAL, e = it.error) {
                            vm.getForecast()
                        }
                    }
                }
            }
        }

        vm.queryState.observe(viewLifecycleOwner) {
            updateQueryState(it)
        }

        vm.previousSearches.observe(viewLifecycleOwner) {
            binding.searchView.setupSearchAdapter(it.reversed())
        }

        vm.currentFilter.observe(viewLifecycleOwner) {
            filterForecasts(it)
        }
    }

    private fun updateUi(extendedForecast: ExtendedForecast) {
        with(binding.layoutHeader) {
            extendedForecast.forecasts?.firstOrNull()?.let {
                val unit = prefsHelper.measurementUnit

                ivIcon.bind(it.weather?.firstOrNull()?.id ?: -1)

                tvTempMax.text =
                    String.format("%d%s", it.temp?.tempMax?.toInt(), unit.temp)

                tvTempMin.text =
                    String.format("%d%s", it.temp?.tempMin?.toInt(), unit.temp)

                tvDayTime.text = it.forecastDateTime?.getDayOfWeekAndTimeFromDate()
                tvDate.text = it.forecastDateTime?.getFormattedDate()

                tvWindValue.text = String.format("%.1f %s", it.wind?.speed, unit.velocity)
                tvHumidityValue.text = String.format("%d%%", it.temp?.humidity)
            }
        }

        with(binding) {
            extendedForecast.forecasts?.let {
                cvForecasts.visibility = VISIBLE
                adapter.submitList(it)
            } ?: {
                cvForecasts.visibility = GONE
            }
        }
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
                    vm.getForecast(lat = it?.latitude, lon = it?.longitude)
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
                searchView.visibility = SearchView.VISIBLE
                toolbar.visibility = SearchView.GONE

                if (vm.queryState.value != null
                    && vm.queryState.value != QueryState.ERROR
                )
                    swipe.visibility = SearchView.GONE
                else
                    errorView.visibility = SearchView.GONE
            } else {
                hideSoftKeyboard()

                searchView.visibility = SearchView.GONE
                toolbar.visibility = SearchView.VISIBLE

                if (vm.queryState.value != null
                    && vm.queryState.value != QueryState.ERROR
                )
                    swipe.visibility = SearchView.VISIBLE
                else
                    errorView.visibility = SearchView.VISIBLE
            }
        }

    private fun setupBackPressedBehavior() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.searchView.visibility == SearchView.VISIBLE)
                setSearchViewVisibility(false)
            else
                findNavController().navigateUp()
        }
    }

    private fun updateQueryState(state: QueryState) {
        with(binding) {
            when (state) {
                QueryState.LOADING -> {
                    swipe.isRefreshing = true
                    svContent.visibility = GONE
                    errorView.visibility = GONE
                }

                QueryState.ERROR -> {
                    swipe.isRefreshing = false
                    svContent.visibility = GONE
                    errorView.visibility = VISIBLE
                    errorView.bind(ErrorView.StateType.CONNECTION) {
                        onRefresh()
                    }
                }

                QueryState.DONE -> {
                    swipe.isRefreshing = false
                    svContent.visibility = VISIBLE
                    errorView.visibility = GONE
                    errorView.bind(ErrorView.StateType.NO_ERROR)
                }
            }
        }
    }

    private fun filterForecasts(filter: ForecastViewModel.Filter) {
        adapter.submitList(
            vm.forecastData.value?.data?.forecasts
                ?.take(filter.count) ?: emptyList()
        )
    }

    override fun onRefresh() {
        vm.refresh()
    }
}