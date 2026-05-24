package com.app.fitt_buddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeVaultActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var rvRecipes: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var recipeAdapter: RecipeAdapter
    private val mealsList = mutableListOf<Meal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_vault)
        
        val mainView = findViewById<View>(R.id.recipie_vault)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearch = findViewById(R.id.et_search)
        rvRecipes = findViewById(R.id.rv_recipes)
        pbLoading = findViewById(R.id.pb_loading)

        setupRecyclerView()

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchMeals(query)
                }
                true
            } else {
                false
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_recipes
        
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, homeactivity::class.java))
                    true
                }
                R.id.nav_recipes -> true
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))
                    true
                }
                R.id.nav_grocery -> {
                    startActivity(Intent(this, GroceryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(mealsList)
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = recipeAdapter
    }

    private fun searchMeals(query: String) {
        pbLoading.visibility = View.VISIBLE
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(MealDBApiService::class.java)
        
        service.searchMeals(query).enqueue(object : Callback<MealResponse> {
            override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    val meals = response.body()?.meals ?: emptyList()
                    mealsList.clear()
                    mealsList.addAll(meals)
                    recipeAdapter.notifyDataSetChanged()
                    
                    if (mealsList.isEmpty()) {
                        Toast.makeText(this@RecipeVaultActivity, "No recipes found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RecipeVaultActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                pbLoading.visibility = View.GONE
                Toast.makeText(this@RecipeVaultActivity, "Failed to connect: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}