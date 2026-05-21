// MainActivity.kt
package com.example.studentmanagerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private var studentList = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        dbHelper = StudentDatabaseHelper(this)

        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recyclerView)
        val addFab: FloatingActionButton = findViewById(R.id.addFab)
        val searchView: SearchView = findViewById(R.id.searchView)

        val currentUserEmail = sessionManager.getEmail() ?: ""
        studentList = dbHelper.getAllStudents(currentUserEmail)
        studentAdapter = StudentAdapter(this, studentList, dbHelper)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter

        // Swipe to Delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val student = studentAdapter.getStudentAt(position)
                
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete ${student.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (dbHelper.deleteStudent(student.id)) {
                            studentAdapter.removeStudent(position)
                            Toast.makeText(this@MainActivity, "Student deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        studentAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        // Search Filter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                studentAdapter.filter.filter(newText)
                return true
            }
        })

        addFab.setOnClickListener {
            val intent = Intent(this, AddEditStudentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            R.id.action_export -> {
                exportToCSV()
                true
            }
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfileDialog() {
        val username = sessionManager.getUsername()
        val email = sessionManager.getEmail()
        AlertDialog.Builder(this)
            .setTitle("User Profile")
            .setMessage("Username: $username\nEmail: $email")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportToCSV() {
        val currentUserEmail = sessionManager.getEmail() ?: ""
        val students = dbHelper.getAllStudents(currentUserEmail)
        if (students.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val file = File(getExternalFilesDir(null), "students_backup.csv")
            val writer = FileWriter(file)
            writer.append("ID,Name,Age,Grade,Email,Phone\n")
            for (s in students) {
                writer.append("${s.id},${s.name},${s.age},${s.grade},${s.email},${s.phone}\n")
            }
            writer.flush()
            writer.close()
            Toast.makeText(this, "Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val currentUserEmail = sessionManager.getEmail() ?: ""
        val updatedList = dbHelper.getAllStudents(currentUserEmail)
        studentAdapter.updateData(updatedList)
    }
}
