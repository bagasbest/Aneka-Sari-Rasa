package com.project.anekasari

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    /// var itu berarti variabel tersebut dapat di ubah atau di isi dengan value lain
    /// val itu berarti variabel tidak bisa di ubah
    private var binding: ActivityRegisterBinding? = null
    private var role = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /// klik register button, maka proses registrasi data user akan disimpan kedalam database
        /// user bisa melakukan login setelah register
        binding?.registerBtn?.setOnClickListener {
            formValidation()
        }


    }


    private fun formValidation() {
        val fullName = binding?.fullName?.text.toString().trim()
        val username = binding?.username?.text.toString().trim()
        val email = binding?.email?.text.toString().trim()
        val password = binding?.password?.text.toString().trim()

        /// buat validasi
        if(fullName.isEmpty()) {
            Toast.makeText(this, "Nama lengkap harus diisi!", Toast.LENGTH_SHORT).show()
        } else if (username.isEmpty()) {
            Toast.makeText(this, "Username harus diisi!", Toast.LENGTH_SHORT).show()
        } else if (email.isEmpty()) {
            Toast.makeText(this, "Email harus diisi!", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password harus diisi!", Toast.LENGTH_SHORT).show()
        } else if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
        }
        else if (role == "") {
            Toast.makeText(this, "Anda harus memilih mendaftar sebagai ?", Toast.LENGTH_SHORT).show()
        } else {

            /// proses registrasi user
            binding?.progressBar?.visibility = View.VISIBLE

            /// registrasikan user email & password
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        /// kita simpan data user ke database
                        saveUserToDatabase(fullName, username, email)
                    } else {
                        /// munculkan peringatan gagal register
                        binding?.progressBar?.visibility = View.GONE
                        try {
                            throw it.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            /// jika email sudah didaftarkan sebelumnya, maka user harus pake email lain
                            showFailureDialog("Email yang anda daftarkan sudah digunakan, silahkan coba email lain")
                        } catch (e: java.lang.Exception) {
                            Log.e("TAG", e.message!!)
                        }
                    }
                }
        }
    }

    private fun saveUserToDatabase(fullName: String, username: String, email: String) {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid

       /// kumpulan informasi dari user yang diisikan di kolom registrasi
        val data = mapOf(
            "uid" to uid,
            "fullName" to fullName,
            "username" to username,
            "email" to email,
            "role" to role,
        )

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    /// munculkan peringatan sukses mendaftar
                    binding?.progressBar?.visibility = View.GONE
                    showSuccessDialog()
                } else {
                    /// munculkan peringatan gagal register
                    binding?.progressBar?.visibility = View.GONE
                    showFailureDialog("Gagal melakukan registrasi")
                }
            }

    }

    fun chooseRole(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.user ->
                    if (checked) {
                        role = "user"
                    }
                R.id.merchant ->
                    if (checked) {
                        role = "merchant"
                    }
            }
        }
    }

    /// munculkan dialog ketika gagal registrasi
    private fun showFailureDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Gagal melakukan registrasi")
            .setMessage(message)
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    /// munculkan dialog ketika sukses registrasi
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Berhasil melakukan registrasi")
            .setMessage("Silahkan login menggunakan username dan kata sandi yang anda daftarkan")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
                onBackPressed()
            }
            .show()
    }


    /// method onDestroy itu merupakan method yang akan bekerja jika activity ini tidak digunakan
    /// tujuan: supaya tidak terjadi freeze atau kebocoran memori pada aplikasi
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}