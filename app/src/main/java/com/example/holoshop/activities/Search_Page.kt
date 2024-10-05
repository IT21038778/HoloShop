package com.example.holoshop.activities

import ProductAdapter
import android.database.MatrixCursor
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.holoshop.R
import com.example.holoshop.adapter.SearchSuggestionsAdapter
import com.example.holoshop.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Search_Page : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var productList: ArrayList<Product> = ArrayList()
    private var productNames: ArrayList<String> = ArrayList()
    private val productMap = mutableMapOf<String, Product>()// Store product names for suggestions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_page)

        db = FirebaseFirestore.getInstance()
        searchView = findViewById(R.id.searchView)  // Initialize SearchView
        searchResultsRecyclerView = findViewById(R.id.searchRecyclerView)

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        productAdapter = ProductAdapter(productList)
        searchResultsRecyclerView.adapter = productAdapter

        // Fetch product names for suggestions
        fetchProductsFromFirebase()
        fetchProductNames()
        // Set the SearchView listener to perform product searches
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)) {
                    searchProducts(query!!)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!TextUtils.isEmpty(newText)) {
                    showSuggestions(newText!!)
                } else {
                    searchView.suggestionsAdapter = null // Clear suggestions if input is empty
                }
                return true
            }
        })
    }

    private fun fetchProductsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                    // Populate the productMap with the product name as key
                    productMap[product.name] = product
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProductNames() {
        db.collection("products")
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                for (document in querySnapshot) {
                    val productName = document.getString("name")
                    if (!productName.isNullOrEmpty()) {
                        productNames.add(productName)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch product names", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showSuggestions(query: String) {
        val cursor = MatrixCursor(arrayOf("_id", "name"))  // Define column names

        val filteredNames = productNames.filter { it.contains(query, ignoreCase = true) }

        Log.d("FilteredNames", "Filtered names: $filteredNames") // Add this log

        if (filteredNames.isNotEmpty()) {
            for (name in filteredNames) {
                cursor.addRow(arrayOf(name.hashCode().toString(), name))  // Add rows to cursor
            }
            // Pass productMap to the adapter
            val adapter = SearchSuggestionsAdapter(this, cursor, productMap)
            searchView.suggestionsAdapter = adapter
        } else {
            searchView.suggestionsAdapter = null  // Clear suggestions if none match
        }
    }

    private fun searchProducts(query: String) {
        db.collection("products")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                productList.clear()
                for (document in querySnapshot) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to search products", Toast.LENGTH_SHORT).show()
            }
    }
}

