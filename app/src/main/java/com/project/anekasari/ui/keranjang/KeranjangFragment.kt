package com.project.anekasari.ui.keranjang

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.R
import com.project.anekasari.databinding.FragmentKeranjangBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class KeranjangFragment : Fragment() {

    private var _binding: FragmentKeranjangBinding? = null
    private var adapter: KeranjangAdapter? = null
    private var listOfCart = ArrayList<KeranjangModel>()
    private var progressDialog: ProgressDialog? = null
    private var merchantIdMatcher = ""
    private var listOfCheckout = ArrayList<KeranjangModel>()
    private var totalPriceCheckout = 0L
    private var isWaiting = false
    private var ongkirChoice = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentKeranjangBinding.inflate(inflater, container, false)

        progressDialog = ProgressDialog(activity)
        progressDialog?.setMessage("Sedang memuat halaman...")
        progressDialog?.show()

        initRecyclerView()
        initViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkoutBtn.setOnClickListener {
            showPopupFillAddressAndPhone()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showPopupFillAddressAndPhone() {
        val addressEt: TextInputEditText
        val phoneEt: TextInputEditText
        val totalPrice: TextView
        val productPrice: TextView
        val sendPrice: TextView
        val confirmBtn: Button
        val pb: ProgressBar
        val innerProvenceRb : Button
        val outerProvenceRb : Button
        val dialog = Dialog(requireContext())
        var totalPriceFinal = 0L
        dialog.setContentView(R.layout.popup_cart)

        addressEt = dialog.findViewById(R.id.address)
        phoneEt = dialog.findViewById(R.id.phone)
        totalPrice = dialog.findViewById(R.id.totalPrice)
        productPrice = dialog.findViewById(R.id.productPrice)
        sendPrice = dialog.findViewById(R.id.sendPrice)
        innerProvenceRb = dialog.findViewById(R.id.innerProvince)
        outerProvenceRb = dialog.findViewById(R.id.outerProvince)
        confirmBtn = dialog.findViewById(R.id.confirmBtn)
        pb = dialog.findViewById(R.id.progressBar)
        val formatter = DecimalFormat("#,###")

        innerProvenceRb.setOnClickListener {
            innerProvenceRb.backgroundTintList = ContextCompat.getColorStateList(it.context, android.R.color.holo_green_dark)
            outerProvenceRb.backgroundTintList = ContextCompat.getColorStateList(it.context, R.color.red)

            ongkirChoice = 20000
            sendPrice.text = "Biaya Ongkir: Rp.${formatter.format(ongkirChoice)}"
            totalPrice.text = "Total Biaya: Rp.${formatter.format(totalPriceFinal + ongkirChoice)}"
        }

        outerProvenceRb.setOnClickListener {
            outerProvenceRb.backgroundTintList = ContextCompat.getColorStateList(it.context, android.R.color.holo_green_dark)
            innerProvenceRb.backgroundTintList = ContextCompat.getColorStateList(it.context, R.color.red)
            ongkirChoice = 50000
            sendPrice.text = "Biaya Ongkir: Rp.${formatter.format(ongkirChoice)}"
            totalPrice.text = "Total Biaya: Rp.${formatter.format(totalPriceFinal + ongkirChoice)}"
        }

        for (index in listOfCart.indices) {
            totalPriceFinal += listOfCart[index].price!!
        }

        productPrice.text = "Total biaya: Rp.${formatter.format(totalPriceFinal)}"

        confirmBtn?.setOnClickListener {
            val address = addressEt.text.toString().trim()
            val phone = phoneEt.text.toString().trim()

            if (address.isEmpty()) {
                Toast.makeText(activity, "Maaf, alamat tidak boleh kosong!", Toast.LENGTH_SHORT)
                    .show()
            } else if (phone.isEmpty()) {
                Toast.makeText(
                    activity,
                    "Maaf, No.Handphone tidak boleh kosong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (ongkirChoice == 0) {
                Toast.makeText(
                    activity,
                    "Maaf, Silahkan pilih ongkos kirim!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                pb.visibility = View.VISIBLE

                for (index in listOfCart.indices) {
                    if (merchantIdMatcher == listOfCart[index].merchantId || merchantIdMatcher == "") {
                        if (index == listOfCart.size - 1) {
                            if(isWaiting) {
                                Handler().postDelayed({
                                    totalPriceCheckout += listOfCart[index].price!!
                                    isWaiting = false
                                    listOfCheckout.add(listOfCart[index])
                                    merchantIdMatcher = listOfCart[index].merchantId!!
                                    /// go input data
                                    goInputData(address, phone, pb, dialog, index)
                                },1000)
                            } else {
                                totalPriceCheckout += listOfCart[index].price!!
                                listOfCheckout.add(listOfCart[index])
                                merchantIdMatcher = listOfCart[index].merchantId!!
                                /// go input data
                                goInputData(address, phone, pb, dialog, index)
                            }
                        } else {
                            if(isWaiting) {
                                Handler().postDelayed({
                                    totalPriceCheckout += listOfCart[index].price!!
                                    isWaiting = false
                                    listOfCheckout.add(listOfCart[index])
                                    merchantIdMatcher = listOfCart[index].merchantId!!
                                },1000)
                            } else {
                                totalPriceCheckout += listOfCart[index].price!!
                                listOfCheckout.add(listOfCart[index])
                                merchantIdMatcher = listOfCart[index].merchantId!!
                            }
                        }
                    } else {
                        if (index == listOfCart.size - 1) {
                            merchantIdMatcher = listOfCart[index].merchantId!!
                            /// go input data
                            goInputDataButFinish(address, phone, index, pb, dialog)
                        } else {
                            isWaiting = true
                            merchantIdMatcher = listOfCart[index].merchantId!!
                            /// go input data
                            goInputDataButNotFinish(address, phone, index)
                        }
                    }
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun goInputData(
        address: String,
        phone: String,
        pb: ProgressBar,
        dialog: Dialog,
        index: Int
    ) {
        val orderId = System.currentTimeMillis().toString()

        val calendar = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy, HH:mm", Locale.getDefault())
        val formattedDate = df.format(calendar.time)

        val data = mapOf(
            "orderId" to orderId,
            "merchantId" to listOfCart[index].merchantId,
            "userId" to listOfCart[0].userId,
            "merchantName" to listOfCart[index].merchantName,
            "date" to formattedDate,
            "paymentStatus" to "Belum Bayar",
            "paymentProof" to "",
            "ongkir" to ongkirChoice.toLong(),
            "product" to listOfCheckout,
            "address" to address,
            "phone" to phone,
            "totalPriceFinal" to totalPriceCheckout,
        )

        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(orderId)
            .set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    deleteCart(dialog, pb)
                } else {
                    dialog.dismiss()
                    pb.visibility = View.GONE
                    showFailureDialog()
                }
            }
    }

    private fun goInputDataButFinish(
        address: String,
        phone: String,
        index: Int,
        pb: ProgressBar,
        dialog: Dialog
    ) {
        val orderId = System.currentTimeMillis().toString()

        val calendar = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy, HH:mm", Locale.getDefault())
        val formattedDate = df.format(calendar.time)

        val data = mapOf(
            "orderId" to orderId,
            "merchantId" to listOfCart[index-1].merchantId,
            "userId" to listOfCart[0].userId,
            "merchantName" to listOfCart[index-1].merchantName,
            "date" to formattedDate,
            "paymentStatus" to "Belum Bayar",
            "paymentProof" to "",
            "product" to listOfCheckout,
            "ongkir" to ongkirChoice.toLong(),
            "address" to address,
            "phone" to phone,
            "totalPriceFinal" to totalPriceCheckout,
        )

        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(orderId)
            .set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    listOfCheckout.clear()
                    totalPriceCheckout = 0L
                    listOfCheckout.add(listOfCart[index])
                    totalPriceCheckout += listOfCart[index].price!!
                    // go input data
                    goInputData(address, phone, pb, dialog, index)
                }
            }
    }

    private fun goInputDataButNotFinish(address: String, phone: String, index: Int) {
        val orderId = System.currentTimeMillis().toString()

        val calendar = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy, HH:mm", Locale.getDefault())
        val formattedDate = df.format(calendar.time)

        val data = mapOf(
            "orderId" to orderId,
            "merchantId" to listOfCart[index-1].merchantId,
            "userId" to listOfCart[0].userId,
            "merchantName" to listOfCart[index-1].merchantName,
            "date" to formattedDate,
            "paymentStatus" to "Belum Bayar",
            "ongkir" to ongkirChoice.toLong(),
            "paymentProof" to "",
            "product" to listOfCheckout,
            "address" to address,
            "phone" to phone,
            "totalPriceFinal" to totalPriceCheckout,
        )

        FirebaseFirestore
            .getInstance()
            .collection("order")
            .document(orderId)
            .set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    listOfCheckout.clear()
                    totalPriceCheckout = 0L
                    listOfCheckout.add(listOfCart[index])
                    totalPriceCheckout += listOfCart[index].price!!
                }
            }
    }


    private fun deleteCart(dialog: Dialog, pb: ProgressBar) {
        for (index in listOfCart.indices) {
            FirebaseFirestore
                .getInstance()
                .collection("cart")
                .document(listOfCart[index].cartId!!)
                .delete()
        }

        Handler().postDelayed({
            listOfCart.clear()
            binding.checkoutBtn.isEnabled = false
            initRecyclerView()
            initViewModel()
            pb.visibility = View.GONE
            dialog.dismiss()
            showSuccessDialog()
        }, 1000)
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Gagal Membuat Order Baru")
            .setMessage("Ups, koneksi internet anda sedang bermasalah, coba lagi nanti!")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil Membuat Order Baru")
            .setMessage("Order anda berhasil dibuat, anda dapat melakukan pembayaran pada navigasi order")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton("OKE") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun initRecyclerView() {
        Handler().postDelayed({
            progressDialog?.dismiss()
            listOfCart.sortBy { it.merchantId }
            binding.cartRv.layoutManager = LinearLayoutManager(activity)
            adapter = KeranjangAdapter(listOfCart, binding.checkoutBtn)
            binding.cartRv.adapter = adapter
        }, 1000)
    }

    private fun initViewModel() {
        val viewModel = ViewModelProvider(this)[KeranjangViewModel::class.java]
        binding.progressBar.visibility = View.VISIBLE

        val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
        viewModel.setListCartById(userId = currentUserUid)
        viewModel.getCartById().observe(viewLifecycleOwner) { cartList ->
            if (cartList.size > 0) {
                listOfCart.clear()
                binding.noData.visibility = View.GONE
                listOfCart.addAll(cartList)
            } else {
                binding.noData.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}