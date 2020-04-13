package com.johan.evmap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.johan.evmap.BR
import com.johan.evmap.R
import com.johan.evmap.api.availability.ChargepointStatus
import com.johan.evmap.api.goingelectric.ChargeLocation
import com.johan.evmap.api.goingelectric.Chargepoint

interface Equatable {
    override fun equals(other: Any?): Boolean;
}

abstract class DataBindingAdapter<T : Equatable>() :
    ListAdapter<T, DataBindingAdapter.ViewHolder<T>>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: T) {
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
        }
    }

    class DiffCallback<T : Equatable> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem === newItem

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    }
}

fun chargepointWithAvailability(
    chargepoints: Iterable<Chargepoint>?,
    availability: Map<Chargepoint, List<ChargepointStatus>>?
): List<ConnectorAdapter.ChargepointWithAvailability>? {
    return chargepoints?.map {
        ConnectorAdapter.ChargepointWithAvailability(
            it, availability?.get(it)?.count { it == ChargepointStatus.AVAILABLE }
        )
    }
}

class ConnectorAdapter : DataBindingAdapter<ConnectorAdapter.ChargepointWithAvailability>() {
    data class ChargepointWithAvailability(val chargepoint: Chargepoint, val available: Int?) :
        Equatable

    override fun getItemViewType(position: Int): Int = R.layout.item_connector
}

class DetailAdapter : DataBindingAdapter<DetailAdapter.Detail>() {
    data class Detail(
        val icon: Int,
        val contentDescription: Int,
        val text: CharSequence,
        val detailText: CharSequence? = null
    ) : Equatable

    override fun getItemViewType(position: Int): Int = R.layout.item_detail
}

fun buildDetails(loc: ChargeLocation?, ctx: Context): List<DetailAdapter.Detail> {
    if (loc == null) return emptyList()

    return listOfNotNull(
        DetailAdapter.Detail(
            R.drawable.ic_address,
            R.string.address,
            loc.address.toString(),
            loc.locationDescription
        ),
        if (loc.operator != null) DetailAdapter.Detail(
            R.drawable.ic_operator,
            R.string.operator,
            loc.operator
        ) else null,
        if (loc.network != null) DetailAdapter.Detail(
            R.drawable.ic_network,
            R.string.network,
            loc.network
        ) else null,
        // TODO: separate layout for opening hours with expandable details
        if (loc.openinghours != null) DetailAdapter.Detail(
            R.drawable.ic_hours,
            R.string.hours,
            loc.openinghours.getStatusText(ctx),
            loc.openinghours.description
        ) else null,
        if (loc.cost != null) DetailAdapter.Detail(
            R.drawable.ic_cost,
            R.string.cost,
            loc.cost.getStatusText(ctx),
            loc.cost.descriptionLong ?: loc.cost.descriptionShort
        )
        else null
    )
}