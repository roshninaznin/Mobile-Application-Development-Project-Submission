package com.example.studentmanagerapp

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private var studentId: Int = -1
    private val TAG = "AddEditStudentActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            finish()
            return
        }
        
        setContentView(R.layout.activity_add_edit_student)

        dbHelper = StudentDatabaseHelper(this)

        val nameEt: TextInputEditText = findViewById(R.id.nameEt)
        val ageEt: TextInputEditText = findViewById(R.id.ageEt)
        val gradeEt: TextInputEditText = findViewById(R.id.gradeEt)
        val emailEt: TextInputEditText = findViewById(R.id.emailEt)
        val phoneEt: TextInputEditText = findViewById(R.id.phoneEt)
        val saveBtn: Button = findViewById(R.id.saveBtn)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        studentId = intent.getIntExtra("studentId", -1)
        if (studentId != -1) {
            toolbar.title = "Edit Student"
            Log.d(TAG, "Editing student with ID: $studentId")
            try {
                val currentUserEmail = sessionManager.getEmail() ?: ""
                val student = dbHelper.getAllStudents(currentUserEmail).find { it.id == studentId }
                student?.let {
                    nameEt.setText(it.name)
                    ageEt.setText(it.age.toString())
                    gradeEt.setText(it.grade)
                    emailEt.setText(it.email)
                    phoneEt.setText(it.phone)
                } ?: run {
                    Log.e(TAG, "Student with ID $studentId not found")
                    Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching student details", e)
            }
        } else {
            toolbar.title = "Add Student"
            Log.d(TAG, "Adding new student")
        }

        saveBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val ageStr = ageEt.text.toString().trim()
            val grade = gradeEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val phone = phoneEt.text.toString().trim()

            // Field Validations
            if (name.isEmpty()) {
                nameEt.error = "Name is required"
                nameEt.requestFocus()
                return@setOnClickListener
            }
            if (ageStr.isEmpty()) {
                ageEt.error = "Age is required"
                ageEt.requestFocus()
                return@setOnClickListener
            }
            val age = ageStr.toIntOrNull() ?: 0
            if (age <= 0 || age > 100) {
                ageEt.error = "Enter a valid age (1-100)"
                ageEt.requestFocus()
                return@setOnClickListener
            }
            if (grade.isEmpty()) {
                gradeEt.error = "Grade is required"
                gradeEt.requestFocus()
                return@setOnClickListener
            }
            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.error = "Invalid email format"
                emailEt.requestFocus()
                return@setOnClickListener
            }
            if (phone.isNotEmpty() && phone.length < 10) {
                phoneEt.error = "Enter a valid phone number"
                phoneEt.requestFocus()
                return@setOnClickListener
            }

            Log.d(TAG, "Validation passed. Saving student: $name")

            try {
                val currentUserEmail = sessionManager.getEmail() ?: ""
                val success = if (studentId == -1) {
                    dbHelper.addStudent(name, age, grade, email, phone, currentUserEmail)
                } else {
                    dbHelper.updateStudent(Student(studentId, name, age, grade, email, phone))
                }

                if (success) {
                    Log.i(TAG, "Student saved successfully")
                    Toast.makeText(this, "Student saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Failed to save student to database")
                    Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving student", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}