package com.project.anekasari.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.R
import com.project.anekasari.databinding.FragmentOrderBinding

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private var paymentStatus : String ? = null
    private var adapter : OrderAdapter? = null
    private var role : String? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        checkRole()
    }

    private fun checkRole() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                role = "" + it.data!!["role"]
                initRecyclerView()
                initViewModel()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOrderBinding.inflate(inflater, container, false)

        showDropdownFilterPaymentStatus()

        return binding.root
    }

    private fun showDropdownFilterPaymentStatus() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_payment_status, android.R.layout.simple_list_item_1
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding.paymentStatus.setAdapter(adapter)
        binding.paymentStatus.setOnItemClickListener { _, _, _, _ ->
            paymentStatus = binding.paymentStatus.text.toString()
            initRecyclerView()
            initViewModel()
        }
    }

    private fun initRecyclerView() {
        binding.orderRv.layoutManager = LinearLayoutManager(activity)
        adapter = OrderAdapter()
        binding.orderRv.adapter = adapter
    }

    private fun initViewModel() {
        val viewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        binding.progressBar.visibility = View.VISIBLE

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if(role == "user") {
            if(paymentStatus != null) {
                viewModel.setListOrderByIdAndPaymentStatus(uid, paymentStatus!!)
            } else {
                viewModel.setListOrderById(uid)
            }
        } else {
            if(paymentStatus != null) {
                viewModel.setListOrderByMerchantIdAndPaymentStatus(uid, paymentStatus!!)
            } else {
                viewModel.setListOrderByMerchantId(uid)
            }
        }

        viewModel.getOrder().observe(viewLifecycleOwner) { orderList ->
            if (orderList.size > 0) {
                binding.noData.visibility = View.GONE
                adapter?.setData(orderList)
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