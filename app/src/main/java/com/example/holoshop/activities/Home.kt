package com.example.holoshop.activities

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.holoshop.R
import com.example.holoshop.model.Product
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val pic = findViewById<ImageView>(R.id.profilePic)
        pic.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }

        val botnav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        botnav.selectedItemId = R.id.bottom_home
        botnav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, Search_Page::class.java))
                    finish()
                    true
                }
                R.id.bottom_cart-> {
                    startActivity(Intent(applicationContext, CartPage::class.java))
                    finish()
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfilePage::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        FirebaseApp.initializeApp(this)
        recyclerView = findViewById(R.id.rvProductList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        fetchProductsFromFirebase()
    }
    private fun fetchProductsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show()
            }
    }
}
