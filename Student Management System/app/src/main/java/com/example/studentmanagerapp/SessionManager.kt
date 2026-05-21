package com.example.studentmanagerapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUser(username: String, email: String) {
        val editor = prefs.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("isLoggedIn", false)

    fun getUsername(): String? = prefs.getString("username", null)
    fun getEmail(): String? = prefs.getString("email", null)

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}