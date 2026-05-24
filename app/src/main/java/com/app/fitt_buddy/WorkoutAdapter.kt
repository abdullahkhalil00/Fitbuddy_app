package com.app.fitt_buddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val exercises: List<WorkoutExercise>,
    private val onStartClick: (WorkoutExercise) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_exercise_category)
        val tvName: TextView = view.findViewById(R.id.tv_exercise_name)
        val tvDuration: TextView = view.findViewById(R.id.tv_exercise_duration)
        val btnStart: Button = view.findViewById(R.id.btn_start_exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvCategory.text = exercise.category.uppercase()
        holder.tvName.text = exercise.name
        holder.tvDuration.text = "${exercise.duration} min"
        
        holder.btnStart.setOnClickListener {
            onStartClick(exercise)
        }
    }

    override fun getItemCount() = exercises.size
}