package com.example.holoshop.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.holoshop.R
import com.example.holoshop.models.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductPage : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 1
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_page)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val tvProductName: TextView = findViewById(R.id.shoppingList)
        val tvProductPrice: TextView = findViewById(R.id.tvProductPrice)
        val ivProductImage: ImageView = findViewById(R.id.ivProductImage)

        val productName = intent.getStringExtra("productName")
        val productPrice = intent.getStringExtra("productPrice")
        val productImage = intent.getStringExtra("productImage")
        val modelUrl = intent.getStringExtra("modelUrl")

        tvProductName.text = productName
        tvProductPrice.text = productPrice
        Glide.with(this)
            .load(productImage)
            .into(ivProductImage)

        val arBtn: Button = findViewById(R.id.arBtn)
        arBtn.setOnClickListener {
            val intent = Intent(this, arActivity::class.java)
            intent.putExtra("productName", productName)
            intent.putExtra("modelUrl", modelUrl)
            startActivity(intent)
        }

        val addToCartButton: Button = findViewById(R.id.cartBtn) // Update with your button ID
        addToCartButton.setOnClickListener {
            if (productName != null) {
                if (productPrice != null) {
                    if (productImage != null) {
                        addToCart(productName, productPrice, productImage)
                    }
                }
            }
        }
    }

    fun addToCart(productName: String, productPrice: String, productImage: String) {
        val userId = auth.currentUser?.uid // Get current user's ID
        if (userId != null) {
            val cartItem = hashMapOf(
                "productName" to productName,
                "productPrice" to productPrice,
                "productImage" to productImage
            )

            // Add item to the user's cart in Firestore
            firestore.collection("carts")
                .document(userId) // Use userId as document ID
                .collection("items")
                .add(cartItem) // Add the item to the subcollection
                .addOnSuccessListener { documentReference ->
                    // Item added successfully
                    Log.d(TAG, "Item added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    Log.w(TAG, "Error adding item", e)
                }
        } else {
            Log.w(TAG, "User is not logged in")
            // Optionally prompt the user to log in
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can start the AR activity
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(
                    this,
                    "Camera permission is required to use AR features.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
