package com.project.anekasari.ui.product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.anekasari.Homepage
import com.project.anekasari.LoginActivity
import com.project.anekasari.R
import com.project.anekasari.RegisterActivity
import com.project.anekasari.databinding.FragmentProductBinding

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private var adapter: ProductAdapter? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var isSearchProductShowedOrNot = false
    private var category: String? =null
    private var searchProduct: String? =null
    private var filter: String? =null

    override fun onResume() {
        super.onResume()
        getRole()
        when (filter) {
            null -> {
                initRecyclerView()
                initViewModel("all", "all")
            }
            "search" -> {
                initRecyclerView()
                initViewModel("all", searchProduct!!)
            }
            "category" -> {
                initRecyclerView()
                initViewModel(category!!, "all")
            }
        }
    }

    private fun getRole() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val role = "" + it.data!!["role"]
                val email = "" + it.data!!["email"]
                if(role == "merchant") {
                    binding.addProduct.visibility = View.VISIBLE
                    if(email == "admin@gmail.com") {
                        binding.addNewAdminBtn.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun initRecyclerView() {
        binding.productRv.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        adapter = ProductAdapter()
        binding.productRv.adapter = adapter
    }

    private fun initViewModel(category: String, searchProduct: String) {
        val viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        binding.progressBar.visibility = View.VISIBLE

        if(category == "all" && searchProduct == "all") {
            viewModel.setListProduct()
        } else if (searchProduct != "all") {
            viewModel.setListProductBySearch(searchProduct)
        } else if (category != "all") {
            viewModel.setListProductByCategory(category)
        }
        viewModel.getProduct().observe(viewLifecycleOwner) { productList ->
            if (productList.size > 0) {
                binding.noData.visibility = View.GONE
                adapter?.setData(productList)
            } else {
                binding.noData.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentProductBinding.inflate(inflater, container, false)

        showDropdownCategory()

        return binding.root
    }

    private fun showDropdownCategory() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.category, android.R.layout.simple_list_item_1
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding.homepageCategory.setAdapter(adapter)
        binding.homepageCategory.setOnItemClickListener { _, _, _, _ ->
            filter = "category"
            category = binding.homepageCategory.text.toString()
            if(category != "Semua") {
                initRecyclerView()
                initViewModel(category!!, "all")
            } else {
                initRecyclerView()
                initViewModel("all", "all")
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addProduct.setOnClickListener {
            startActivity(Intent(activity, AddProductActivity::class.java))
        }

        binding.switchBtn.setOnClickListener {
            if(!isSearchProductShowedOrNot) {
                binding.textInputLayout.visibility = View.VISIBLE
                binding.textInputLayout123.visibility = View.VISIBLE
                isSearchProductShowedOrNot = true
            } else {
                binding.textInputLayout.visibility = View.GONE
                binding.textInputLayout123.visibility = View.GONE
                isSearchProductShowedOrNot = false
            }
        }

        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(searchEt: Editable?) {
                searchProduct = searchEt.toString().trim()

                if(searchEt!!.isEmpty()) {
                    searchProduct = "all"
                    if(filter == "search") {
                        initRecyclerView()
                        initViewModel("all", "all")
                    }
                } else {
                    filter = "search"
                    initRecyclerView()
                    initViewModel("all", searchProduct!!)
                }
            }

        })


        binding.addNewAdminBtn.setOnClickListener {
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.putExtra(RegisterActivity.ROLE, "admin")
            startActivity(intent)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}