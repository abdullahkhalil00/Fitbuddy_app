package com.app.fitt_buddy

data class DietMeal(
    var breakfast: String = "",
    var lunch: String = "",
    var dinner: String = "",
    var cost: Double = 0.0,
    var date: String = "",
    var dayNumber: Int = 0
) {
    constructor() : this("", "", "", 0.0, "", 0)

    companion object {
        fun fromMap(data: Map<String, Any?>, dayNum: Int): DietMeal {
            val meal = DietMeal()
            meal.dayNumber = dayNum
            
            // Flexible mapping for Breakfast
            meal.breakfast = (data["Breakfast"] ?: data["breakfast"] ?: data["break fast"] ?: data["Break fast"] ?: "N/A").toString()
            
            // Flexible mapping for Lunch
            meal.lunch = (data["Lunch"] ?: data["lunch"] ?: "N/A").toString()
            
            // Flexible mapping for Dinner
            meal.dinner = (data["Dinner"] ?: data["dinner"] ?: "N/A").toString()
            
            // Flexible mapping for Cost
            val costVal = data["Cost"] ?: data["cost"] ?: 0.0
            meal.cost = when (costVal) {
                is Number -> costVal.toDouble()
                is String -> costVal.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            return meal
        }
    }
}