package com.project.anekasari.ui.product

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.Homepage
import com.project.anekasari.R
import com.project.anekasari.databinding.ActivityProdukDetailBinding
import java.text.DecimalFormat

class ProdukDetailActivity : AppCompatActivity() {

    private var binding: ActivityProdukDetailBinding? = null
    private var model: ProductModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val formatter = DecimalFormat("#,###")

        model = intent.getParcelableExtra(EXTRA_DATA)

        checkRole()

        Glide.with(this)
            .load(model?.image)
            .into(binding!!.image)

        binding?.name?.text = model?.name
        binding?.variant?.text = "Varian Rasa: ${model?.variant}"
        binding?.price?.text = "Rp.${formatter.format(model?.price)}"
        binding?.description?.text = model?.description
        binding?.category?.text = "Kategori: ${model?.category}"
        binding?.merchant?.text = "Penjual: ${model?.merchantName}"


        binding?.backButton?.setOnClickListener {
            onBackPressed()
        }

        binding?.addToCart?.setOnClickListener {
            showPopupQty()
        }

        binding?.edit?.setOnClickListener {
            val intent = Intent(this, ProductEditActivity::class.java)
            intent.putExtra(ProductEditActivity.EXTRA_DATA, model)
            startActivity(intent)
        }


        binding?.delete?.setOnClickListener {
            showConfirmationDeleteDialog()
        }

    }

    private fun showPopupQty() {
        val qtyEditText: TextInputEditText
        val confirmBtn: Button
        val pb: ProgressBar
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_qty_product)
        qtyEditText = dialog.findViewById(R.id.qty)
        confirmBtn = dialog.findViewById(R.id.confirmBtn)
        pb = dialog.findViewById(R.id.progressBar)



        confirmBtn?.setOnClickListener {
            val qtyProduct = qtyEditText.text.toString().trim()

            if(qtyProduct.isEmpty() || qtyProduct.toLong() <= 0) {
                Toast.makeText(this, "Maaf, kuantitas produk minimal 1", Toast.LENGTH_SHORT).show()
            } else {
                pb.visibility = View.VISIBLE

                val cartId = System.currentTimeMillis().toString()
                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                val data = mapOf(
                    "cartId" to cartId,
                    "merchantId" to model?.merchantId,
                    "productId" to model?.productId,
                    "userId" to userId,
                    "image" to model?.image,
                    "merchantName" to model?.merchantName,
                    "name" to model?.name,
                    "description" to model?.description,
                    "category" to model?.category,
                    "price" to model?.price!! * qtyProduct.toLong(),
                    "variant" to model?.variant,
                    "nameTemp" to model?.nameTemp,
                    "qty" to qtyProduct,
                )

                FirebaseFirestore
                    .getInstance()
                    .collection("cart")
                    .document(cartId)
                    .set(data)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            dialog.dismiss()
                            pb.visibility = View.GONE
                            showSuccessDialog()
                        } else {
                            dialog.dismiss()
                            pb.visibility = View.GONE
                            showFailureDialog()
                        }
                    }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Gagal menambahkan produk kedalam keranjang")
            .setMessage("Ups, koneksi internet anda sedang bermasalah, coba lagi nanti!")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Berhasil menambahkan produk kedalam keranjang")
            .setMessage("Produk ${model?.name} berhasil ditambahkan kedalam keranjang")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun showConfirmationDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi menghapus produk ${model?.name}")
            .setMessage("Apakah anda yakin ingin menghapus produk ini ?")
            .setIcon(R.drawable.ic_baseline_warning_24)
            .setPositiveButton("YA") { dialogInterface, _ ->
                dialogInterface.dismiss()
                deleteProduct()
            }
            .setNegativeButton("TIDAK", null)
            .show()
    }

    private fun deleteProduct() {
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()


        FirebaseFirestore
            .getInstance()
            .collection("product")
            .document(model?.productId!!)
            .delete()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    mProgressDialog.dismiss()
                    Toast.makeText(this, "Sukses menghapus produk", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Homepage::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    mProgressDialog.dismiss()
                    Toast.makeText(this, "Ups, sepertinya koneksi internetmu bermasalah, silahkan coba beberapa saat lagi", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkRole() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if(uid == model?.merchantId) {
            binding?.edit?.visibility = View.VISIBLE
            binding?.delete?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val EXTRA_DATA = "data"
    }
}