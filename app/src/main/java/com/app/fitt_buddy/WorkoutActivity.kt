package com.app.fitt_buddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WorkoutActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    
    private lateinit var tvWeek: TextView
    private lateinit var tvTitle: TextView
    private lateinit var rvExercises: RecyclerView
    private lateinit var llDaysContainer: LinearLayout
    
    private var currentWeek = 1
    private var maxWeeks = 4 
    private var userGoal = "Shoulders"
    
    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var adapter: WorkoutAdapter
    
    private val dateList = mutableListOf<Date>()
    private var selectedDateIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_details)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvWeek = findViewById(R.id.tv_current_week)
        tvTitle = findViewById(R.id.tv_workout_title)
        rvExercises = findViewById(R.id.rv_exercises)
        llDaysContainer = findViewById(R.id.ll_days_container)

        generateDateList()
        setupRecyclerView()
        fetchUserData()

        findViewById<ImageButton>(R.id.btn_prev_week).setOnClickListener {
            if (currentWeek > 1) {
                currentWeek--
                updateWeekUI()
            }
        }

        findViewById<ImageButton>(R.id.btn_next_week).setOnClickListener {
            if (currentWeek < maxWeeks) {
                currentWeek++
                updateWeekUI()
            } else {
                Toast.makeText(this, "Your plan duration has reached the limit!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }

        setupBottomNav()
    }

    private fun generateDateList() {
        dateList.clear()
        val calendar = Calendar.getInstance()
        for (i in 0..6) {
            dateList.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter(exerciseList) { exercise ->
            saveProgressToCache(exercise.name, exercise.duration)
        }
        rvExercises.layoutManager = LinearLayoutManager(this)
        rvExercises.adapter = adapter
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            userGoal = doc.getString("goal") ?: "Shoulders"
            maxWeeks = doc.getLong("commitmentWeeks")?.toInt() ?: 4
            updateWeekUI()
        }
    }

    private fun updateWeekUI() {
        tvWeek.text = "WEEK $currentWeek"
        setupDays()
        loadExercises()
    }

    private fun setupDays() {
        llDaysContainer.removeAllViews()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val inflater = LayoutInflater.from(this)
        
        for (i in 0 until dateList.size) {
            val date = dateList[i]
            val dayView = inflater.inflate(R.layout.item_day_chip, llDaysContainer, false)
            val tvDay = dayView.findViewById<TextView>(R.id.tv_day_number)
            
            tvDay.text = dateFormat.format(date)
            
            if (i == selectedDateIndex) {
                dayView.setBackgroundResource(R.drawable.bg_capsule_selected)
                tvDay.setTextColor(getColor(R.color.bg_dark))
            } else {
                dayView.setBackgroundResource(R.drawable.bg_card_unselected)
                tvDay.setTextColor(getColor(R.color.white))
            }

            dayView.setOnClickListener {
                selectedDateIndex = i
                setupDays()
                loadExercises()
            }
            llDaysContainer.addView(dayView)
        }
    }

    private fun loadExercises() {
        val calendar = Calendar.getInstance()
        calendar.time = dateList[selectedDateIndex]
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val category = when (dayOfWeek) {
            Calendar.MONDAY -> "Chest"
            Calendar.TUESDAY -> "Back"
            Calendar.WEDNESDAY -> "Shoulders"
            Calendar.THURSDAY -> "Biceps"
            Calendar.FRIDAY -> "Triceps"
            Calendar.SATURDAY -> "Legs"
            Calendar.SUNDAY -> "Abs"
            else -> userGoal
        }
        tvTitle.text = category.uppercase()

        db.collection("workout").document(category)
            .get().addOnSuccessListener { doc ->
                exerciseList.clear()
                if (doc.exists()) {
                    val data = doc.data
                    data?.forEach { (name, durationValue) ->
                        val durationStr = durationValue.toString()
                        val durationInt = durationStr.replace(" min", "").trim().toIntOrNull() ?: 5
                        
                        exerciseList.add(WorkoutExercise(
                            name = name.trim(),
                            duration = durationInt,
                            category = category
                        ))
                    }
                }
                
                if (exerciseList.isEmpty()) loadDefaultExercises(category)
                
                // Mark as completed if already saved today
                checkCompletedStatus()
                adapter.notifyDataSetChanged()
            }.addOnFailureListener {
                loadDefaultExercises(category)
                checkCompletedStatus()
                adapter.notifyDataSetChanged()
            }
    }

    private fun checkCompletedStatus() {
        val sharedPref = getSharedPreferences("WorkoutCache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        exerciseList.forEach { exercise ->
            exercise.isCompleted = sharedPref.getBoolean("${today}_${exercise.name.trim()}", false)
        }
    }

    private fun loadDefaultExercises(category: String) {
        when (category) {
            "Shoulders" -> {
                exerciseList.add(WorkoutExercise("Shoulder press", 10, category))
                exerciseList.add(WorkoutExercise("Lateral raises", 9, category))
            }
            "Chest" -> {
                exerciseList.add(WorkoutExercise("Bench press", 12, category))
                exerciseList.add(WorkoutExercise("Push ups", 8, category))
            }
            "Back" -> {
                exerciseList.add(WorkoutExercise("Lat pull down", 10, category))
                exerciseList.add(WorkoutExercise("Bent over row", 10, category))
            }
            "Biceps" -> {
                exerciseList.add(WorkoutExercise("Hammer curls", 8, category))
                exerciseList.add(WorkoutExercise("Barbell curls", 8, category))
            }
            "Triceps" -> {
                exerciseList.add(WorkoutExercise("Skull crushers", 10, category))
                exerciseList.add(WorkoutExercise("Tricep pushdown", 8, category))
            }
            "Legs" -> {
                exerciseList.add(WorkoutExercise("Squats", 15, category))
                exerciseList.add(WorkoutExercise("Leg press", 10, category))
            }
            "Abs" -> {
                exerciseList.add(WorkoutExercise("Crunches", 5, category))
                exerciseList.add(WorkoutExercise("Plank", 5, category))
            }
            else -> {
                exerciseList.add(WorkoutExercise("Stretching", 10, category))
                exerciseList.add(WorkoutExercise("Cardio", 15, category))
            }
        }
    }

    private fun saveProgressToCache(exerciseName: String, duration: Int) {
        val sharedPref = getSharedPreferences("WorkoutCache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cleanName = exerciseName.trim()
        
        val currentExerciseMins = sharedPref.getInt("${today}_${cleanName}_mins", 0)
        
        with(sharedPref.edit()) {
            putInt("${today}_${cleanName}_mins", currentExerciseMins + duration)
            putBoolean("${today}_${cleanName}", true)
            apply()
        }
        Toast.makeText(this, "$cleanName Completed!", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_workouts
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, homeactivity::class.java)); true }
                R.id.nav_recipes -> { startActivity(Intent(this, RecipeVaultActivity::class.java)); true }
                R.id.nav_workouts -> true
                else -> false
            }
        }
    }
}
