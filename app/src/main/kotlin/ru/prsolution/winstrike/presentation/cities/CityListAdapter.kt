package ru.prsolution.winstrike.presentation.cities

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_city.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import ru.prsolution.winstrike.R
import ru.prsolution.winstrike.presentation.model.CityItem
import ru.prsolution.winstrike.presentation.utils.inflate
import ru.prsolution.winstrike.presentation.utils.pref.PrefUtils

/**
 * Created by Oleg Sitnikov on 2019-02-12
 */

class CityListAdapter constructor(private val itemClick: (CityItem) -> Unit) :
        ListAdapter<CityItem, CityListAdapter.ViewHolder>(CityItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityListAdapter.ViewHolder =
            ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(parent.inflate(R.layout.item_city)) {

        fun bind(item: CityItem) {
            itemView.name_tv.text = item.name

            if (SELECTED_ITEM == item.id) {
                with(itemView) {
                   card_view.setCardBackgroundColor(resources.getColor(R.color.colorAccent))

                   name_tv.setTextColor(resources.getColor(R.color.color_white))

                    checkbox_iv.setImageResource(R.drawable.ic_check_white)
                }
            }
            itemView.setOnClickListener { itemClick.invoke(item) }
        }
    }

    companion object {
        var SELECTED_ITEM: String? = PrefUtils.cityPid
    }
}


private class CityItemDiffCallback : DiffUtil.ItemCallback<CityItem>() {
    override fun areItemsTheSame(oldItem: CityItem, newItem: CityItem): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CityItem, newItem: CityItem): Boolean =
            oldItem.name == newItem.name
}