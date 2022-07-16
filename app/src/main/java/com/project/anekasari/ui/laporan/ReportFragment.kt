package com.project.anekasari.ui.laporan

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.project.anekasari.databinding.FragmentReportBinding
import com.project.anekasari.ui.order.OrderAdapter
import com.project.anekasari.ui.order.OrderModel
import com.project.anekasari.ui.order.OrderViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private var adapter: OrderAdapter? = null
    private val reportList = ArrayList<OrderModel>()

    private var from = 0L
    private var to = 0L


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        initRecyclerView()
        initViewModel()

        return binding.root
    }

    private fun initView() {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Silahkan tunggu hingga proses selesai...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        Handler().postDelayed({
            progressDialog.dismiss()

            Log.e("Sasa", reportList.size.toString())

            if(reportList.size > 0) {
                binding.sellingProduct.text = "Total produk terjual : " + reportList.size.toString() + " Pcs"
                var totalOmset = 0L
                for(i in reportList.indices) {
                    totalOmset += reportList[i].totalPriceFinal?.plus(reportList[i].ongkir!!) ?: 0L
                }
                val formatter = DecimalFormat("#,###")
                binding.omset.text = "Omset penjualan : Rp." + formatter.format(totalOmset)
            } else {
                binding.sellingProduct.text = "Total produk terjual : 0 Pcs"
                binding.omset.text = "Omset penjualan : Rp.0"
            }

        }, 1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startDate.setOnClickListener {
            val datePicker: MaterialDatePicker<*> =
                MaterialDatePicker.Builder.datePicker().setTitleText("Filter Laporan Penjualan Dari Tanggal").build()
            datePicker.show(childFragmentManager, datePicker.toString())
            datePicker.addOnPositiveButtonClickListener { selection: Any ->
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val format = sdf.format(Date(selection.toString().toLong()))
                binding.startDate.text = format
                from = selection.toString().toLong() - 25200000
            }
        }

        binding.finishDate.setOnClickListener {
            val datePicker: MaterialDatePicker<*> =
                MaterialDatePicker.Builder.datePicker().setTitleText("Filter Laporan Penjualan Ke Tanggal").build()
            datePicker.show(childFragmentManager, datePicker.toString())
            datePicker.addOnPositiveButtonClickListener { selection: Any ->
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val format = sdf.format(Date(selection.toString().toLong()))
                binding.finishDate.text = format
                to = selection.toString().toLong() + 61200000
            }
        }

        binding.filter.setOnClickListener {
            if(from != 0L && to != 0L) {
                if (from < to) {
                    initRecyclerView()
                    initViewModelFilter()
                } else {
                    Toast.makeText(activity, "Tanggal Awal harus lebih kecil dibanding Tanggal Akhir", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(activity, "Anda harus menginputkan tanggal awal - tanggal akhir", Toast.LENGTH_SHORT).show()
            }
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
            reportList.clear()
            reportList.addAll(itemList)
            if (itemList.size > 0) {
                binding.noData.visibility = View.GONE
                adapter?.setData(reportList)
            } else {
                binding.noData.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
        }

        initView()

    }

    private fun initViewModelFilter() {
        val viewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        binding.progressBar.visibility = View.VISIBLE

        viewModel.setListOrderByDate(from, to)
        viewModel.getOrder().observe(viewLifecycleOwner) { itemList ->
            reportList.clear()
            reportList.addAll(itemList)
            if (itemList.size > 0) {
                binding.noData.visibility = View.GONE
                adapter?.setData(reportList)
            } else {
                binding.noData.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
        }

        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}