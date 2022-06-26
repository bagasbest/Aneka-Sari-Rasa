package com.project.anekasari

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.bumptech.glide.Glide
import com.project.anekasari.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        Glide.with(this)
            .load(R.drawable.bg1)
            .into(binding!!.background)

        Glide.with(this)
            .load(R.drawable.logo_aplikasi)
            .into(binding!!.logoAplikasi)


        Handler().postDelayed({
            /// setelah menunggu 3 detik, activity akan pindah ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}