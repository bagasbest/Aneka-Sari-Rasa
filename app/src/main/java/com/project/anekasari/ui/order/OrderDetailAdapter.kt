package com.project.anekasari.ui.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.anekasari.databinding.ItemOrderDetailBinding
import com.project.anekasari.ui.keranjang.KeranjangModel
import java.text.DecimalFormat

class OrderDetailAdapter : RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    private val cartList = ArrayList<KeranjangModel>()
    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: ArrayList<KeranjangModel>) {
        cartList.clear()
        cartList.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding : ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(model: KeranjangModel) {
            with(binding) {
                val formatter = DecimalFormat("#,###")
                Glide.with(itemView.context)
                    .load(model.image)
                    .into(image)

                name.text = model.name
                price.text = "Rp.${formatter.format(model.price)}"
                qty.text = "Qty: " + model.qty

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    override fun getItemCount(): Int = cartList.size
}