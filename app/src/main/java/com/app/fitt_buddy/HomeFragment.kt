package com.app.fitt_buddy

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var tvWelcome: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressPercentCenter: TextView
    private lateinit var tvProgressDetail: TextView
    private lateinit var pbProgress: ProgressBar
    private lateinit var rvTodayExercises: RecyclerView
    
    private val todayExercises = mutableListOf<WorkoutExercise>()
    private lateinit var exerciseAdapter: HomeExerciseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvWelcome = view.findViewById(R.id.tv_welcome_name)
        tvProgressPercent = view.findViewById(R.id.tv_calories_value)
        tvProgressPercentCenter = view.findViewById(R.id.tv_progress_percent_center)
        tvProgressDetail = view.findViewById(R.id.tv_calories_total)
        pbProgress = view.findViewById(R.id.pb_calories)
        rvTodayExercises = view.findViewById(R.id.rv_today_exercises)

        rvTodayExercises.layoutManager = LinearLayoutManager(requireContext())
        
        return view
    }

    private fun loadHomeData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (!isAdded) return@addOnSuccessListener
            if (document != null && document.exists()) {
                val name = document.getString("name") ?: "User"
                tvWelcome.text = "Hello, $name!"
                fetchTodayWorkout()
            }
        }.addOnFailureListener {
            if (isAdded) fetchTodayWorkout()
        }
    }

    private fun fetchTodayWorkout() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val category = when (dayOfWeek) {
            Calendar.MONDAY -> "Chest"
            Calendar.TUESDAY -> "Back"
            Calendar.WEDNESDAY -> "Shoulders"
            Calendar.THURSDAY -> "Biceps"
            Calendar.FRIDAY -> "Triceps"
            Calendar.SATURDAY -> "Legs"
            Calendar.SUNDAY -> "Abs"
            else -> "Full body"
        }

        db.collection("workout").document(category).get().addOnSuccessListener { doc ->
            if (!isAdded) return@addOnSuccessListener
            todayExercises.clear()
            if (doc.exists()) {
                doc.data?.forEach { (name, durationValue) ->
                    val durationInt = durationValue.toString().replace(" min", "").trim().toIntOrNull() ?: 5
                    todayExercises.add(WorkoutExercise(name, durationInt, category))
                }
            }
            
            // Fallback if empty
            if (todayExercises.isEmpty()) {
                loadDefaultExercises(category)
            }
            
            updateProgressUI()
        }.addOnFailureListener {
            if (!isAdded) return@addOnFailureListener
            todayExercises.clear()
            loadDefaultExercises(category)
            updateProgressUI()
        }
    }

    private fun loadDefaultExercises(category: String) {
        // Default values for common categories
        when (category) {
            "Shoulders" -> {
                todayExercises.add(WorkoutExercise("Shoulder press", 10, category))
                todayExercises.add(WorkoutExercise("Lateral raises", 9, category))
            }
            "Chest" -> {
                todayExercises.add(WorkoutExercise("Bench press", 12, category))
                todayExercises.add(WorkoutExercise("Push ups", 8, category))
            }
            else -> {
                todayExercises.add(WorkoutExercise("Generic Exercise", 10, category))
            }
        }
    }

    private fun updateProgressUI() {
        if (!isAdded) return
        val sharedPref = requireContext().getSharedPreferences("WorkoutCache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completedMins = sharedPref.getInt("${today}_completed", 0)
        
        var totalMins = 0
        val completedMap = mutableMapOf<String, Boolean>()
        todayExercises.forEach { 
            totalMins += it.duration
            completedMap[it.name] = sharedPref.getBoolean("${today}_${it.name}", false)
        }

        val percent = if (totalMins > 0) (completedMins * 100) / totalMins else 0
        val cappedPercent = if (percent > 100) 100 else percent
        
        tvProgressPercent.text = "$cappedPercent%"
        tvProgressPercentCenter.text = "$cappedPercent%"
        pbProgress.progress = cappedPercent
        tvProgressDetail.text = "$completedMins of $totalMins min completed"

        exerciseAdapter = HomeExerciseAdapter(todayExercises, completedMap)
        rvTodayExercises.adapter = exerciseAdapter
    }

    override fun onResume() {
        super.onResume()
        loadHomeData()
    }
}