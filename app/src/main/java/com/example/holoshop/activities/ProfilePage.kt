package com.example.holoshop.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.holoshop.R
import com.example.holoshop.R.id.bottomNavigation
import com.example.holoshop.R.id.bottom_cart
import com.example.holoshop.R.id.bottom_home
import com.example.holoshop.R.id.bottom_profile
import com.example.holoshop.R.id.bottom_search
import com.example.holoshop.fragments.editProfileModal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailField: TextView
    private lateinit var usernameField: TextView
    private lateinit var passwordField: TextView
    private lateinit var editBtn: Button
    private lateinit var deleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailField = findViewById(R.id.Pemail)
        usernameField = findViewById(R.id.Pusername)
        passwordField = findViewById(R.id.Ppass)
        editBtn = findViewById(R.id.editBtn)
        deleteBtn = findViewById(R.id.deletebtn)

        loadUserData()

        editBtn.setOnClickListener {
            val dialog = editProfileModal()
            dialog.show(supportFragmentManager, "EditProfileDialog")
        }


        // Bottom navigation setup
        val botnav = findViewById<BottomNavigationView>(bottomNavigation)
        botnav.selectedItemId = bottom_profile

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
                bottom_cart -> {
                    startActivity(Intent(applicationContext, CartPage::class.java))
                    finish()
                    true
                }
                bottom_profile -> true
                else -> false
            }
        }

        deleteBtn.setOnClickListener {
            deleteUserAccount()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val email = document.getString("email") ?: "N/A"
                        val username = document.getString("username") ?: "N/A"
                        emailField.text = email
                        usernameField.text = username
                        passwordField.text = "******" // Display a placeholder for security
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error here
                }
        }
    }

    private fun deleteUserAccount() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid

            // Delete user data from Firestore
            db.collection("Users").document(userId).delete()
                .addOnSuccessListener {
                    // Delete user from Firebase Authentication
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                                // Redirect to Splash screen after deletion
                                val intent = Intent(this, SplashScreen::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}