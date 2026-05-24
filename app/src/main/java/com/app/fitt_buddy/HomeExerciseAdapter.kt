package com.app.fitt_buddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeExerciseAdapter(
    private val exercises: List<WorkoutExercise>,
    private val completedMap: Map<String, Boolean>
) : RecyclerView.Adapter<HomeExerciseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_exercise_name)
        val tvDetails: TextView = view.findViewById(R.id.tv_exercise_details)
        val ivCheck: ImageView = view.findViewById(R.id.iv_check)
        val vIndicator: View = view.findViewById(R.id.v_status_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvName.text = exercise.name
        holder.tvDetails.text = "${exercise.duration} min • ${exercise.category}"
        
        val isDone = completedMap[exercise.name] ?: false
        if (isDone) {
            holder.ivCheck.visibility = View.VISIBLE
            holder.vIndicator.setBackgroundResource(R.color.primary_green)
        } else {
            holder.ivCheck.visibility = View.GONE
            holder.vIndicator.setBackgroundResource(android.R.color.darker_gray)
        }
    }

    override fun getItemCount() = exercises.size
}