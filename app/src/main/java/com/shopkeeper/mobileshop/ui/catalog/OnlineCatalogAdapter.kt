package com.shopkeeper.mobileshop.ui.catalog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.catalog.OnlinePhoneModel
import com.shopkeeper.mobileshop.databinding.ItemOnlinePhoneBinding

class OnlineCatalogAdapter(
    private val onAddToStockClick: (OnlinePhoneModel) -> Unit,
    private val onShareSpecsClick: (OnlinePhoneModel) -> Unit
) : ListAdapter<OnlinePhoneModel, OnlineCatalogAdapter.ModelViewHolder>(DiffCallback) {

    inner class ModelViewHolder(private val binding: ItemOnlinePhoneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: OnlinePhoneModel) {
            binding.tvBrandBadge.text = model.brand
            binding.tvModelName.text = model.fullName
            binding.tvKeySpecs.text = model.specs
            binding.tvDisplay.text = "Display: ${model.display}"
            binding.tvMemory.text = "Memory: ${model.ramOptions} • ${model.storageOptions}"
            binding.tvCameraBattery.text = "Camera: ${model.camera} • ${model.battery}"
            binding.tvColors.text = "Colors: ${model.colors}"

            binding.btnAddToStock.setOnClickListener { onAddToStockClick(model) }
            binding.btnShareSpecs.setOnClickListener { onShareSpecsClick(model) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemOnlinePhoneBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<OnlinePhoneModel>() {
            override fun areItemsTheSame(oldItem: OnlinePhoneModel, newItem: OnlinePhoneModel): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: OnlinePhoneModel, newItem: OnlinePhoneModel): Boolean =
                oldItem == newItem
        }
    }
}
