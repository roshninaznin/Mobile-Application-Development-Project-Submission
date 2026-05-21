package com.example.studentmanagerapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class StudentAdapter(
    private val context: Context,
    private var students: ArrayList<Student>,
    private val dbHelper: StudentDatabaseHelper
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>(), Filterable {

    private var studentsFull = ArrayList(students)

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView = itemView.findViewById(R.id.nameTv)
        val gradeTv: TextView = itemView.findViewById(R.id.gradeTv)
        val emailTv: TextView = itemView.findViewById(R.id.emailTv)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val student = students[position]
                    val intent = Intent(context, AddEditStudentActivity::class.java)
                    intent.putExtra("studentId", student.id)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.nameTv.text = student.name
        holder.gradeTv.text = "Grade: ${student.grade}"
        holder.emailTv.text = student.email
    }

    fun updateData(newList: ArrayList<Student>) {
        students = newList
        studentsFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun getStudentAt(position: Int): Student = students[position]

    fun removeStudent(position: Int) {
        students.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return studentFilter
    }

    private val studentFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Student>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(studentsFull)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in studentsFull) {
                    if (item.name.lowercase().contains(filterPattern) || 
                        item.grade.lowercase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            students.clear()
            students.addAll(results?.values as ArrayList<Student>)
            notifyDataSetChanged()
        }
    }
}