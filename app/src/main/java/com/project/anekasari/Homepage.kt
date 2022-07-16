package com.project.anekasari

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.databinding.ActivityHomepageBinding

class Homepage : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRole()
    }

    private fun checkRole() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Silahkan tunggu hingga proses selesai...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val role = "" + it.data!!["role"]

                if(role == "user") {
                    binding.clUser.visibility =  View.VISIBLE
                    val navView: BottomNavigationView = binding.navView

                    val navController = findNavController(R.id.nav_host_fragment_activity_homepage)
                    // Passing each menu ID as a set of Ids because each
                    // menu should be considered as top level destinations.
                    navView.setupWithNavController(navController)
                } else {
                    binding.clAdmin.visibility =  View.VISIBLE
                    val navView2: BottomNavigationView = binding.navView2

                    val navController2 = findNavController(R.id.nav_host_fragment_activity_homepage2)
                    // Passing each menu ID as a set of Ids because each
                    // menu should be considered as top level destinations.
                    navView2.setupWithNavController(navController2)
                }
                progressDialog.dismiss()
            }
    }
}