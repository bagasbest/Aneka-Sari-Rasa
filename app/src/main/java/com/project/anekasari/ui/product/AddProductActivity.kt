package com.project.anekasari.ui.product

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
import com.google.firebase.storage.FirebaseStorage
import com.project.anekasari.R
import com.project.anekasari.databinding.ActivityAddProductBinding
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private var binding: ActivityAddProductBinding? = null

    /// variable untuk menampung gambar dari galeri handphone
    private var image: String? = null

    /// variable untuk permission ke galeri handphone
    private val REQUEST_IMAGE_GALLERY = 1001
    private var merchantName: String? = null
    private var category: String? =  null
    private var merchantId: String? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        showDropdownCategory()
        getMerchantName()

        /// klik tombol kembali
        binding?.backButton?.setOnClickListener {
            onBackPressed()
        }

        /// tambahkan gambar produk
        binding?.imageHint?.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .compress(1024)
                .start(REQUEST_IMAGE_GALLERY)
        }


        /// save produk
        binding?.saveBtn?.setOnClickListener {
            formValidation()
        }

    }

    private fun showDropdownCategory() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.category, android.R.layout.simple_list_item_1
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding?.homepageCategory?.setAdapter(adapter)
        binding?.homepageCategory?.setOnItemClickListener { _, _, _, _ ->
            category = binding?.homepageCategory!!.text.toString()
        }
    }

    private fun getMerchantName() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                /// dapatkan nama merchant atau penjual
                merchantName = "" + it.data!!["fullName"]
                merchantId = "" + it.data!!["uid"]
            }
    }

    private fun formValidation() {
        val name = binding?.name?.text.toString().trim()
        val description = binding?.description?.text.toString().trim()
        val variant = binding?.variant?.text.toString().trim()
        val price = binding?.price?.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama produk harus diisi", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi produk harus diisi", Toast.LENGTH_SHORT).show()
        } else if (variant.isEmpty()) {
            Toast.makeText(this, "Varian produk harus diisi", Toast.LENGTH_SHORT).show()
        } else if (price.isEmpty()) {
            Toast.makeText(this, "Harga produk harus diisi", Toast.LENGTH_SHORT).show()
        } else if (image == null) {
            Toast.makeText(this, "Gambar produk harus diisi", Toast.LENGTH_SHORT).show()
        } else if (category == null) {
            Toast.makeText(this, "Kategori produk harus dipilih", Toast.LENGTH_SHORT).show()
        }
        else {

            /// jika semua kondisi terpenuhi, maka upload data produk ke dalam database
            binding?.progressBar?.visibility = View.VISIBLE

            val productId = System.currentTimeMillis().toString()
            val data = mapOf(
                "productId" to productId,
                "merchantName" to merchantName,
                "merchantId" to merchantId,
                "name" to name,
                "nameTemp" to name.toLowerCase(Locale.ROOT),
                "description" to description,
                "variant" to variant,
                "category" to category,
                "price" to price.toLong(),
                "image" to image,
            )

            FirebaseFirestore
                .getInstance()
                .collection("product")
                .document(productId)
                .set(data)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        /// sukses upload produk ke database
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(this, "Produk berhasil di unggah kedalam database", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    } else {
                        /// gagal upload produk ke database
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(this, "Produk gagal di unggah kedalam database", Toast.LENGTH_SHORT).show()
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


        val imageFileName = "product/image_" + System.currentTimeMillis() + ".png"
        /// proses upload gambar ke databsae
        mStorageRef.child(imageFileName).putFile(data!!)
            .addOnSuccessListener {
                mStorageRef.child(imageFileName).downloadUrl
                    .addOnSuccessListener { uri: Uri ->

                        /// proses upload selesai, berhasil
                        mProgressDialog.dismiss()
                        image = uri.toString()
                        Glide.with(this)
                            .load(image)
                            .into(binding!!.image)
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


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}