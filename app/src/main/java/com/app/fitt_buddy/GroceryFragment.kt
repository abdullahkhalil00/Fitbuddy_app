package com.app.fitt_buddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GroceryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var rvDietList: RecyclerView
    private lateinit var tvTotalCost: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvTitle: TextView

    private val dietList = mutableListOf<DietMeal>()
    private lateinit var adapter: DietAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_grocery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvDietList = view.findViewById(R.id.rv_diet_list)
        tvTotalCost = view.findViewById(R.id.tv_total_cost)
        pbLoading = view.findViewById(R.id.pb_loading)
        tvTitle = view.findViewById(R.id.tv_grocery_title)

        tvTitle.text = "Diet List"

        setupRecyclerView()
        fetchDietPlan()
        
        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = DietAdapter(dietList)
        rvDietList.layoutManager = LinearLayoutManager(requireContext())
        rvDietList.adapter = adapter
    }

    private fun fetchDietPlan() {
        if (!isAdded) return
        pbLoading.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            if (!isAdded) return@addOnSuccessListener
            var startDateStr = userDoc.getString("dietStartDate")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            if (startDateStr == null) {
                val todayStr = sdf.format(Date())
                db.collection("users").document(userId).update("dietStartDate", todayStr)
                startDateStr = todayStr
            }

            val startDate = sdf.parse(startDateStr!!) ?: Date()
            loadDailyRecipes(startDate)
        }.addOnFailureListener {
            if (isAdded) pbLoading.visibility = View.GONE
        }
    }

    private fun loadDailyRecipes(startDate: Date) {
        db.collection("Daily recipie").get().addOnSuccessListener { snapshots ->
            if (!isAdded) return@addOnSuccessListener
            pbLoading.visibility = View.GONE
            
            val templates = mutableMapOf<Int, DietMeal>()
            for (doc in snapshots) {
                val dayNum = doc.id.replace("Day ", "").trim().toIntOrNull() ?: continue
                val data = doc.data
                
                // Flexible mapping to handle Capitalized or lowercase field names from Firestore
                val meal = DietMeal(
                    breakfast = (data["Breakfast"] ?: data["breakfast"] ?: "").toString(),
                    lunch = (data["Lunch"] ?: data["lunch"] ?: "").toString(),
                    dinner = (data["Dinner"] ?: data["dinner"] ?: "").toString(),
                    cost = (data["Cost"] ?: data["cost"] ?: 0.0).toString().toDoubleOrNull() ?: 0.0,
                    dayNumber = dayNum
                )
                templates[dayNum] = meal
            }

            if (templates.isEmpty()) {
                Toast.makeText(requireContext(), "No diet templates found in Firestore", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            generateRollingList(startDate, templates)
        }.addOnFailureListener {
            if (isAdded) pbLoading.visibility = View.GONE
        }
    }

    private fun generateRollingList(startDate: Date, templates: Map<Int, DietMeal>) {
        dietList.clear()
        
        // Start showing from Today
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        // The date the plan actually started
        val startCal = Calendar.getInstance()
        startCal.time = startDate
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        
        for (i in 0..6) {
            val currentIterDate = today.clone() as Calendar
            currentIterDate.add(Calendar.DAY_OF_YEAR, i)

            // Calculate which "Day X" this date corresponds to in the 7-day cycle
            val diffInMillis = currentIterDate.timeInMillis - startCal.timeInMillis
            val diffInDays = (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
            
            // Cycle: (0%7)+1 = Day 1, (1%7)+1 = Day 2... (6%7)+1 = Day 7, (7%7)+1 = Day 1
            val dayInCycle = (diffInDays % 7) + 1
            
            val template = templates[dayInCycle] ?: DietMeal(dayNumber = dayInCycle)
            
            val meal = template.copy(
                date = sdf.format(currentIterDate.time),
                dayNumber = dayInCycle
            )
            dietList.add(meal)
            
            // Today's cost (first item in rolling list)
            if (i == 0) {
                tvTotalCost.text = "PKR ${meal.cost}"
            }
        }

        adapter.notifyDataSetChanged()
    }
}