package com.forecasty.view.current_weather.adapters

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.forecasty.databinding.ViewItemPreviousSearchBinding

class PreviousSearchesAdapter(
    private val searches: List<Pair<String, String>>,
    private val onItemClick: (latlon: String) -> Unit
) : RecyclerView.Adapter<PreviousSearchesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewItemPreviousSearchBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            onItemClick
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(searches[position])
    }

    override fun getItemCount() = searches.size

    class ViewHolder(
        private val binding: ViewItemPreviousSearchBinding,
        private val onItemClick: (latlon: String) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(search: Pair<String, String>) {
            with(binding) {
                if (search.first.trim().isEmpty()) {
                    if (search.second.trim().isNotEmpty())
                        tvSearchTerm.text = search.second
                } else
                    tvSearchTerm.text = search.first

                if (search.first.trim().isEmpty())
                    tvLatLon.visibility = GONE
                else if (search.second.trim().isEmpty())
                    tvLatLon.visibility = GONE
                else {
                    tvLatLon.visibility = VISIBLE
                    tvLatLon.text = search.second
                }

                binding.root.setOnClickListener {
                    onItemClick(search.second)
                }
            }
        }
    }
}