package com.app.fitt_buddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcome = view.findViewById<TextView>(R.id.tv_welcome_name)
        val tvCalorieTotal = view.findViewById<TextView>(R.id.tv_calories_total)
        val tvCalorieValue = view.findViewById<TextView>(R.id.tv_calories_value)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "User"
                        val totalCalories = document.getLong("calories") ?: 2400
                        
                        tvWelcome.text = "Hello, $name!"
                        tvCalorieTotal.text = "↑ of $totalCalories kcal"
                        
                        // For demonstration, setting remaining calories to the total
                        // In a real app, you'd subtract logged calories
                        tvCalorieValue.text = String.format("%, d", totalCalories)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}