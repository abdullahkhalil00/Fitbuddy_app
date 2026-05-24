package com.app.fitt_buddy

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MealResponse(
    @SerializedName("meals") val meals: List<Meal>?
) : Serializable

data class Meal(
    @SerializedName("strMeal") val label: String,
    @SerializedName("strMealThumb") val image: String,
    @SerializedName("strCategory") val source: String, // Using category as source/subtitle
    @SerializedName("strSource") val url: String?,
    @SerializedName("strInstructions") val instructions: String?
) : Serializable

data class RecipeResponse(
    @SerializedName("hits") val hits: List<RecipeHit>?
)

data class RecipeHit(
    @SerializedName("recipe") val recipe: Recipe
)

data class Recipe(
    val label: String,
    val image: String,
    val source: String,
    val url: String
)
