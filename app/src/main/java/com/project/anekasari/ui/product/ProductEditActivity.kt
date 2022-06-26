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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.anekasari.Homepage
import com.project.anekasari.R
import com.project.anekasari.databinding.ActivityProductEditBinding
import java.util.*

class ProductEditActivity : AppCompatActivity() {

    private var binding : ActivityProductEditBinding? = null
    private var model : ProductModel? = null
    private var category : String? = null
    /// variable untuk menampung gambar dari galeri handphone
    private var image: String? = null

    /// variable untuk permission ke galeri handphone
    private val REQUEST_IMAGE_GALLERY = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductEditBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        model = intent.getParcelableExtra(EXTRA_DATA)
        showDropdownCategory()
        Glide.with(this)
            .load(model?.image)
            .into(binding!!.image)

        image = model?.image
        category = model?.category
        binding?.name?.setText(model?.name)
        binding?.description?.setText(model?.description)
        binding?.variant?.setText(model?.variant)
        binding?.price?.setText(model?.price.toString())

        binding?.backButton?.setOnClickListener {
            onBackPressed()
        }


        binding?.saveBtn?.setOnClickListener {
            formValidation()
        }

        /// tambahkan gambar produk
        binding?.imageHint?.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .compress(1024)
                .start(REQUEST_IMAGE_GALLERY)
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
        }
        else {

            /// jika semua kondisi terpenuhi, maka upload data produk ke dalam database
            binding?.progressBar?.visibility = View.VISIBLE

            val data = mapOf(
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
                .document(model?.productId!!)
                .update(data)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        /// sukses upload produk ke database
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Produk berhasil diperbarui kedalam database",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, Homepage::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        /// gagal upload produk ke database
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Produk gagal diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()
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

    companion object {
        const val EXTRA_DATA = "data"
    }
}