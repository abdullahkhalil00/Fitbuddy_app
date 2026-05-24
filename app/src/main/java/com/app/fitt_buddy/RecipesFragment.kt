package com.app.fitt_buddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipesFragment : Fragment() {

    private var tags: List<TextView> = listOf()
    private lateinit var etSearch: EditText
    private lateinit var rvRecipes: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var recipeAdapter: RecipeAdapter
    private val mealsList = mutableListOf<Meal>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recipe_vault, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        etSearch = view.findViewById(R.id.et_search)
        rvRecipes = view.findViewById(R.id.rv_recipes)
        pbLoading = view.findViewById(R.id.pb_loading)

        val tagAll = view.findViewById<TextView>(R.id.tag_all)
        val tagProtein = view.findViewById<TextView>(R.id.tag_protein)
        val tagCalories = view.findViewById<TextView>(R.id.tag_calories)
        val tagVegan = view.findViewById<TextView>(R.id.tag_vegan)

        tags = listOf(tagAll, tagProtein, tagCalories, tagVegan)

        setupRecyclerView()

        // Handle Tag Clicks
        tags.forEach { tag ->
            tag.isClickable = true
            tag.isFocusable = true
            tag.setOnClickListener {
                Log.d("RecipesFragment", "Tag clicked: ${tag.text}")
                selectTag(tag)
                searchMeals(tag.text.toString()) // Search by tag
            }
        }

        // Handle Search Action from Keyboard
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

        // Load default recipes on start
        if (mealsList.isEmpty()) {
            searchMeals("chicken")
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(mealsList)
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        rvRecipes.adapter = recipeAdapter
    }

    private fun selectTag(selectedTag: TextView) {
        tags.forEach { tag ->
            if (tag == selectedTag) {
                tag.setBackgroundResource(R.drawable.bg_capsule_selected)
                tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark))
            } else {
                tag.setBackgroundResource(R.drawable.bg_card_unselected)
                tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
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
                if (!isAdded) return
                pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    val meals = response.body()?.meals ?: emptyList()
                    mealsList.clear()
                    mealsList.addAll(meals)
                    recipeAdapter.notifyDataSetChanged()
                    
                    if (mealsList.isEmpty()) {
                        Toast.makeText(requireContext(), "No recipes found for '$query'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                if (!isAdded) return
                pbLoading.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to connect: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
