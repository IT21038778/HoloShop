package com.example.holoshop.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.holoshop.R
import com.example.holoshop.R.id.bottomNavigation
import com.example.holoshop.R.id.bottom_cart
import com.example.holoshop.R.id.bottom_home
import com.example.holoshop.R.id.bottom_profile
import com.example.holoshop.R.id.bottom_search
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class CartPage : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvEmptyCart: TextView
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var ivProductImage: ImageView
    private lateinit var paymentProceed: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart_page)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        checkCart()

        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        tvProductName = findViewById(R.id.tvCartProductName)
        tvProductPrice = findViewById(R.id.tvCartProductPrice)
        ivProductImage = findViewById(R.id.ivCartProductImage)
        paymentProceed = findViewById(R.id.paymentProceed)

        val botnav = findViewById<BottomNavigationView>(bottomNavigation)
        botnav.selectedItemId = bottom_cart
        botnav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                bottom_home -> {
                    startActivity(Intent(applicationContext, Home::class.java))
                    finish()
                    true
                }
                bottom_search -> {
                    startActivity(Intent(applicationContext, Search_Page::class.java))
                    finish()
                    true
                }
                bottom_cart -> true
                bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfilePage::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkCart() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        tvEmptyCart.visibility = View.VISIBLE // Show empty cart message
                    } else {
                        tvEmptyCart.visibility = View.GONE // Hide empty cart message
                        // Assuming you only want to show the first item
                        val firstItem = snapshot.documents.first()
                        displayProduct(firstItem)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        } else {
            // User is not logged in, show empty cart
            tvEmptyCart.visibility = View.VISIBLE
        }
    }

    private fun displayProduct(product: DocumentSnapshot) {
        // Log the document data for debugging
        Log.d("CartPage", "Product data: ${product.data}")

        val productName = product.getString("productName")
        val productPrice = product.getString("productPrice")
        val productImage = product.getString("productImage")

        // Log the retrieved values
        Log.d("CartPage", "Product Name: $productName")
        Log.d("CartPage", "Product Price: $productPrice")
        Log.d("CartPage", "Product Image URL: $productImage")

        // Check for null values
        tvProductName.text = productName ?: "Unknown Product"
        tvProductPrice.text = productPrice ?: "0"
        Glide.with(this).load(productImage).into(ivProductImage)
    }

}