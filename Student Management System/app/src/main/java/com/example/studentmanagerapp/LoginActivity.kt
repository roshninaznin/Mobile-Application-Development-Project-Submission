// LoginActivity.kt
package com.example.studentmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_login)

        dbHelper = StudentDatabaseHelper(this)

        val emailEt: TextInputEditText = findViewById(R.id.emailEt)
        val passwordEt: TextInputEditText = findViewById(R.id.passwordEt)
        val loginBtn: Button = findViewById(R.id.loginBtn)
        val registerBtn: Button = findViewById(R.id.registerBtn)

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            Log.d(TAG, "Login attempt for email: $email")

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
            if (password.isEmpty()) {
                passwordEt.error = "Password is required"
                passwordEt.requestFocus()
                return@setOnClickListener
            }

            try {
                val username = dbHelper.loginUser(email, password)
                if (username != null) {
                    Log.i(TAG, "Login successful for user: $username ($email)")
                    sessionManager.saveUser(username, email)
                    Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "Login failed for email: $email - Invalid credentials")
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}