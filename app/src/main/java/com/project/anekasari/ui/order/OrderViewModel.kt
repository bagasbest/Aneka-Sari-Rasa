package com.project.anekasari.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class OrderViewModel : ViewModel() {

    private val orderList = MutableLiveData<ArrayList<OrderModel>>()
    private val listData = ArrayList<OrderModel>()
    private val TAG = OrderViewModel::class.java.simpleName


    fun setListOrderById(userId: String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("order")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = OrderModel()


                        model.orderId = document.data["orderId"].toString()
                        model.dateInMillis = document.data["dateInMillis"] as Long
                        model.merchantName = document.data["merchantName"].toString()
                        model.userId = document.data["userId"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.date = document.data["date"].toString()
                        model.paymentStatus = document.data["paymentStatus"].toString()
                        model.paymentProof = document.data["paymentProof"].toString()
                        model.address = document.data["address"].toString()
                        model.phone = document.data["phone"].toString()
                        model.totalPriceFinal = document.data["totalPriceFinal"] as Long
                        model.ongkir = document.data["ongkir"] as Long
                        model.product = document.toObject(OrderModel::class.java).product

                        listData.add(model)
                    }
                    orderList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun setListOrderByStatus(paymentStatus : String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("order")
                .whereEqualTo("paymentStatus", paymentStatus)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = OrderModel()


                        model.orderId = document.data["orderId"].toString()
                        model.dateInMillis = document.data["dateInMillis"] as Long
                        model.merchantName = document.data["merchantName"].toString()
                        model.userId = document.data["userId"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.date = document.data["date"].toString()
                        model.paymentStatus = document.data["paymentStatus"].toString()
                        model.paymentProof = document.data["paymentProof"].toString()
                        model.address = document.data["address"].toString()
                        model.phone = document.data["phone"].toString()
                        model.totalPriceFinal = document.data["totalPriceFinal"] as Long
                        model.ongkir = document.data["ongkir"] as Long
                        model.product = document.toObject(OrderModel::class.java).product

                        listData.add(model)
                    }
                    orderList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun setListOrderByIdAndPaymentStatus(uid: String, paymentStatus: String) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("order")
                .whereEqualTo("userId", uid)
                .whereEqualTo("paymentStatus", paymentStatus)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = OrderModel()


                        model.orderId = document.data["orderId"].toString()
                        model.dateInMillis = document.data["dateInMillis"] as Long
                        model.merchantName = document.data["merchantName"].toString()
                        model.userId = document.data["userId"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.date = document.data["date"].toString()
                        model.paymentStatus = document.data["paymentStatus"].toString()
                        model.paymentProof = document.data["paymentProof"].toString()
                        model.address = document.data["address"].toString()
                        model.phone = document.data["phone"].toString()
                        model.totalPriceFinal = document.data["totalPriceFinal"] as Long
                        model.ongkir = document.data["ongkir"] as Long
                        model.product = document.toObject(OrderModel::class.java).product

                        listData.add(model)
                    }
                    orderList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun setListOrderByAll() {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("order")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = OrderModel()


                        model.orderId = document.data["orderId"].toString()
                        model.dateInMillis = document.data["dateInMillis"] as Long
                        model.merchantName = document.data["merchantName"].toString()
                        model.userId = document.data["userId"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.date = document.data["date"].toString()
                        model.paymentStatus = document.data["paymentStatus"].toString()
                        model.paymentProof = document.data["paymentProof"].toString()
                        model.address = document.data["address"].toString()
                        model.phone = document.data["phone"].toString()
                        model.totalPriceFinal = document.data["totalPriceFinal"] as Long
                        model.ongkir = document.data["ongkir"] as Long
                        model.product = document.toObject(OrderModel::class.java).product

                        listData.add(model)
                    }
                    orderList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }


    fun setListOrderByDate(from: Long, to: Long) {
        listData.clear()

        try {
            FirebaseFirestore.getInstance().collection("order")
                .whereGreaterThanOrEqualTo("dateInMillis", from)
                .whereLessThanOrEqualTo("dateInMillis", to)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = OrderModel()


                        model.orderId = document.data["orderId"].toString()
                        model.dateInMillis = document.data["dateInMillis"] as Long
                        model.merchantName = document.data["merchantName"].toString()
                        model.userId = document.data["userId"].toString()
                        model.merchantId = document.data["merchantId"].toString()
                        model.date = document.data["date"].toString()
                        model.paymentStatus = document.data["paymentStatus"].toString()
                        model.paymentProof = document.data["paymentProof"].toString()
                        model.address = document.data["address"].toString()
                        model.phone = document.data["phone"].toString()
                        model.totalPriceFinal = document.data["totalPriceFinal"] as Long
                        model.ongkir = document.data["ongkir"] as Long
                        model.product = document.toObject(OrderModel::class.java).product

                        listData.add(model)
                    }
                    orderList.postValue(listData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun getOrder() : LiveData<ArrayList<OrderModel>> {
        return orderList
    }
}