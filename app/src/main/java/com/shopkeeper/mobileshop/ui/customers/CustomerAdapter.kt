package com.shopkeeper.mobileshop.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Customer

import android.graphics.Color
import android.view.View
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
import com.shopkeeper.mobileshop.databinding.ItemCustomerBinding

class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(DiffCallback) {
    var pendingMap: Map<Long, Long> = emptyMap()
    val agingAnalyzer = UdhaarAgingAnalyzer()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(c: Customer) {
            binding.tvName.text = c.name
            binding.tvPhone.text = c.phone
            binding.tvInitials.text = c.name.take(1).uppercase()
            binding.root.setOnClickListener { onItemClick(c) }
            
            val oldestSaleDate = pendingMap[c.id]
            if (oldestSaleDate != null) {
                binding.tvAging.visibility = View.VISIBLE
                val aging = agingAnalyzer.getAgingCategory(oldestSaleDate)
                binding.tvAging.text = aging
                when (aging) {
                    "0-30 Days Overdue" -> binding.tvAging.setTextColor(Color.parseColor("#F57F17")) // Yellow
                    "31-60 Days Overdue" -> binding.tvAging.setTextColor(Color.parseColor("#E65100")) // Orange
                    "61-90 Days Overdue", "90+ Days Overdue (Critical)" -> binding.tvAging.setTextColor(Color.parseColor("#B71C1C")) // Red
                    else -> binding.tvAging.setTextColor(Color.GRAY)
                }
            } else {
                binding.tvAging.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Customer, newItem: Customer) = oldItem == newItem
    }
}
