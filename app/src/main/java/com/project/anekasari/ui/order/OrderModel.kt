package com.project.anekasari.ui.order

import android.os.Parcelable
import com.project.anekasari.ui.keranjang.KeranjangModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderModel(

    var orderId : String? = null,
    var merchantId : String? = null,
    var userId : String? = null,
    var merchantName : String? = null,
    var date : String? = null,
    var paymentStatus : String? = null,
    var paymentProof : String? = null,
    var product : ArrayList<KeranjangModel>? = null,
    var address : String? = null,
    var phone : String? = null,
    var totalPriceFinal : Long? = 0L,

) : Parcelable