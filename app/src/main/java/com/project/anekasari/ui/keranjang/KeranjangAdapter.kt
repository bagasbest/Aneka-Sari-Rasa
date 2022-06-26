package com.project.anekasari.ui.keranjang

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.R
import com.project.anekasari.databinding.ItemCartBinding
import java.text.DecimalFormat

class KeranjangAdapter(
    private val listOfCart: ArrayList<KeranjangModel>,
    private val checkoutBtn: Button
) :
    RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(model: KeranjangModel) {
            with(binding) {
                val formatter = DecimalFormat("#,###")
                checkoutBtn.isEnabled = listOfCart.size > 0

                Glide.with(itemView.context)
                    .load(model.image)
                    .into(image)

                textView5.text = model.name
                qty.text = "Qty: ${model.qty}"
                price.text = "Qty: ${formatter.format(model.price)}"


                delete.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Konfirmasi Menghapus Produk")
                        .setMessage("Apakah anda yakin ingin menghapus produk ${model.name} dari keranjang ?")
                        .setIcon(R.drawable.ic_baseline_warning_24)
                        .setPositiveButton("YA") { dialogInterface, _ ->
                            dialogInterface.dismiss()

                            /// hapus produk jika ya
                            FirebaseFirestore
                                .getInstance()
                                .collection("cart")
                                .document(model.cartId!!)
                                .delete()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        listOfCart.removeAt(adapterPosition)
                                        notifyDataSetChanged()
                                        checkoutBtn.isEnabled = listOfCart.size > 0
                                    }
                                }
                        }
                        .setNegativeButton("TIDAK", null)
                        .show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listOfCart[position])
    }

    override fun getItemCount(): Int = listOfCart.size
}