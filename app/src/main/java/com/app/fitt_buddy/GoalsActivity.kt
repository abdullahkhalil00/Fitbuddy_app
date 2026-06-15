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

class GoalsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedGoal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goals)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainView = findViewById<View>(R.id.goals_main)
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

        val cards = listOf(
            findViewById<View>(R.id.cl_fat_loss) to "Fat Loss",
            findViewById<View>(R.id.cl_core_strength) to "Core Strength",
            findViewById<View>(R.id.cl_glow_up) to "Glow Up"
        )

        cards.forEach { (card, goal) ->
            card.setOnClickListener { selected ->
                cards.forEach { it.first.isSelected = false }
                selected.isSelected = true
                selectedGoal = goal
            }
        }

        val continueButton: androidx.appcompat.widget.AppCompatButton = findViewById(R.id.btn_continue)
        continueButton.setOnClickListener {
            if (selectedGoal.isEmpty()) {
                Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(userId)
                .update("goal", selectedGoal)
                .addOnSuccessListener {
                    val intent = Intent(this, CommitmentActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}