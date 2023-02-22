package com.forecasty.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.forecasty.R
import com.forecasty.databinding.ViewSearchBinding
import com.forecasty.util.QueryType
import com.forecasty.util.ValidationUtils
import java.util.regex.Pattern

class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSearchBinding

    init {
        binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun bind(
        previousSearches: List<String> = emptyList(),
        onSearchCompleted: (query: String, type: QueryType) -> Unit,
        onGetLocationClicked: () -> Unit,
        onCloseClicked: () -> Unit
    ) {
        setupSearchAdapter(previousSearches)
        setupTextWatchers()
        setupClickListeners(
            previousSearches,
            onSearchCompleted,
            onGetLocationClicked,
            onCloseClicked
        )
    }

    private fun setupSearchAdapter(previousSearches: List<String>) {
        with(binding) {
            val previousSearchesAdapter = ArrayAdapter(
                context,
                R.layout.view_item_previous_search,
                previousSearches
            )

            acSearch.threshold = 0
            acSearch.dropDownVerticalOffset = 0

            acSearch.setAdapter(previousSearchesAdapter)

            acSearch.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && previousSearches.isNotEmpty())
                    acSearch.showDropDown()
            }
        }
    }

    private fun setupTextWatchers() {
        with(binding) {
            acSearch.doOnTextChanged { text, _, _, _ ->
                if (text?.isEmpty() == true) {
                    ivClear.visibility = GONE
                    ivGetLocation.visibility = VISIBLE
                } else {
                    ivClear.visibility = VISIBLE
                    ivGetLocation.visibility = GONE
                }
            }

            acSearch.doAfterTextChanged { text ->
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
        previousSearches: List<String>,
        onSearchCompleted: (query: String, type: QueryType) -> Unit,
        onGetLocationClicked: () -> Unit,
        onCloseClicked: () -> Unit
    ) {
        with(binding) {
            acSearch.setOnClickListener {
                if (previousSearches.isNotEmpty())
                    acSearch.showDropDown()
            }

            acSearch.setOnItemClickListener { adapterView, _, i, _ ->
                onSearchCompleted(
                    adapterView.getItemAtPosition(i) as String,
                    QueryType.values().find {
                        it.resId == rgSearchType.checkedRadioButtonId
                    } ?: QueryType.CITY_NAME
                )
            }

            acSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (rgSearchType.checkedRadioButtonId == R.id.rbLatLon
                        && acSearch.error != null
                    ) {
                        return@setOnEditorActionListener true
                    }

                    onSearchCompleted(
                        acSearch.text.trim().toString(),
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
                        acSearch.hint = resources.getString(R.string.lat_lon_hint)
                    else -> {
                        acSearch.hint = resources.getString(R.string.search_hint)
                    }
                }
            }

            ivClear.setOnClickListener {
                acSearch.setText("")
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
            binding.acSearch.error = null
        else
            binding.acSearch.error = resources.getString(R.string.err_lat_lon_entry)

    }

    private fun validateZipCode(query: String) {
        val isValid = Pattern.matches(
            ValidationUtils.ZIP_CODE_REGEX,
            query
        )

        if (isValid || query.isEmpty())
            binding.acSearch.error = null
        else
            binding.acSearch.error = resources.getString(R.string.err_zip_code_entry)
    }
}