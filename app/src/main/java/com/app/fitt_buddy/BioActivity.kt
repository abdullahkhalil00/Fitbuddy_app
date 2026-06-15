package com.app.fitt_buddy

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BioActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val selectedPreferences = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bio)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainView = findViewById<View>(R.id.main_bio)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<TextView>(R.id.tv_login_link).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Dietary tags selection
        val tags = listOf(
            findViewById<TextView>(R.id.tag_vegan) to "Vegan",
            findViewById<TextView>(R.id.tag_keto) to "Keto",
            findViewById<TextView>(R.id.tag_paleo) to "Paleo",
            findViewById<TextView>(R.id.tag_gluten_free) to "Gluten Free",
            findViewById<TextView>(R.id.tag_pescatarian) to "Pescatarian",
            findViewById<TextView>(R.id.tag_halal) to "Halal"
        )

        tags.forEach { (tag, label) ->
            tag.setOnClickListener {
                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    selectedPreferences.add(label)
                } else {
                    selectedPreferences.remove(label)
                }
            }
        }

        // Meal reminder switch logic
        val mealSwitch = findViewById<SwitchMaterial>(R.id.sw_meal_reminder)
        mealSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mealSwitch.thumbTintList = ColorStateList.valueOf(getColor(R.color.primary_green))
                mealSwitch.trackTintList = ColorStateList.valueOf(getColor(R.color.primary_green))
            } else {
                mealSwitch.thumbTintList = ColorStateList.valueOf(getColor(R.color.white))
                mealSwitch.trackTintList = ColorStateList.valueOf(getColor(R.color.white))
                mealSwitch.alpha = 0.3f
            }
        }

        val confirmButton: androidx.appcompat.widget.AppCompatButton = findViewById(R.id.btn_confirm_macros)
        confirmButton.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val updates = mapOf(
                "dietaryPreferences" to selectedPreferences,
                "mealReminders" to mealSwitch.isChecked
            )

            db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    val intent = Intent(this, ProtocolActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}