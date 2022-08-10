package com.project.anekasari.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.anekasari.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    /// variable untuk menampung gambar dari galeri handphone
    private var image: String? = null

    /// variable untuk permission ke galeri handphone
    private val REQUEST_IMAGE_GALLERY = 1001
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        getUserData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /// tambahkan gambar produk
        binding.imageHint.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .compress(1024)
                .start(REQUEST_IMAGE_GALLERY)
        }


        binding.updateBtn.setOnClickListener {
            formValidation()
        }
    }

    private fun formValidation() {
        val fullName = binding.fullName.text.toString().trim()
        val username = binding.username.text.toString().trim()

        if(fullName.isEmpty()) {
            Toast.makeText(activity, "Nama lengkap tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else if (username.isEmpty()) {
            Toast.makeText(activity, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else {

            val data = mapOf(
                "fullName" to fullName,
                "username" to username,
            )

            FirebaseFirestore
                .getInstance()
                .collection("users")
                .document(uid)
                .update(data)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(activity, "Berhasil memperbarui profil", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun getUserData() {
        val mProgressDialog = ProgressDialog(activity)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val image = "" + it.data!!["image"]
                val fullName = "" + it.data!!["fullName"]
                val username = "" + it.data!!["username"]
                val email = "" + it.data!!["email"]
                val role = "" + it.data!!["role"]

                Log.e("tag", image)
                if(image != "null") {
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.image)
                }

                binding.fullName.setText(fullName)
                binding.username.setText(username)
                binding.email.setText(email)
                binding.role.setText(role)
                mProgressDialog.dismiss()
            }
    }

    // ini adalah program untuk menambahkan gambar kedalalam halaman ini
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                uploadImageToDatabase(data?.data)
            }
        }
    }


    /// fungsi untuk mengupload foto kedalam cloud storage
    private fun uploadImageToDatabase(data: Uri?) {
        val mStorageRef = FirebaseStorage.getInstance().reference

        val mProgressDialog = ProgressDialog(requireContext())
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()


        val imageFileName = "profile/image_" + System.currentTimeMillis() + ".png"
        /// proses upload gambar ke databsae
        mStorageRef.child(imageFileName).putFile(data!!)
            .addOnSuccessListener {
                mStorageRef.child(imageFileName).downloadUrl
                    .addOnSuccessListener { uri: Uri ->

                        /// proses upload selesai, berhasil

                        image = uri.toString()
                        Glide.with(requireContext())
                            .load(image)
                            .into(binding.image)

                        saveImageToDatabase(mProgressDialog)
                    }

                    /// proses upload selesai, gagal
                    .addOnFailureListener { e: Exception ->
                        mProgressDialog.dismiss()
                        Toast.makeText(
                            activity,
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
                    activity,
                    "Gagal mengunggah gambar",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("imageDp: ", e.toString())
            }
    }

    private fun saveImageToDatabase(mProgressDialog: ProgressDialog) {
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update("image", image)
            .addOnCompleteListener {
                mProgressDialog.dismiss()
                if(it.isSuccessful) {
                    Toast.makeText(activity, "Berhasil mengunggah foto profil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Gagal mengunggah foto profil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}