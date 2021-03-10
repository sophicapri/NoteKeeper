package com.socap.notekeeper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class CourseRecyclerAdapter(context: Context, private val courses: List<CourseInfo>) : RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val itemView = layoutInflater.inflate(R.layout.item_course_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.textCourse.text = course.title
        holder.currentPosition = position
    }

    override fun getItemCount() = courses.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textCourse: TextView = itemView.findViewById(R.id.text_course)
        var currentPosition = 0

        init {
            itemView.setOnClickListener { view ->
                Snackbar.make(view, courses[currentPosition].title, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}