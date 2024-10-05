package com.example.holoshop.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.holoshop.activities.ProductPage
import android.content.Intent
import android.util.Log
import com.example.holoshop.model.Product

class SearchSuggestionsAdapter(
    private val context: Context,
    cursor: Cursor,
    private val productMap: Map<String, Product>  // Add productMap parameter
) : CursorAdapter(context, cursor, 0) {

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        // Log column names for debugging
        val columnCount = cursor.columnCount
        for (i in 0 until columnCount) {
            Log.d("Cursor Column", cursor.getColumnName(i))  // Log each column name
        }

        val textView: TextView = view.findViewById(android.R.id.text1)
        val suggestionIndex = cursor.getColumnIndex("name")  // Get index of the column

        if (suggestionIndex != -1) {
            val suggestion = cursor.getString(suggestionIndex)
            textView.text = suggestion

            view.setOnClickListener {
                val product = productMap[suggestion]  // Retrieve product from the map
                if (product != null) {
                    val intent = Intent(context, ProductPage::class.java)
                    intent.putExtra("productName", product.name)
                    intent.putExtra("productPrice", product.price)
                    intent.putExtra("productImage", product.imageUrl)
                    intent.putExtra("modelUrl", product.modelUrl)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("Cursor Error", "Column 'name' not found in cursor")
        }
    }

}
