package com.project.anekasari

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    var binding : ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /// auto login berfungsi jika user sebelumnya pernah login,
        autoLogin()

        /// jika user klik ''Registrasi'', maka akan pindah ke halaman RegisterActivity
        binding?.registerBtn?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra(RegisterActivity.ROLE, "user")
            startActivity(intent)
        }


        /// jika user klik tombol login
        binding?.loginBtn?.setOnClickListener {
            formValidation()
        }

    }

    private fun autoLogin() {
        if(FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, Homepage::class.java))
            finish()
        }
    }

    private fun formValidation() {
        val username = binding?.username?.text.toString().trim()
        val password = binding?.password?.text.toString().trim()

        if(username.isEmpty()) {
            Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Kata sandi harus diisi", Toast.LENGTH_SHORT).show()
        } else {
            /// login proses
            binding?.progressBar?.visibility = View.VISIBLE

            /// cek apakah username dan password yang diinputkan pengguna ada di database atau tidak
            FirebaseFirestore
                .getInstance()
                .collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->

                    /// jika username ada di database maka, user bisa melanjutkan login
                    if(documents.size() > 0) {
                        for(document in documents) {

                            /// ambil email dari databsase
                            val email = "" + document.data["email"]

                            /// jika email sudah di ambil dari database, maka lakukan login
                            /// pakai email & password
                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener {
                                    if (it.isSuccessful){
                                        binding?.progressBar?.visibility = View.GONE
                                        startActivity(Intent(this, Homepage::class.java))
                                        finish()
                                    } else {
                                        binding?.progressBar?.visibility = View.GONE
                                        Toast.makeText(this, "Login Gagal, pastikan koneksi internet anda stabil!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        }
                    } else {
                        /// jika data username tidak ada di database, maka user tidak bisa melanjutkan login
                        binding?.progressBar?.visibility = View.GONE
                        showFailureDialog()
                    }
                }
        }
    }


    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Gagal Melakukan Login")
            .setMessage("Username atau Kata sandi salah, silahkan periksa username dan kata sandi anda!")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}