package com.project.anekasari.ui.order

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.project.anekasari.Homepage
import com.project.anekasari.R
import com.project.anekasari.databinding.ActivityOrderPaymentBinding
import java.text.DecimalFormat

class OrderPaymentActivity : AppCompatActivity() {

    private var binding : ActivityOrderPaymentBinding? = null
    private var paymentMethod : String? = null
    /// variable untuk menampung gambar dari galeri handphone
    private var image: String? = null

    /// variable untuk permission ke galeri handphone
    private val REQUEST_IMAGE_GALLERY = 1001
    private var model : OrderModel ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPaymentBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        model = intent.getParcelableExtra(EXTRA_DATA)
        checkRole()
        showPaymentMethod()

        if(model?.paymentProof != "") {
            Glide.with(this)
                .load(model?.paymentProof)
                .into(binding!!.paymentProof)
        }

        binding?.backButton?.setOnClickListener {
            onBackPressed()
        }

        binding?.imageHint?.setOnClickListener {
            if(paymentMethod != null) {
                ImagePicker.with(this)
                    .galleryOnly()
                    .compress(1024)
                    .start(REQUEST_IMAGE_GALLERY)
            } else {
                Toast.makeText(this, "Anda harus memilih metode pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show()
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
                if(role == "user" && model?.paymentStatus == "Belum Bayar") {
                    binding?.imageHint?.visibility = View.VISIBLE
                } else if (role == "merchant") {
                    binding?.textInputLayout123?.visibility = View.GONE
                    binding?.paymentInfo?.visibility = View.GONE
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun showPaymentMethod() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.payment_method, android.R.layout.simple_list_item_1
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding?.paymentStatus?.setAdapter(adapter)
        binding?.paymentStatus?.setOnItemClickListener { _, _, _, _ ->
            paymentMethod = binding?.paymentStatus?.text.toString()
            when (paymentMethod) {
                "Bank BCA" -> {
                    binding?.bankName?.text = "Nama Bank : $paymentMethod"
                    binding?.recNumber?.text = "No.Rekening : 007-1234-5678"
                    binding?.recName?.text = "Atas Nama : ${model?.merchantName}"
                    val formatter = DecimalFormat("#,###")
                    binding?.finalPrice?.text = "Nominal Transfer : Rp.${formatter.format(model?.totalPriceFinal)}"
                }
                "Bank Mandiri" -> {
                    binding?.bankName?.text = "Nama Bank : $paymentMethod"
                    binding?.recNumber?.text = "No.Rekening : 012-8384-1273"
                    binding?.recName?.text = "Atas Nama : ${model?.merchantName}"
                    val formatter = DecimalFormat("#,###")
                    binding?.finalPrice?.text = "Nominal Transfer : Rp.${formatter.format(model?.totalPriceFinal)}"
                }
                else -> {
                    binding?.bankName?.text = "Nama Bank : $paymentMethod"
                    binding?.recNumber?.text = "No.Rekening : 022-3234-4322"
                    binding?.recName?.text = "Atas Nama : ${model?.merchantName}"
                    val formatter = DecimalFormat("#,###")
                    binding?.finalPrice?.text = "Nominal Transfer : Rp.${formatter.format(model?.totalPriceFinal)}"
                }
            }
        }
    }

    /// ini adalah program untuk menambahkan gambar kedalalam halaman ini
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                uploadImageToDatabase(data?.data)
            }
        }
    }


    /// fungsi untuk mengupload foto kedalam cloud storage
    private fun uploadImageToDatabase(data: Uri?) {
        val mStorageRef = FirebaseStorage.getInstance().reference

        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()


        val imageFileName = "payment_proof/image_" + System.currentTimeMillis() + ".png"
        /// proses upload gambar ke databsae
        mStorageRef.child(imageFileName).putFile(data!!)
            .addOnSuccessListener {
                mStorageRef.child(imageFileName).downloadUrl
                    .addOnSuccessListener { uri: Uri ->

                        /// proses upload selesai, berhasil
                        image = uri.toString()
                        Glide.with(this)
                            .load(image)
                            .into(binding!!.paymentProof)
                        savePaymentProofToDatabase(mProgressDialog)
                    }

                    /// proses upload selesai, gagal
                    .addOnFailureListener { e: Exception ->
                        mProgressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Gagal mengunggah gambar",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("imageDp: ", e.toString())
                    }
            }
            /// proses upload selesai, gagal
            .addOnFailureListener { e: Exception ->
                mProgressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Gagal mengunggah gambar",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("imageDp: ", e.toString())
            }
    }

    private fun savePaymentProofToDatabase(mProgressDialog: ProgressDialog) {
        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(model?.orderId!!)
            .update("paymentProof", image)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    mProgressDialog.dismiss()
                    Toast.makeText(this, "Sukses mengunggah bukti pembayaran!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Homepage::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
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