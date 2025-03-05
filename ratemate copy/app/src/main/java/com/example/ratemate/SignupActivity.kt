package com.example.ratemate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get references to the views
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)

        // Handle sign-up button click
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            // Check if the email, password, or username is empty
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please enter email, password, and username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading indicator while processing
            loadingIndicator.visibility = View.VISIBLE

            // Perform Firebase sign-up
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Hide the loading indicator
                    loadingIndicator.visibility = View.GONE

                    if (task.isSuccessful) {
                        // On success, save the username to Firestore
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserToFirestore(userId, email, username)
                        }

                        // Navigate to Login Activity
                        Toast.makeText(this, "Sign Up Successful! Please log in.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close the sign-up activity
                    } else {
                        // On failure, show the error message
                        Toast.makeText(
                            this,
                            "Sign Up Failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveUserToFirestore(userId: String, email: String, username: String) {
        // Create a user data object
        val userData = hashMapOf(
            "email" to email,
            "username" to username
        )

        // Save the user data to Firestore
        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("SignupActivity", "User data saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("SignupActivity", "Error saving user data: ${e.message}")
            }
    }
}