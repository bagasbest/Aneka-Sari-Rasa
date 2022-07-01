package com.project.anekasari.ui.product

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.anekasari.databinding.ItemProductBinding
import java.text.DecimalFormat

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val productList = ArrayList<ProductModel>()
    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: ArrayList<ProductModel>) {
        productList.clear()
        productList.addAll(items)
        notifyDataSetChanged()
    }


    class ViewHolder(private val binding : ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(productModel: ProductModel) {
            with(binding) {
                val formatter = DecimalFormat("#,###")

                /// pasang gambar dari list product ke masing masing item
                Glide.with(itemView.context)
                    .load(productModel.image)
                    .into(image)

                name.text = productModel.name
                price.text = "Rp.${formatter.format(productModel.price ?: 0)}"
                sellerName.text = productModel.merchantName

                itemCardView.setOnClickListener {
                    val intent = Intent(itemView.context, ProdukDetailActivity::class.java)
                    intent.putExtra(ProdukDetailActivity.EXTRA_DATA, productModel)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size


}