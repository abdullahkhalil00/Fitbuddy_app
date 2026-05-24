package com.app.fitt_buddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DietAdapter(
    private val dietList: List<DietMeal>
) : RecyclerView.Adapter<DietAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvDayLabel: TextView = view.findViewById(R.id.tv_day_label)
        val tvBreakfast: TextView = view.findViewById(R.id.tv_breakfast)
        val tvLunch: TextView = view.findViewById(R.id.tv_lunch)
        val tvDinner: TextView = view.findViewById(R.id.tv_dinner)
        val tvCost: TextView = view.findViewById(R.id.tv_cost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diet_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = dietList[position]
        holder.tvDate.text = meal.date
        holder.tvDayLabel.text = "Day ${meal.dayNumber}"
        holder.tvBreakfast.text = "Breakfast: ${meal.breakfast}"
        holder.tvLunch.text = "Lunch: ${meal.lunch}"
        holder.tvDinner.text = "Dinner: ${meal.dinner}"
        holder.tvCost.text = "PKR ${meal.cost}"
    }

    override fun getItemCount() = dietList.size
}