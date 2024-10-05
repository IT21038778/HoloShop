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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class sign_in : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.pass)
        val loginBtn = findViewById<Button>(R.id.login)
        val createAccountText = findViewById<TextView>(R.id.create)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Login button click listener
        loginBtn.setOnClickListener {
            val input = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if input is email or username
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                // Input is an email, sign in using Firebase Authentication
                signInWithEmail(input, password)
            } else {
                // Input is a username, get the corresponding email from Firestore
                signInWithUsername(input, password)
            }
        }

        // Redirect to sign-up page
        createAccountText.setOnClickListener {
            val intent = Intent(this, sign_up::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successful sign in, navigate to HomePageActivity
                    navigateToHomePage()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithUsername(username: String, password: String) {
        // Query Firestore to get the email corresponding to the username
        db.collection("Users").whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Get the first document (user) from the query result
                val userDocument = documents.first()
                val email = userDocument.getString("email")

                // Sign in using the email found in Firestore
                if (email != null) {
                    signInWithEmail(email, password)
                } else {
                    Toast.makeText(this, "Email not found for this username", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, Home::class.java) // Change to your Home activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}