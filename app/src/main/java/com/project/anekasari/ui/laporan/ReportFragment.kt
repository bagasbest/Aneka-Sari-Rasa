package com.project.anekasari.ui.laporan

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.project.anekasari.LoginActivity
import com.project.anekasari.R
import com.project.anekasari.databinding.FragmentReportBinding
import com.project.anekasari.ui.order.OrderAdapter
import com.project.anekasari.ui.order.OrderModel
import com.project.anekasari.ui.order.OrderViewModel
import java.text.DecimalFormat


class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private var adapter: OrderAdapter? = null
    private val reportList = ArrayList<OrderModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        initRecyclerView()
        initViewModel()

        initView()

        return binding.root
    }

    private fun initView() {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Silahkan tunggu hingga proses selesai...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        Handler().postDelayed({
            progressDialog.dismiss()

            if(reportList.size > 0) {
                binding.sellingProduct.text = "Total produk terjual : " + reportList.size.toString() + " Pcs"
                var totalOmset = 0L
                for(i in reportList.indices) {
                    totalOmset += reportList[i].totalPriceFinal?.plus(reportList[i].ongkir!!) ?: 0L
                }
                val formatter = DecimalFormat("#,###")
                binding.omset.text = "Omset penjualan : Rp." + formatter.format(totalOmset)
            }

        }, 1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startDate.setOnClickListener {

        }

        binding.finishDate.setOnClickListener {

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

        viewModel.setListOrderByStatus("Order Dikirim")
        viewModel.getOrder().observe(viewLifecycleOwner) { itemList ->
            if (itemList.size > 0) {
                reportList.clear()
                reportList.addAll(itemList)
                binding.noData.visibility = View.GONE
                adapter?.setData(reportList)
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