package com.project.anekasari.ui.order

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.anekasari.R
import com.project.anekasari.databinding.ItemOrderBinding
import java.text.DecimalFormat

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val orderList = ArrayList<OrderModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: ArrayList<OrderModel>) {
        orderList.clear()
        orderList.addAll(items)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(model: OrderModel) {
            with(binding) {
                val formatter = DecimalFormat("#,###")

                orderId.text = "Order ID : INV-${model.orderId}"
                date.text = "Tanggal order : ${model.date}"
                paymentStatus.text = "${model.paymentStatus}"
                price.text = "Total harga : Rp.${formatter.format(model.totalPriceFinal)}"

                /// misal status == belum bayar -> background warna merah
                /// misal status == sudah bayar -> background warna biru
                /// misal status == pembayaran ditolak admin -> background orange
                /// misal status == order dikirim ke alamat user -> background hijau

                when (model.paymentStatus) {
                    "Belum Bayar" -> {
                        background.backgroundTintList =
                            ContextCompat.getColorStateList(itemView.context, R.color.red)
                    }
                    "Sudah Bayar" -> {
                        background.backgroundTintList = ContextCompat.getColorStateList(
                            itemView.context,
                            android.R.color.holo_blue_dark
                        )
                    }
                    "Pembayaran Ditolak" -> {
                        background.backgroundTintList = ContextCompat.getColorStateList(
                            itemView.context,
                            android.R.color.holo_orange_dark
                        )
                    }
                    "Order Dikirim" -> {
                        background.backgroundTintList = ContextCompat.getColorStateList(
                            itemView.context,
                            android.R.color.holo_green_dark
                        )
                    }
                }


                itemClick.setOnClickListener {
                    val intent = Intent(itemView.context, OrderDetailActivity::class.java)
                    intent.putExtra(OrderDetailActivity.EXTRA_DATA, model)
                    itemView.context.startActivity(intent)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size
}