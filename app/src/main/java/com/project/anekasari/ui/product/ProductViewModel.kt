package com.project.anekasari.ui.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewModel : ViewModel() {

    private val productList = MutableLiveData<ArrayList<ProductModel>>()
    private val listData = ArrayList<ProductModel>()
    private val TAG = ProductViewModel::class.java.simpleName


    fun setListProduct() {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("product")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = ProductModel()

                        model.productId = document.data["productId"].toString()
                        model.image = document.data["image"].toString()
                        model.name = document.data["name"].toString()
                        model.nameTemp = document.data["nameTemp"].toString()
                        model.merchantName = document.data["merchantName"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.price = document.data["price"] as Long
                        model.description = document.data["description"].toString()
                        model.variant = document.data["variant"].toString()
                        model.category = document.data["category"].toString()

                        listData.add(model)
                    }
                    productList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun setListProductByCategory(category: String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("product")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = ProductModel()

                        model.productId = document.data["productId"].toString()
                        model.image = document.data["image"].toString()
                        model.name = document.data["name"].toString()
                        model.nameTemp = document.data["nameTemp"].toString()
                        model.merchantName = document.data["merchantName"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.price = document.data["price"] as Long
                        model.description = document.data["description"].toString()
                        model.variant = document.data["variant"].toString()
                        model.category = document.data["category"].toString()

                        listData.add(model)
                    }
                    productList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }


    fun setListProductBySearch(searchProduct: String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("product")
                .whereGreaterThanOrEqualTo("nameTemp", searchProduct)
                .whereLessThanOrEqualTo("nameTemp", searchProduct + '\uf8ff')
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = ProductModel()

                        model.productId = document.data["productId"].toString()
                        model.image = document.data["image"].toString()
                        model.name = document.data["name"].toString()
                        model.nameTemp = document.data["nameTemp"].toString()
                        model.merchantName = document.data["merchantName"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.price = document.data["price"] as Long
                        model.description = document.data["description"].toString()
                        model.variant = document.data["variant"].toString()
                        model.category = document.data["category"].toString()

                        listData.add(model)
                    }
                    productList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }



    fun getProduct() : LiveData<ArrayList<ProductModel>> {
        return productList
    }



}

