package com.app.fitt_buddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommitmentActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedWeeks: Int = 0
    private var selectedCalories: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_commitment)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainView = findViewById<View>(R.id.commitment_main)
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

        val durationOptions = listOf(
            findViewById<TextView>(R.id.tv_duration_4) to 4,
            findViewById<TextView>(R.id.tv_duration_8) to 8,
            findViewById<TextView>(R.id.tv_duration_12) to 12
        )

        durationOptions.forEach { (view, weeks) ->
            view.setOnClickListener { selected ->
                durationOptions.forEach { 
                    it.first.setBackgroundResource(R.drawable.bg_capsule_unselected)
                    it.first.setTextColor(getColor(R.color.white))
                }
                selected.setBackgroundResource(R.drawable.bg_capsule_selected)
                (selected as TextView).setTextColor(getColor(R.color.bg_dark))
                selectedWeeks = weeks
            }
        }

        val calorieOptions = listOf(
            findViewById<View>(R.id.tv_cal_2200) to 2200,
            findViewById<View>(R.id.tv_cal_2300) to 2300,
            findViewById<View>(R.id.cl_cal_optimal) to 2450
        )

        calorieOptions.forEach { (view, cals) ->
            view.setOnClickListener { selected ->
                calorieOptions.forEach { 
                    it.first.setBackgroundResource(R.drawable.bg_card_unselected)
                    it.first.alpha = 0.4f
                }
                selected.setBackgroundResource(R.drawable.bg_card_selected)
                selected.alpha = 1.0f
                selectedCalories = cals
                
                findViewById<TextView>(R.id.tv_calorie_value).text = String.format("%, d", cals)
            }
        }

        val nextStepButton: androidx.appcompat.widget.AppCompatButton = findViewById(R.id.btn_next_step)
        nextStepButton.setOnClickListener {
            if (selectedWeeks == 0 || selectedCalories == 0) {
                Toast.makeText(this, "Please select both duration and calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val updates = mapOf(
                "commitmentWeeks" to selectedWeeks,
                "calories" to selectedCalories
            )

            db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    val intent = Intent(this, BioActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}