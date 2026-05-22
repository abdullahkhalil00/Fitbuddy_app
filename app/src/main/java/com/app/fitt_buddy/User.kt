package com.app.fitt_buddy

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    var goal: String = "",
    var commitmentWeeks: Int = 0,
    var calories: Int = 0,
    var dietaryPreferences: List<String> = emptyList(),
    var mealReminders: Boolean = false,
    var experienceLevel: String = ""
)