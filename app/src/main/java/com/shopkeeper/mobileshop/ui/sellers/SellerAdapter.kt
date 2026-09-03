package com.shopkeeper.mobileshop.ui.sellers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Seller
import com.shopkeeper.mobileshop.databinding.ItemSellerBinding

class SellerAdapter(
    private val onCallClick: (Seller) -> Unit,
    private val onWhatsAppClick: (Seller) -> Unit,
    private val onDeleteClick: (Seller) -> Unit
) : ListAdapter<Seller, SellerAdapter.SellerViewHolder>(DiffCallback) {

    inner class SellerViewHolder(private val binding: ItemSellerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(seller: Seller) {
            binding.tvSellerName.text = seller.name
            binding.tvSellerRole.text = seller.role
            binding.tvCommission.text = "${seller.commissionPercent}% Comm."

            val phoneText = seller.phone.ifEmpty { "No phone recorded" }
            binding.tvSellerPhone.text = phoneText

            binding.btnCallSeller.setOnClickListener { onCallClick(seller) }
            binding.btnWhatsAppSeller.setOnClickListener { onWhatsAppClick(seller) }
            binding.btnDeleteSeller.setOnClickListener { onDeleteClick(seller) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerViewHolder {
        val binding = ItemSellerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SellerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SellerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Seller>() {
            override fun areItemsTheSame(oldItem: Seller, newItem: Seller): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Seller, newItem: Seller): Boolean =
                oldItem == newItem
        }
    }
}
