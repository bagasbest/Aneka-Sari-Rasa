package com.project.anekasari.ui.keranjang

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KeranjangModel(
    var cartId : String? = null,
    var merchantId : String? = null,
    var productId : String? = null,
    var userId : String? = null,
    var image : String? = null,
    var merchantName : String? = null,
    var name : String? = null,
    var description : String? = null,
    var category : String? = null,
    var price : Long? = 0L,
    var variant : String? = null,
    var nameTemp : String? = null,
    var qty : String? = null,
) : Parcelable