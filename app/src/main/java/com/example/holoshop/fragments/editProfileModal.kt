package com.example.holoshop.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.holoshop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class editProfileModal : DialogFragment() {
    private lateinit var emailField: EditText
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var saveButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile_modal, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailField = view.findViewById(R.id.editEmail)
        usernameField = view.findViewById(R.id.editUsername)
        passwordField = view.findViewById(R.id.editPassword)
        saveButton = view.findViewById(R.id.saveButton)

        loadCurrentUserData()

        saveButton.setOnClickListener {
            updateUserData()
        }

        return view
    }

    private fun loadCurrentUserData() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        emailField.setText(document.getString("email"))
                        usernameField.setText(document.getString("username"))
                    }
                }
        }
    }

    private fun updateUserData() {
        val newEmail = emailField.text.toString().trim()
        val newUsername = usernameField.text.toString().trim()
        val newPassword = passwordField.text.toString().trim()

        val user = auth.currentUser
        user?.let {
            val userId = it.uid

            // Update email in Firebase Auth
            if (newEmail.isNotEmpty()) {
                user.updateEmail(newEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update in Firestore
                        db.collection("Users").document(userId)
                            .update(mapOf("email" to newEmail))
                    } else {
                        Toast.makeText(context, "Failed to update email", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Update password in Firebase Auth
            if (newPassword.isNotEmpty()) {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (newUsername.isNotEmpty()) {
                db.collection("Users").document(userId)
                    .update("username", newUsername)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()  // Use requireContext() instead of context
                        this.dismiss()  // Close the dialog
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()  // Use requireContext() here too
                    }
            }
        }
    }
}