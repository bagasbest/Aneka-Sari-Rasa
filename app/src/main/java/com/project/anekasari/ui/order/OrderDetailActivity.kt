package com.project.anekasari.ui.order

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.R
import com.project.anekasari.databinding.ActivityOrderDetailBinding
import com.project.anekasari.ui.keranjang.KeranjangModel
import java.text.DecimalFormat

class OrderDetailActivity : AppCompatActivity() {

    var binding: ActivityOrderDetailBinding? = null
    var model: OrderModel? = null
    var listOfProduct = ArrayList<KeranjangModel>()
    var adapter: OrderDetailAdapter? = null
    var name: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        model = intent.getParcelableExtra(EXTRA_DATA)
        checkRole()
        getUserName()

        listOfProduct.addAll(model?.product!!)


        val formatter = DecimalFormat("#,###")
        binding?.address?.text = "Alamat : ${model?.address}"
        binding?.phone?.text = "No.Handphone : ${model?.phone}"
        binding?.ongkir?.text = "Biaya Ongkir : Rp.${formatter.format(model?.ongkir)}"
        binding?.price?.text = "Total biaya : Rp.${
            formatter.format(
                model?.totalPriceFinal?.plus(
                    model?.ongkir!!
                ) ?: 0
            )
        }"

        initRecyclerView()

        binding?.uploadPaymentProofBtn?.setOnClickListener {
            val intent = Intent(this, OrderPaymentActivity::class.java)
            intent.putExtra(OrderPaymentActivity.EXTRA_DATA, model)
            startActivity(intent)
        }

        /// dari sisi merchant kita bisa menerima atau menolak order
        binding?.acc?.setOnClickListener {
            accOrder()
        }
        binding?.decline?.setOnClickListener {
            declineOrder()
        }

        /// dari sisi merchant bisa menekan tombol order dikirim
        binding?.orderSentBtn?.setOnClickListener {
            updatePaymentStatus()
        }

        binding?.backButton?.setOnClickListener {
            onBackPressed()
        }

        binding?.resiAdd?.setOnClickListener{
            showResiDialog()
        }
    }

    private fun showResiDialog() {
        val resiEt: TextInputEditText
        val confirmBtn: Button
        val pb: ProgressBar
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_resi)

        resiEt = dialog.findViewById(R.id.resi)
        confirmBtn = dialog.findViewById(R.id.confirmBtn)
        pb = dialog.findViewById(R.id.progressBar)

        confirmBtn.setOnClickListener {
            val resi = resiEt.text.toString().trim()
            if(resi.isEmpty()) {
                Toast.makeText(this, "No.Resi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                pb.visibility = View.VISIBLE

                FirebaseFirestore
                    .getInstance()
                    .collection("order")
                    .document(model?.orderId!!)
                    .update("resi", resi)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            binding?.resi?.text = resi
                            Toast.makeText(this, "Berhasil menginputkan No.Resi", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun getUserName() {
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(model?.userId!!)
            .get()
            .addOnSuccessListener {
                name = "" + it.data!!["fullName"]
                binding?.name?.text = "Nama : $name"
            }
    }

    /// ini method untuk mengubah paymentStatus menjadi "Order Dikirim"
    private fun updatePaymentStatus() {
        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(model?.orderId!!)
            .update("paymentStatus", "Order Dikirim")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    binding?.orderSentBtn?.visibility = View.INVISIBLE
                    Toast.makeText(
                        this,
                        "Berhasil memperbarui status order menjadi ''Order Dikirim''",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /// ini method untuk menerima bukti pembayaran
    /// ini method untuk mengubah paymentStatus menjadi "Pembayaran Diterima"
    private fun accOrder() {
        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(model?.orderId!!)
            .update("paymentStatus", "Sudah Bayar")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    binding?.linearLayout?.visibility = View.INVISIBLE
                    Toast.makeText(this, "Berhasil menerima pembayaran", Toast.LENGTH_SHORT).show()
                    binding?.orderSentBtn?.visibility = View.VISIBLE
                }
            }
    }

    /// ini method untuk menolak bukti pembayaran
    /// ini method untuk mengubah paymentStatus menjadi "Pembayaran Ditolak"
    private fun declineOrder() {
        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(model?.orderId!!)
            .update("paymentStatus", "Pembayaran Ditolak")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    binding?.linearLayout?.visibility = View.INVISIBLE
                    Toast.makeText(this, "Berhasil menolak pembayaran", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkRole() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val role = "" + it.data!!["role"]
                if (role == "merchant") {
                    if (model?.paymentStatus == "Belum Bayar") {
                        binding?.linearLayout?.visibility = View.VISIBLE
                    } else if (model?.paymentStatus == "Sudah Bayar") {
                        binding?.orderSentBtn?.visibility = View.VISIBLE
                    }

                    if(model?.paymentStatus == "Order Dikirim") {
                        binding?.resiAdd?.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun initRecyclerView() {
        binding?.productRv?.layoutManager = LinearLayoutManager(this)
        adapter = OrderDetailAdapter()
        binding?.productRv?.adapter = adapter
        adapter?.setData(listOfProduct)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val EXTRA_DATA = "data"
    }
}