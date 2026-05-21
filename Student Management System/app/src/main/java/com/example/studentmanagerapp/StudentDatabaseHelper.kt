package com.example.studentmanagerapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class StudentDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"
        private const val DATABASE_NAME = "student_manager.db"
        private const val DATABASE_VERSION = 1

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "username"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"

        // Students table
        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_STUDENT_ID = "id"
        private const val COLUMN_STUDENT_NAME = "name"
        private const val COLUMN_STUDENT_AGE = "age"
        private const val COLUMN_STUDENT_GRADE = "grade"
        private const val COLUMN_STUDENT_EMAIL = "email"
        private const val COLUMN_STUDENT_PHONE = "phone"
        private const val COLUMN_STUDENT_OWNER = "owner_email"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.i(TAG, "Creating database tables...")
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS(
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_EMAIL TEXT UNIQUE,
                $COLUMN_USER_PASSWORD TEXT
            )
        """.trimIndent()

        val createStudentsTable = """
            CREATE TABLE $TABLE_STUDENTS(
                $COLUMN_STUDENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_NAME TEXT,
                $COLUMN_STUDENT_AGE INTEGER,
                $COLUMN_STUDENT_GRADE TEXT,
                $COLUMN_STUDENT_EMAIL TEXT,
                $COLUMN_STUDENT_PHONE TEXT,
                $COLUMN_STUDENT_OWNER TEXT
            )
        """.trimIndent()

        db?.execSQL(createUsersTable)
        db?.execSQL(createStudentsTable)
        Log.i(TAG, "Tables created successfully.")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    // Register User
    fun registerUser(username: String, email: String, password: String): Boolean {
        if (isEmailExists(email)) {
            Log.w(TAG, "Registration failed: Email $email already exists.")
            return false
        }
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_USER_NAME, username)
                put(COLUMN_USER_EMAIL, email)
                put(COLUMN_USER_PASSWORD, password)
            }
            val result = db.insert(TABLE_USERS, null, values)
            db.close()
            val success = result != -1L
            if (success) Log.i(TAG, "User registered successfully: $email") 
            else Log.e(TAG, "Registration failed in DB for: $email")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during user registration", e)
            false
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=?", arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Login User
    fun loginUser(email: String, password: String): String? {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT $COLUMN_USER_NAME FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=? AND $COLUMN_USER_PASSWORD=?",
                arrayOf(email, password)
            )
            var username: String? = null
            if (cursor.moveToFirst()) {
                username = cursor.getString(0)
            }
            cursor.close()
            db.close()
            Log.d(TAG, "Login attempt for $email: ${if (username != null) "Success" else "Failed"}")
            username
        } catch (e: Exception) {
            Log.e(TAG, "Error during login query", e)
            null
        }
    }

    // CRUD Students
    fun addStudent(name: String, age: Int, grade: String, email: String, phone: String, ownerEmail: String): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_NAME, name)
                put(COLUMN_STUDENT_AGE, age)
                put(COLUMN_STUDENT_GRADE, grade)
                put(COLUMN_STUDENT_EMAIL, email)
                put(COLUMN_STUDENT_PHONE, phone)
                put(COLUMN_STUDENT_OWNER, ownerEmail)
            }
            val result = db.insert(TABLE_STUDENTS, null, values)
            db.close()
            val success = result != -1L
            if (success) Log.d(TAG, "Student added for $ownerEmail: $name") else Log.e(TAG, "Failed to add student: $name")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error adding student", e)
            false
        }
    }

    fun getAllStudents(ownerEmail: String): ArrayList<Student> {
        val list = ArrayList<Student>()
        try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_OWNER=?", arrayOf(ownerEmail))
            if (cursor.moveToFirst()) {
                do {
                    val student = Student(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_GRADE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_PHONE))
                    )
                    list.add(student)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            Log.d(TAG, "Fetched ${list.size} students for $ownerEmail.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching students", e)
        }
        return list
    }

    fun updateStudent(student: Student): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_NAME, student.name)
                put(COLUMN_STUDENT_AGE, student.age)
                put(COLUMN_STUDENT_GRADE, student.grade)
                put(COLUMN_STUDENT_EMAIL, student.email)
                put(COLUMN_STUDENT_PHONE, student.phone)
            }
            val result = db.update(
                TABLE_STUDENTS,
                values,
                "$COLUMN_STUDENT_ID=?",
                arrayOf(student.id.toString())
            )
            db.close()
            val success = result > 0
            if (success) Log.d(TAG, "Student updated ID: ${student.id}") else Log.e(TAG, "Failed to update student ID: ${student.id}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student", e)
            false
        }
    }

    fun deleteStudent(studentId: Int): Boolean {
        return try {
            val db = this.writableDatabase
            val result = db.delete(TABLE_STUDENTS, "$COLUMN_STUDENT_ID=?", arrayOf(studentId.toString()))
            db.close()
            val success = result > 0
            if (success) Log.d(TAG, "Student deleted ID: $studentId") else Log.e(TAG, "Failed to delete student ID: $studentId")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting student", e)
            false
        }
    }
}