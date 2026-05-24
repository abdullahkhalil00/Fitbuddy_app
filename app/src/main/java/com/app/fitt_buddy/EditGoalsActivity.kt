package com.app.fitt_buddy

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditGoalsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerDuration: Spinner
    private lateinit var spinnerCalories: Spinner
    private lateinit var pbLoading: ProgressBar

    private val goalOptions = listOf("Fat Loss", "Core Strength", "Glow Up")
    private val durationOptions = listOf("4 Weeks", "8 Weeks", "12 Weeks")
    private val durationValues = listOf(4, 8, 12)
    private val calorieOptions = listOf("2200 kcal", "2300 kcal", "2450 kcal")
    private val calorieValues = listOf(2200, 2300, 2450)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_goals)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        spinnerGoal = findViewById(R.id.spinner_goal)
        spinnerDuration = findViewById(R.id.spinner_duration)
        spinnerCalories = findViewById(R.id.spinner_calories)
        pbLoading = findViewById(R.id.pb_loading)

        setupSpinners()
        fetchCurrentGoals()

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_save).setOnClickListener { saveChanges() }
    }

    private fun setupSpinners() {
        val goalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goalOptions)
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGoal.adapter = goalAdapter

        val durationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durationOptions)
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDuration.adapter = durationAdapter

        val calorieAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, calorieOptions)
        calorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCalories.adapter = calorieAdapter
    }

    private fun fetchCurrentGoals() {
        val userId = auth.currentUser?.uid ?: return
        pbLoading.visibility = View.VISIBLE
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                pbLoading.visibility = View.GONE
                if (doc.exists()) {
                    val currentGoal = doc.getString("goal")
                    val currentWeeks = doc.getLong("commitmentWeeks")?.toInt()
                    val currentCalories = doc.getLong("calories")?.toInt()

                    // Pre-fill Goal
                    val goalIndex = goalOptions.indexOf(currentGoal)
                    if (goalIndex != -1) spinnerGoal.setSelection(goalIndex)

                    // Pre-fill Duration
                    val durationIndex = durationValues.indexOf(currentWeeks)
                    if (durationIndex != -1) spinnerDuration.setSelection(durationIndex)

                    // Pre-fill Calories
                    val calorieIndex = calorieValues.indexOf(currentCalories)
                    if (calorieIndex != -1) spinnerCalories.setSelection(calorieIndex)
                }
            }
            .addOnFailureListener {
                pbLoading.visibility = View.GONE
                Toast.makeText(this, "Failed to load current goals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val userId = auth.currentUser?.uid ?: return
        
        val newGoal = spinnerGoal.selectedItem.toString()
        val newWeeks = durationValues[spinnerDuration.selectedItemPosition]
        val newCalories = calorieValues[spinnerCalories.selectedItemPosition]

        pbLoading.visibility = View.VISIBLE
        
        val updates = mapOf(
            "goal" to newGoal,
            "commitmentWeeks" to newWeeks,
            "calories" to newCalories
        )

        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                pbLoading.visibility = View.GONE
                Toast.makeText(this, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                pbLoading.visibility = View.GONE
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
