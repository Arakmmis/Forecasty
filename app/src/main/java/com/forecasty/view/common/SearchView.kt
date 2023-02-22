package com.forecasty.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.forecasty.R
import com.forecasty.databinding.ViewSearchBinding
import com.forecasty.util.QueryType
import com.forecasty.util.ValidationUtils
import com.forecasty.view.current_weather.adapters.PreviousSearchesAdapter
import java.util.regex.Pattern

class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSearchBinding

    private var adapter: PreviousSearchesAdapter? = null

    private lateinit var onSearchCompleted: (query: String, type: QueryType) -> Unit

    init {
        binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun bind(
        onSearchCompleted: (query: String, type: QueryType) -> Unit,
        onGetLocationClicked: () -> Unit,
        onCloseClicked: () -> Unit
    ) {
        binding.etSearch.requestFocus()
        this.onSearchCompleted = onSearchCompleted

        setupTextWatchers()
        setupClickListeners(
            onSearchCompleted,
            onGetLocationClicked,
            onCloseClicked
        )
    }

    fun setupSearchAdapter(previousSearches: List<Pair<String, String>>) {
        with(binding) {
            if (previousSearches.isEmpty())
                rvPreviousSearches.visibility = GONE
            else {
                rvPreviousSearches.visibility = VISIBLE
                adapter = PreviousSearchesAdapter(previousSearches) {
                    onSearchCompleted(
                        it,
                        QueryType.LAT_LON
                    )
                }
                rvPreviousSearches.adapter = adapter
            }
        }
    }

    private fun setupTextWatchers() {
        with(binding) {
            etSearch.doOnTextChanged { text, _, _, _ ->
                if (text?.isEmpty() == true) {
                    ivClear.visibility = GONE
                    ivGetLocation.visibility = VISIBLE
                } else {
                    ivClear.visibility = VISIBLE
                    ivGetLocation.visibility = GONE
                }
            }

            etSearch.doAfterTextChanged { text ->
                if (rgSearchType.checkedRadioButtonId == R.id.rbLatLon) {
                    validateLatLon(text?.trim().toString())
                    return@doAfterTextChanged
                }

                if (rgSearchType.checkedRadioButtonId == R.id.rbZip) {
                    validateZipCode(text?.trim().toString())
                }
            }
        }
    }

    private fun setupClickListeners(
        onSearchCompleted: (query: String, type: QueryType) -> Unit,
        onGetLocationClicked: () -> Unit,
        onCloseClicked: () -> Unit
    ) {
        with(binding) {
            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (rgSearchType.checkedRadioButtonId == R.id.rbLatLon
                        && etSearch.error != null
                    ) {
                        return@setOnEditorActionListener true
                    }

                    onSearchCompleted(
                        etSearch.text.trim().toString(),
                        QueryType.values().find {
                            it.resId == rgSearchType.checkedRadioButtonId
                        } ?: QueryType.CITY_NAME
                    )

                    return@setOnEditorActionListener true
                }

                false
            }

            rgSearchType.setOnCheckedChangeListener { _, id ->
                when (id) {
                    R.id.rbLatLon ->
                        etSearch.hint = resources.getString(R.string.lat_lon_hint)
                    else -> {
                        etSearch.hint = resources.getString(R.string.search_hint)
                    }
                }
            }

            ivClear.setOnClickListener {
                etSearch.setText("")
            }

            ivGetLocation.setOnClickListener {
                onGetLocationClicked()
            }

            ivClose.setOnClickListener {
                onCloseClicked()
            }
        }
    }

    private fun validateLatLon(query: String) {
        val isValid = Pattern.matches(
            ValidationUtils.LAT_LON_REGEX,
            query
        )

        if (isValid || query.isEmpty())
            binding.etSearch.error = null
        else
            binding.etSearch.error = resources.getString(R.string.err_lat_lon_entry)
    }

    private fun validateZipCode(query: String) {
        val isValid = Pattern.matches(
            ValidationUtils.ZIP_CODE_REGEX,
            query
        )

        if (isValid || query.isEmpty())
            binding.etSearch.error = null
        else
            binding.etSearch.error = resources.getString(R.string.err_zip_code_entry)
    }
}