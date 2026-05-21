// RegisterActivity.kt
package com.example.studentmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = StudentDatabaseHelper(this)

        val usernameEt: EditText = findViewById(R.id.usernameEt)
        val emailEt: EditText = findViewById(R.id.emailEt)
        val passwordEt: EditText = findViewById(R.id.passwordEt)
        val registerBtn: Button = findViewById(R.id.registerBtn)
        val loginBtn: Button = findViewById(R.id.loginBtn)

        loginBtn.setOnClickListener {
            finish()
        }

        registerBtn.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            Log.d(TAG, "Attempting to register user: $username, $email")

            if (username.isEmpty()) {
                usernameEt.error = "Username is required"
                usernameEt.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailEt.error = "Email is required"
                emailEt.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.error = "Enter a valid email"
                emailEt.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordEt.error = "Password must be at least 6 characters"
                passwordEt.requestFocus()
                return@setOnClickListener
            }

            try {
                if (dbHelper.registerUser(username, email, password)) {
                    Log.i(TAG, "Registration successful for $email")
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Registration failed for $email - Database returned error")
                    Toast.makeText(this, "Registration Failed (Email might already exist)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration", e)
                Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
