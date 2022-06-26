package com.project.anekasari.ui.order

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.databinding.ActivityOrderDetailBinding
import com.project.anekasari.ui.keranjang.KeranjangModel
import java.text.DecimalFormat

class OrderDetailActivity : AppCompatActivity() {

    var binding: ActivityOrderDetailBinding? = null
    var model: OrderModel? = null
    var listOfProduct = ArrayList<KeranjangModel>()
    var adapter: OrderDetailAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        model = intent.getParcelableExtra(EXTRA_DATA)
        checkRole()

        listOfProduct.addAll(model?.product!!)


        val formatter = DecimalFormat("#,###")
        binding?.address?.text = "Alamat : ${model?.address}"
        binding?.phone?.text = "No.Handphone : ${model?.phone}"
        binding?.price?.text = "Total biaya : Rp.${formatter.format(model?.totalPriceFinal)}"

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
    }

    /// ini method untuk mengubah paymentStatus menjadi "Order Dikirim"
    private fun updatePaymentStatus() {
        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(model?.orderId!!)
            .update("paymentStatus", "Order Dikirim")
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    binding?.orderSentBtn?.visibility = View.INVISIBLE
                    Toast.makeText(this, "Berhasil memperbarui status order menjadi ''Order Dikirim''", Toast.LENGTH_SHORT).show()
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
                if(it.isSuccessful) {
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
                if(it.isSuccessful) {
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
                    if(model?.paymentStatus == "Belum Bayar") {
                        binding?.linearLayout?.visibility = View.VISIBLE
                    } else if (model?.paymentStatus == "Sudah Bayar") {
                        binding?.orderSentBtn?.visibility = View.VISIBLE
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