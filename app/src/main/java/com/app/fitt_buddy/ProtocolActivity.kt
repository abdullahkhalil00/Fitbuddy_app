package com.app.fitt_buddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProtocolActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedLevel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_protocol)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainView = findViewById<View>(R.id.protocol_main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        val cards = listOf(
            findViewById<View>(R.id.cl_advanced) to "Advanced",
            findViewById<View>(R.id.cl_intermediate) to "Intermediate"
        )

        cards.forEach { (card, level) ->
            card?.setOnClickListener { selected ->
                cards.forEach { it.first?.isSelected = false }
                selected.isSelected = true
                selectedLevel = level
            }
        }

        val button : androidx.appcompat.widget.AppCompatButton = findViewById(R.id.btn_start_transformation)
        button.setOnClickListener {
            if (selectedLevel.isEmpty()) {
                Toast.makeText(this, "Please select your experience level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(userId)
                .update("experienceLevel", selectedLevel)
                .addOnSuccessListener {
                    val intent = Intent(this, homeactivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}