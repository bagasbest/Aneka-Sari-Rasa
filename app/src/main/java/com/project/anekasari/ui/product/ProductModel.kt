package com.project.anekasari.ui.product

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel (
    var productId : String ?  = null,
    var merchantName : String ?  = null,
    var merchantId : String ?  = null,
    var name : String ?  = null,
    var nameTemp : String ?  = null,
    var description : String ?  = null,
    var variant : String ?  = null,
    var category : String ?  = null,
    var image : String ?  = null,
    var price : Long ?  = 0L,
) : Parcelable