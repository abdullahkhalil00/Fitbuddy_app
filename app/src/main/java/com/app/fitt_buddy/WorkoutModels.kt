package com.app.fitt_buddy

import java.io.Serializable

data class WorkoutExercise(
    val name: String = "",
    val duration: Int = 5, // in minutes
    val category: String = "",
    var isCompleted: Boolean = false
) : Serializable

data class DayPlan(
    val dayNumber: Int = 1,
    val exercises: List<WorkoutExercise> = emptyList()
)

// For Local Progress Tracking (Cache)
data class DailyProgress(
    val date: String, // Format: YYYY-MM-DD
    val completedMinutes: Int,
    val totalMinutes: Int,
    val exerciseStatus: Map<String, Boolean> // Exercise Name -> Status
)