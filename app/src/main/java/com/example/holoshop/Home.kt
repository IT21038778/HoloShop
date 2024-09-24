package com.example.holoshop

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.holoshop.databinding.ActivityHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var productList: ArrayList<Product>
    private lateinit var adapter: ProductAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productList = ArrayList()
        adapter = ProductAdapter(productList)

        binding.rvProductList.layoutManager = LinearLayoutManager(this)
        binding.rvProductList.adapter = adapter

        fetchProductsFromFirebase()
    }

    private fun fetchProductsFromFirebase() {
        firestore.collection("products").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val productName = document.getString("name") ?: ""
                    val productPrice = document.getString("price") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    // Load image from Firebase Storage
                    val product = Product(productName, productPrice, imageUrl)
                    productList.add(product)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting products: ", exception)
            }
    }
}
