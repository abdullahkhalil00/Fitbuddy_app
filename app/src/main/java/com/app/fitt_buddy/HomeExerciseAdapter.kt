package com.app.fitt_buddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeExerciseAdapter(
    private val exercises: List<WorkoutExercise>,
    private val completedMinutesMap: Map<String, Int>
) : RecyclerView.Adapter<HomeExerciseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_exercise_name)
        val tvDetails: TextView = view.findViewById(R.id.tv_exercise_details)
        val ivCheck: ImageView = view.findViewById(R.id.iv_check)
        val vIndicator: View = view.findViewById(R.id.v_status_indicator)
        val pbExercise: ProgressBar = view.findViewById(R.id.pb_exercise_progress)
        val tvProgressText: TextView = view.findViewById(R.id.tv_exercise_progress_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvName.text = exercise.name
        holder.tvDetails.text = "${exercise.duration} min • ${exercise.category}"
        
        val completedMins = completedMinutesMap[exercise.name] ?: 0
        
        // Use a 0-100 scale for the progress bar to ensure visual consistency
        val progressPercent = if (exercise.duration > 0) {
            (completedMins * 100) / exercise.duration
        } else {
            0
        }
        
        holder.pbExercise.max = 100
        holder.pbExercise.progress = if (progressPercent > 100) 100 else progressPercent
        holder.tvProgressText.text = "$completedMins/${exercise.duration} min"
        
        if (completedMins >= exercise.duration) {
            holder.ivCheck.visibility = View.VISIBLE
            holder.vIndicator.setBackgroundResource(R.color.primary_green)
        } else {
            holder.ivCheck.visibility = View.GONE
            holder.vIndicator.setBackgroundResource(android.R.color.darker_gray)
        }
    }

    override fun getItemCount() = exercises.size
}
