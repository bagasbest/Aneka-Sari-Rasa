package com.project.anekasari.ui.keranjang

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class KeranjangViewModel : ViewModel() {

    private val cartList = MutableLiveData<ArrayList<KeranjangModel>>()
    private val listData = ArrayList<KeranjangModel>()
    private val TAG = KeranjangViewModel::class.java.simpleName


    fun setListCartById(userId: String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = KeranjangModel()

                        model.productId = document.data["productId"].toString()
                        model.cartId = document.data["cartId"].toString()
                        model.image = document.data["image"].toString()
                        model.name = document.data["name"].toString()
                        model.nameTemp = document.data["nameTemp"].toString()
                        model.merchantName = document.data["merchantName"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.price = document.data["price"] as Long
                        model.description = document.data["description"].toString()
                        model.variant = document.data["variant"].toString()
                        model.category = document.data["category"].toString()
                        model.userId = document.data["userId"].toString()
                        model.qty = document.data["qty"].toString()

                        listData.add(model)
                    }
                    cartList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun getCartById() : LiveData<ArrayList<KeranjangModel>> {
        return cartList
    }

}