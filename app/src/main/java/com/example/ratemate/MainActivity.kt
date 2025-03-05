package com.example.ratemate

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rememberMeCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RateMatePrefs", MODE_PRIVATE)

        // Get references to the views
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)

        // Check if email is saved in SharedPreferences
        val savedEmail = sharedPreferences.getString("SAVED_EMAIL", null)
        if (savedEmail != null) {
            emailEditText.setText(savedEmail)
            rememberMeCheckbox.isChecked = true
        }

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if the email or password is empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading indicator while processing
            loadingIndicator.visibility = View.VISIBLE

            // Perform Firebase authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Hide the loading indicator
                    loadingIndicator.visibility = View.GONE

                    if (task.isSuccessful) {
                        // Save email in SharedPreferences if "Remember Me" is checked
                        if (rememberMeCheckbox.isChecked) {
                            sharedPreferences.edit().putString("SAVED_EMAIL", email).apply()
                        } else {
                            sharedPreferences.edit().remove("SAVED_EMAIL").apply()
                        }

                        // On success, navigate to Home Activity
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // Close the login activity
                    } else {
                        // On failure, show the error message
                        Toast.makeText(
                            this,
                            "Authentication Failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Method to handle SignUp link click (to navigate to SignUpActivity)
    fun onSignupClicked(view: View) {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }
}
