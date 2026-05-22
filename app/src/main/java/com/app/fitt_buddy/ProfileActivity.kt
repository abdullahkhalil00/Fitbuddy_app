package com.app.fitt_buddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        val mainView = findViewById<View>(R.id.profile_main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile
        
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, homeactivity::class.java))

                    true
                }
                R.id.nav_recipes -> {
                    startActivity(Intent(this, RecipeVaultActivity::class.java))

                    true
                }
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))

                    true
                }
                R.id.nav_grocery -> {
                    startActivity(Intent(this, GroceryActivity::class.java))

                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}